package com.wk.ti.route.config;

import com.drew.lang.annotations.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@SuppressWarnings("ConfigurationProperties")
@Component
@Data
@ConfigurationProperties(prefix = "ms")
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class MsUriConfiguration {
    @NotNull
    private Map<String, String> serviceNameToUri;
}
