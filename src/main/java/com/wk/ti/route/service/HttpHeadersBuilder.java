package com.wk.ti.route.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.util.Enumeration;
import java.util.List;

import static java.lang.String.format;

@Slf4j
@Data
public class HttpHeadersBuilder {
    public static final String AUTHORIZATION_HEADER_VALUE = "Bearer %s";

    private HttpHeaders values = new HttpHeaders();

    public HttpHeadersBuilder idToken(OidcIdToken idToken) {
        values.add(HttpHeaders.AUTHORIZATION, format(AUTHORIZATION_HEADER_VALUE, idToken.getTokenValue()));
        return this;
    }

    public HttpHeadersBuilder request(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames == null) {
            return this;
        }
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            String header = request.getHeader(headerName);
            values.add(headerName, header);
        }
        return this;
    }

    public static boolean shouldSkipResponse(HttpHeaders httpHeaders) {
        // check headers: HttpHeaders.CONTENT_DISPOSITION and application/force-download
        return httpHeaders.containsHeader(HttpHeaders.CONTENT_DISPOSITION)
                || httpHeaders.containsHeader("application/force-download");
    }

    public HttpHeadersBuilder acceptEncoding(String value) {

        List<String> existingValues = values.get(HttpHeaders.ACCEPT_ENCODING);

        if (existingValues != null) {
            log.info(
                    "Request contains header: {} with value: {} replaced by value: {}",
                    HttpHeaders.ACCEPT_ENCODING,
                    existingValues,
                    value
            );
        }

        values.set(HttpHeaders.ACCEPT_ENCODING, value);
        return this;
    }

    public HttpHeadersBuilder cacheControl(String value) {
        values.add(HttpHeaders.CACHE_CONTROL, value);  // "no-cache"
        return this;
    }

    public HttpHeaders build() {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(values);

        return headers;
    }
}
