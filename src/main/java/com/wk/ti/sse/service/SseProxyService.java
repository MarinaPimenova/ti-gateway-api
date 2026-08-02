package com.wk.ti.sse.service;

import com.wk.ti.exception.SSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseProxyService {
    private static final String EVENT_SERVER_ERROR = "server-error";
    // Important: do NOT use RestTemplate.exchange(..., String.class) for SSE.
    @Qualifier("sseRestTemplate")
    private final RestTemplate sseRestTemplate;

    // Used to run the downstream streaming copy loop asynchronously.
    private final TaskExecutor sseProxyExecutor;

    /**
     * Proxy an SSE subscription request to a downstream service and stream bytes to the caller.
     */
    public SseEmitter proxySseGet(String downstreamUrl,
                                  HttpHeaders headers) {

        // 0 = no timeout at emitter level (we rely on downstream/ingress settings)
        // You can also set a long timeout here (e.g. minutes) if you want server-side cleanup.
        SseEmitter emitter = new SseEmitter(0L);

        // If client disconnects, stop work (downstream connection will error on write eventually)
        emitter.onCompletion(() -> log.debug("SSE proxy emitter completed. url={}", downstreamUrl));
        emitter.onTimeout(() -> log.debug("SSE proxy emitter timeout. url={}", downstreamUrl));
        emitter.onError(ex -> log.debug("SSE proxy emitter error. url={}, err={}", downstreamUrl, ex.toString()));
        // Run the downstream streaming in background so controller can return immediately.
        sseProxyExecutor.execute(() -> streamDownstreamToEmitter(downstreamUrl, headers, emitter));

        return emitter;
    }

    private void streamDownstreamToEmitter(String downstreamUrl, HttpHeaders headers, SseEmitter emitter) {
        try {
            RequestCallback requestCallback = request -> {
                request.getHeaders().putAll(headers);
                request.getHeaders().remove(HttpHeaders.ORIGIN);
                request.getHeaders().remove(HttpHeaders.HOST);
                request.getHeaders().remove(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
                request.getHeaders().remove(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
                request.getHeaders().set(HttpHeaders.ACCEPT, TEXT_EVENT_STREAM_VALUE);
            };

            ResponseExtractor<Void> extractor = response -> {
                HttpStatusCode status = response.getStatusCode();
                MediaType contentType = response.getHeaders().getContentType();

                log.debug("Downstream SSE status={} contentType={} url={}", status, contentType, downstreamUrl);

                // Some upstreams/proxies omit Content-Type for streaming; allow null.
                boolean looksLikeSse = (contentType == null) || MediaType.TEXT_EVENT_STREAM.includes(contentType);

                // If downstream is not a successful SSE response, do not try to parse as SSE frames.
                if (!status.is2xxSuccessful() || !looksLikeSse) {
                    String bodySnippet = readBodySnippet(response, 16_384);
                    // Emit explicit SSE error event so UI can react deterministically,
                    // then complete the emitter (connection will close).
                    sendServerErrorEvent(emitter, status.value(), contentType, bodySnippet);
                    emitter.complete();
                    return null;
                }

                // Normal SSE streaming path (read frames delimited by blank line)
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {

                    String line;
                    StringBuilder frame = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        frame.append(line).append("\n");

                        if (line.isEmpty()) {
                            reEmitSseFrame(frame.toString(), emitter);
                            frame.setLength(0);
                        }
                    }

                    // flush remaining partial frame if any
                    if (!frame.isEmpty()) {
                        reEmitSseFrame(frame.toString(), emitter);
                    }
                } catch (Exception e) {
                    throw new SSEException(e);
                }

                return null;
            };

            // IMPORTANT: this call will block while downstream stream is open.
            // That is why we run it on sseProxyExecutor.
            sseRestTemplate.execute(downstreamUrl, HttpMethod.GET, requestCallback, extractor);

            // If we exit restTemplate.execute normally, downstream completed.
            emitter.complete();
        } catch (Exception ex) {
            // Most common: client disconnect causes IOException on emitter.send()
            log.warn("SSE proxy failed. url={}, err={}", downstreamUrl, ex.toString());
            try {
                emitter.send(SseEmitter.event()
                        .name(EVENT_SERVER_ERROR)
                        .data("Gateway SSE proxy failure: " + ex.getMessage()));
                emitter.complete();
            } catch (Exception sendEx) {
                emitter.completeWithError(sendEx);
            }
        }
    }

    private void sendServerErrorEvent(SseEmitter emitter, int status, MediaType contentType, String bodySnippet) {
        try {
            String payload = String.format(
                    "{\"status\":%d,\"contentType\":%s,\"body\":%s}",
                    status,
                    contentType == null ? "null" : "\"" + jsonEscape(contentType.toString()) + "\"",
                    bodySnippet == null ? "null" : "\"" + jsonEscape(bodySnippet) + "\""
            );

            emitter.send(SseEmitter.event()
                    .name(EVENT_SERVER_ERROR)
                    .data(payload));
        } catch (IOException e) {
            throw new SSEException(e);
        }
    }

    private static String readBodySnippet(ClientHttpResponse response, int maxChars) {
        try (InputStream is = response.getBody()) {

            byte[] bytes = is.readNBytes(Math.max(0, maxChars));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Failed to read downstream body: " + e.getMessage();
        }
    }

    /** Minimal JSON string escaping (sufficient for log/debug payloads). */
    private static String jsonEscape(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
    /**
     * Parse a downstream SSE frame and re-emit it as proper SseEmitter events so that the browser
     * sees the same event types ("ping" etc.) and doesn't get double-wrapped "data:".
     */
    private void reEmitSseFrame(String rawFrame, SseEmitter emitter) {
        // rawFrame contains lines including trailing blank line. Example:
        // event: ping
        // data: heartbeat
        //
        // or:
        // data: {"statusCodeValue":200,...}
        // etc.
        String eventName = null;
        StringBuilder data = new StringBuilder();

        for (String line : rawFrame.split("\n", -1)) {
            if (line.startsWith("event:")) {
                eventName = line.substring("event:".length()).trim();
            } else if (line.startsWith("data:")) {
                // data can be multiple lines
                String d = line.substring("data:".length()).trim();
                if (!data.isEmpty()) data.append("\n");
                data.append(d);
            }
        }

        String normalizedEvent = (eventName == null || eventName.isBlank()) ? "data" : eventName;

        try {
            switch (normalizedEvent) {
                case "connected", "ping", "data", EVENT_SERVER_ERROR:
                    emitter.send(SseEmitter.event()
                            .name(normalizedEvent)
                            .data(data.toString()));
                    break;
                default:
                    log.debug("Unknown SSE event received: {}", normalizedEvent);
                    emitter.send(SseEmitter.event()
                            .name("data")
                            .data(data.toString()));
            }
        } catch (IOException e) {
            throw new SSEException(e);
        }
    }
}

