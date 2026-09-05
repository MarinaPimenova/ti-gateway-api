package com.wk.ti.controller;

import com.wk.ti.route.service.DownstreamService;
import com.wk.ti.route.service.RestTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

//@RestController
//@RequestMapping("/rest/v1/**")
@Slf4j
public class GatewayExtController {
    private final DownstreamService downstreamService;
    private final RestTemplateService restTemplateService;

    public GatewayExtController(DownstreamService downstreamService, RestTemplateService restTemplateService) {
        this.downstreamService = downstreamService;
        this.restTemplateService = restTemplateService;
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> forwardRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body) throws URISyntaxException {

        // Build the URI for the downstream service
        URI uri = downstreamService.getDownstreamServiceUrl(request);
        // Copy headers from the incoming request

        HttpHeaders headers = restTemplateService.getHttpHeaders(request);

        // Create a new HttpEntity with the copied headers and request body
        HttpEntity<?> entity = restTemplateService.getHttpEntity(Optional.ofNullable(body), headers);

        // Forward the request to the downstream service
        return restTemplateService.exchange(uri, HttpMethod.valueOf(request.getMethod()), entity);
    }
}
