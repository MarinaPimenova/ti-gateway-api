package com.wk.ti.controller;

import com.wk.ti.redirection.model.RedirectionAttribute;
import com.wk.ti.route.config.MsUriConfiguration;
import com.wk.ti.route.service.DownstreamService;
import com.wk.ti.route.service.RestTemplateService;

import com.wk.ti.throttling.config.RateLimitingProperties;
import com.wk.ti.throttling.service.RateLimiterService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.wk.ti.util.TestUtils.sessionOidc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GatewayExtController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@Import({
        DownstreamService.class,
        MsUriConfiguration.class
})
@ActiveProfiles("test")
class GatewayExtControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    MeterRegistry meterRegistry;
    @MockitoBean
    RedirectionAttribute redirectionAttribute;
    @MockitoBean
    RateLimiterService rateLimiterService;
    @MockitoBean
    RateLimitingProperties rateLimitingProperties;

    @MockitoBean
    RestTemplateService restTemplateService;

    @Test
    void shouldExchangeRequest() throws Exception {
        // given
        ResponseEntity<?> responseEntity = ResponseEntity.ok("OK");
        doReturn(responseEntity)
                .when(restTemplateService)
                .exchange(any(), any(), any());
        // when
        MvcResult mvcResult = mockMvc
                .perform(get("/rest/v1/knowledge/version").with(sessionOidc()))
                .andExpect(status().isOk())
                .andReturn();
        // then
        assertThat(mvcResult).isNotNull();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertNotNull(response);

        verify(restTemplateService)
                .exchange(any(), any(), any());
    }
}
