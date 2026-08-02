package com.wk.ti.redirection.config;

import com.drew.lang.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/** @noinspection ConfigurationProperties*/
@Component
@Data
@ConfigurationProperties(prefix = "ms")
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class RedirectionConfiguration {
    @NotNull
    private Map<String, String> clientToUrl;
}
