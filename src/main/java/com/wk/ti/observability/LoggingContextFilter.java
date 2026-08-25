package com.wk.ti.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

import static com.wk.ti.observability.MetricsContract.*;

@SuppressWarnings("FieldCanBeLocal")
@Component
@Slf4j
public class LoggingContextFilter implements Filter {
    // Set of headers to sanitize/redact
    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization",
            "cookie",
            "set-cookie",
            "proxy-authorization",
            "x-api-key"
    );
    private final Counter failedRequestsCounter;
    private final Timer durationRequestTimer;

    public LoggingContextFilter(
            MeterRegistry meterRegistry) {
        this.failedRequestsCounter = meterRegistry.counter(METRIC_FAILED_REQUESTS_COUNT);
        this.durationRequestTimer = meterRegistry.timer(METRIC_REQUEST_DURATION);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            long start = System.currentTimeMillis();
            logRequest(httpRequest);
            chain.doFilter(request, response);

            logResponse(httpRequest, httpResponse, start);

        } finally {
            log.debug("Logging completes: request: {}:{}",
                    request.getRemoteHost(),
                    request.getRemotePort()
            );
        }
    }

    private void logRequest(HttpServletRequest request) {
        log.info("Incoming Request: [{}] {}", request.getMethod(), request.getRequestURI());
        request.getHeaderNames().asIterator().forEachRemaining(header -> {
            String value = isSensitiveHeader(header)
                    ? "[REDACTED]"
                    : request.getHeader(header);
            log.info("Header: {} = {}", header, value);
        });
    }

    private boolean isSensitiveHeader(String header) {
        return header != null && SENSITIVE_HEADERS.contains(header.toLowerCase());
    }

    private void logResponse(
            HttpServletRequest request,
            HttpServletResponse httpResponse,
            long start) {
        long requestDuration = (System.currentTimeMillis() - start);
        log.info("Outgoing Response for [{}] {}: Status = {}, Total elapsed time: {} ms",
                request.getMethod(),
                request.getRequestURI(),
                httpResponse.getStatus(),
                requestDuration);
        //durationRequestTimer.record(requestDuration, TimeUnit.MILLISECONDS); // for particular request
        // metric based on response's status
        if (httpResponse.getStatus() >= 400) {
            failedRequestsCounter.increment();
        }
    }
}

