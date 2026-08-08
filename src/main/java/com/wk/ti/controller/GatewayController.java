package com.wk.ti.controller;

import com.wk.ti.route.service.DownstreamProtectUrlService;
import com.wk.ti.route.service.RestTemplateService;
import com.wk.ti.sse.service.SseProxyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.util.Optional;

import static org.springframework.http.MediaType.*;

@SuppressWarnings("SpringElInspection")
@RestController
@RequestMapping({"/api/v1"})
@Slf4j
public class GatewayController {

    private final DownstreamProtectUrlService downstreamProtectUrlService;
    private final RestTemplateService restTemplateService;
    private final SseProxyService sseProxyService;

    public GatewayController(DownstreamProtectUrlService downstreamProtectUrlService,
                             RestTemplateService restTemplateService,
                             SseProxyService sseProxyService) {
        this.downstreamProtectUrlService = downstreamProtectUrlService;
        this.restTemplateService = restTemplateService;
        this.sseProxyService = sseProxyService;
    }

    // -------------------------------------------------------------------------
    // SSE passthrough (MUST be explicit; must not use RestTemplate.exchange(String.class))
    // -------------------------------------------------------------------------

    /**
     * SSE subscription endpoint (streaming).
     * This MUST bypass the generic forwardRequestGet() because SSE responses are long-lived and
     * cannot be buffered into a String. We proxy the downstream SSE stream and re-emit frames.
     */
    @GetMapping(value = "/ai-assistant/sse/subscription/{conversationId}/{questionId}",
            produces = TEXT_EVENT_STREAM_VALUE)
    public SseEmitter proxySseSubscription(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("conversationId") String conversationId,
            @PathVariable("questionId") Long questionId,
            @AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        // Build downstream URL from the incoming request via existing routing logic
        URI downstream = getURI(request);

        // Copy headers (cookies, auth, etc.) using existing logic
        HttpHeaders headers = getHeaders(request, idToken);
        // Instruct nginx/ALB to disable proxy buffering for this SSE response.
        // nginx honors this header and will flush frames to the client immediately
        // without waiting for the full response — no ingress annotation change needed.
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        log.info("Proxying SSE subscription: convId={}, questionId={}, downstream={}",
                conversationId, questionId, downstream);

        // Stream downstream -> client immediately
        return sseProxyService.proxySseGet(downstream.toString(), headers);
    }

    /**
     * Triggers downstream async processing (normal JSON/empty response).
     * This endpoint is not itself a stream; it just triggers the orchestration and returns 200 OK.
     * Safe to forward through the normal exchange().
     */
    @GetMapping(value = "/ai-assistant/sse/question", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> proxySseQuestionTrigger(
            HttpServletRequest request,
            @RequestParam("conversationId") String conversationId,
            @RequestParam("questionId") Long questionId,
            @AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        log.info("Proxying SSE question: convId={}, questionId={}",
                conversationId, questionId);
        return exchange(request, Optional.empty(), idToken);
    }

    @PostMapping(value = "/**/upload", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        return ResponseEntity.ok(restTemplateService.upload(getURI(request), file,
                restTemplateService.getHttpHeaders(idToken)));
    }

    @SneakyThrows
    private URI getURI(HttpServletRequest request) {
        // Build the URI for the downstream service
        return downstreamProtectUrlService.getDownstreamServiceUrl(request);
    }

    private HttpHeaders getHeaders(HttpServletRequest request, OidcIdToken idToken) {
        // Copy headers from the incoming request
        return restTemplateService.getHttpHeaders(request, idToken);
    }

    @RequestMapping(
            value = "/**",
            method = {RequestMethod.DELETE},
            consumes = {APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE, APPLICATION_XML_VALUE,
                    APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<?> forwardRequestDelete(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        return exchange(request, Optional.ofNullable(body), idToken);
    }

    /**
     * Catch-all GET forwarder.
     * NOTE: The explicit SSE mappings above will win for the SSE routes, so this will not be used
     * for /api/v1/ai-assistant/sse/subscription/** or /api/v1/ai-assistant/sse/question
     */
    @GetMapping(value = "/**")
    public ResponseEntity<?> forwardRequestGet(
            HttpServletRequest request,
            @AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        log.info("CATCH_ALL_GET_HIT ... requestURI={}", request.getRequestURI());
        return exchange(request, Optional.empty(), idToken);
    }

    @RequestMapping(
            value = "/**",
            method = {RequestMethod.POST},
            consumes = {APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE, APPLICATION_XML_VALUE,
                    APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<?> forwardRequestPost(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        return exchange(request, Optional.ofNullable(body), idToken);
    }

    @RequestMapping(
            value = "/**",
            method = {RequestMethod.PUT},
            consumes = {APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE, APPLICATION_XML_VALUE,
                    APPLICATION_FORM_URLENCODED_VALUE, APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<?> forwardRequestPut(
            HttpServletRequest request,
            @RequestBody(required = false) String body,
            @AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        return exchange(request, Optional.ofNullable(body), idToken);
    }

    private ResponseEntity<?> exchange(
            HttpServletRequest request,
            Optional<String> body,
            OidcIdToken idToken
    ) {
        // Create a new HttpEntity with the copied headers and request body
        HttpEntity<?> entity = restTemplateService.getHttpEntity(body, getHeaders(request, idToken));

        // Forward the request to the downstream service
        return restTemplateService.exchange(getURI(request), HttpMethod.valueOf(request.getMethod()), entity);
    }
}

