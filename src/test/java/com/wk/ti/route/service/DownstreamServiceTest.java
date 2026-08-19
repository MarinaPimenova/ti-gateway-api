package com.wk.ti.route.service;

import com.wk.ti.exception.RouteNotSupportedException;
import com.wk.ti.route.config.MsUriConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

class DownstreamServiceTest {
    private final MsUriConfiguration configuration = Mockito.mock(MsUriConfiguration.class);
    private final DownstreamService service = new DownstreamService(configuration);

    @BeforeEach
    void setup() {
        Map<String, String> serviceNameToUriMap = new HashMap<>();
        serviceNameToUriMap.put("knowledge", "http://local:8081");
        given(configuration.getServiceNameToUri()).willReturn(serviceNameToUriMap);
    }

    @Test
    void shouldDownstreamServiceUrl() throws URISyntaxException {
        // given
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/knowledge/version");

        // when
        URI result = service.getDownstreamServiceUrl(request);
        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowException() {
        // given
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/unknown/version");

        // when
        RouteNotSupportedException exception =
                assertThrows(
                        RouteNotSupportedException.class,
                        () -> service.getDownstreamServiceUrl(request));

        // then
        assertNotNull(exception);
        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                "Action: Forward request. Params: path - /api/v1/unknown/version"));
    }
}
