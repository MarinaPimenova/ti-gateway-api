package com.wk.ti.throttling;

import com.wk.ti.throttling.config.RateLimitingProperties;
import com.wk.ti.throttling.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final RateLimitingProperties properties;

    public RateLimitingFilter(
            RateLimiterService rateLimiterService,
            RateLimitingProperties properties) {

        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String key;

        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

            key = authentication.getName();
        }
        else {

            key = request.getRemoteAddr();
        }

        Bucket bucket = rateLimiterService.resolveBucket(key);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            response.setHeader(
                    "X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));

            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);

        response.setHeader(
                "Retry-After",
                String.valueOf(
                        TimeUnit.NANOSECONDS.toSeconds(
                                probe.getNanosToWaitForRefill())
                ));

        response.getWriter().write("Rate limit exceeded");
    }
}
