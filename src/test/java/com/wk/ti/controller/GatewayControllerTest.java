package com.wk.ti.controller;

import com.wk.ti.redirection.model.RedirectionAttribute;
import com.wk.ti.route.service.DownstreamProtectUrlService;
import com.wk.ti.route.service.RestTemplateService;
import com.wk.ti.sse.service.SseProxyService;
import com.wk.ti.throttling.config.RateLimitingProperties;
import com.wk.ti.throttling.service.RateLimiterService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GatewayController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@ActiveProfiles("test")
class GatewayControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MeterRegistry meterRegistry;
    @MockitoBean
    RedirectionAttribute redirectionAttribute;
    @MockitoBean
    RateLimiterService rateLimiterService;

    @MockitoBean
    RestTemplateService restTemplateService;
    @MockitoBean
    RateLimitingProperties rateLimitingProperties;

    @MockitoBean
    DownstreamProtectUrlService downstreamProtectUrlService;

    @MockitoBean
    SseProxyService sseProxyService;

    @Test
    @WithOidcUser
    void shouldExchangeRequest() throws Exception {

        when(downstreamProtectUrlService.getDownstreamServiceUrl(any()))
                .thenReturn(URI.create("http://localhost:8081/api/v1/version"));

        when(restTemplateService.getHttpHeaders(any(), any()))
                .thenReturn(new HttpHeaders());

        when(restTemplateService.getHttpEntity(any(), any()))
                .thenReturn(new HttpEntity<>(null, new HttpHeaders()));

        ResponseEntity<?> response = ResponseEntity.ok("OK");

        doReturn(response)
                .when(restTemplateService)
                .exchange(any(), any(), any());

        mockMvc.perform(get("/api/v1/knowledge/version"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        verify(restTemplateService).exchange(any(), any(), any());
    }
}