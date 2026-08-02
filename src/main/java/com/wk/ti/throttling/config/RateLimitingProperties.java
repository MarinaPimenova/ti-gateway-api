package com.wk.ti.throttling.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "rate.limiting")
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class RateLimitingProperties {
    boolean enabled;
    long capacity;
    long refill;
    Duration refillPeriod;
}

