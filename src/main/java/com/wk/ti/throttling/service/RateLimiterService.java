package com.wk.ti.throttling.service;

import com.wk.ti.throttling.config.RateLimitingProperties;
import io.github.bucket4j.*;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final RateLimitingProperties properties;

    public RateLimiterService(RateLimitingProperties properties) {
        this.properties = properties;
    }

    public Bucket resolveBucket(String key) {

        return buckets.computeIfAbsent(key, k ->

                Bucket.builder()
                        .addLimit(
                                Bandwidth.classic(
                                        properties.getCapacity(),
                                        Refill.greedy(
                                                properties.getRefill(),
                                                properties.getRefillPeriod()
                                        )
                                )
                        )
                        .build()
        );
    }
}
