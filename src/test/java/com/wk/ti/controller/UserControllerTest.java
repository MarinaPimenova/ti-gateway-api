package com.wk.ti.controller;

import com.wk.ti.redirection.model.RedirectionAttribute;
import com.wk.ti.security.model.UserDetail;
import com.wk.ti.security.service.GoogleUserDetailExtractorStrategy;
import com.wk.ti.security.service.OktaUserDetailExtractorStrategy;
import com.wk.ti.security.service.UserDetailExtractorResolver;
import com.wk.ti.throttling.config.RateLimitingProperties;
import com.wk.ti.throttling.service.RateLimiterService;
import com.wk.ti.util.TestUtils;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.wk.ti.util.TestUtils.sessionOidc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@Import({
        UserDetailExtractorResolver.class,
        GoogleUserDetailExtractorStrategy.class
})
@ActiveProfiles("test")
class UserControllerTest {
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
    OktaUserDetailExtractorStrategy oktaUserDetailExtractorStrategy;

    @Test
    @WithOidcUser
    void shouldReturnToken() throws Exception {
        // when then
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/token")
                        .with(sessionOidc())
                ).andExpect(status().isOk())
                .andReturn();
        // then
        assertThat(mvcResult).isNotNull();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response).isNotNull();
    }

    @Test
    @WithOidcUser
    void shouldReturnUserProfile() throws Exception {
        // when then
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/user")
                .with(sessionOidc())
        ).andReturn();
        // then
        assertThat(mvcResult).isNotNull();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response).isNotNull();
        UserDetail result = TestUtils.byte2Object(response, UserDetail.class);
        assertThat(result).isNotNull();
    }
}
