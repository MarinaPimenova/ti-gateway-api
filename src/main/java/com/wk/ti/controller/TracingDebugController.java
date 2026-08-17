package com.wk.ti.controller;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TracingDebugController {

    private final Tracer tracer;

    @GetMapping("/debug/tracing")
    public Map<String, Object> tracing() {
        Span span = tracer.nextSpan().name("debug-span").start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            return Map.of(
                    "traceId", span.context().traceId(),
                    "spanId", span.context().spanId()
            );
        } finally {
            span.end();
        }
    }
}
