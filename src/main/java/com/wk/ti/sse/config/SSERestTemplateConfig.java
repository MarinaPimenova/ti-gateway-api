package com.wk.ti.sse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SSERestTemplateConfig {

    @Bean
    public RestTemplate sseRestTemplate() {
        return new RestTemplate();
    }
}
