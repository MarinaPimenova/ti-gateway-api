package com.wk.ti.observability;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static java.lang.String.format;

@Component
public class MdcFilter extends OncePerRequestFilter {
    private static final String HEADER_NAME = "X-Request-Id";
    private static final String REQUEST_HEADER_TEMPLATE = "ti-%s";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Extract from headers or security context
            String requestId = request.getHeader(HEADER_NAME);
            if (requestId == null) {
                requestId = format(REQUEST_HEADER_TEMPLATE,
                        UUID.randomUUID());
                response.setHeader(HEADER_NAME, requestId);

            }
            // Add it to the outbound response header for client-side tracking
            MDC.put("requestId", requestId);

            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Always clear the thread local map to prevent memory leaks
            MDC.remove("requestId");
        }
    }
}
