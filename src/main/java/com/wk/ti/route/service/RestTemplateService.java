package com.wk.ti.route.service;

import com.wk.ti.exception.IntegrationException;
import com.wk.ti.exception.RouteNotSupportedException;
import com.wk.ti.exception.model.ClientErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.converter.*;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static com.wk.ti.route.service.HttpHeadersBuilder.shouldSkipResponse;
import static com.wk.ti.util.FileUtil.toTempFileWithUtf8IfText;
import static java.lang.String.format;

/**
 * @noinspection unchecked, rawtypes
 */
@Slf4j
@Service
public class RestTemplateService {
    private final ObjectMapper mapper;
    private final RestTemplate restTemplate;

    public RestTemplateService() {
        this.mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();

        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<?> exchange(URI uri, HttpMethod httpMethod, HttpEntity<?> entity) {
        try {
            // Forward the request to the downstream service
            ResponseEntity<byte[]> response =
                    restTemplate.exchange(uri, httpMethod, entity, byte[].class);

            if (isDownload(response)) {
                return processBinaryResponse(response, uri);
            }

            Charset charset = response.getHeaders().getContentType() != null
                    && response.getHeaders().getContentType().getCharset() != null
                    ? response.getHeaders().getContentType().getCharset()
                    : StandardCharsets.UTF_8;
            String body = response.getBody() == null
                    ? null
                    : new String(response.getBody(), charset);

            return processJsonResponse(
                    new ResponseEntity<>(
                            body,
                            response.getHeaders(),
                            response.getStatusCode()),
                    uri);
        } catch (Throwable ex) {
            String message = format("Action: Forward request %s failed. Caused by: %s",
                    uri.toString(), ex.getMessage());
            log.error(message);
            if (ex.getMessage().contains("401")) {
                throw new CredentialsExpiredException(message);
            }
            throw new RouteNotSupportedException(message);
        }
    }

    private boolean isDownload(ResponseEntity<byte[]> response) {
        return shouldSkipResponse(response.getHeaders());
    }

    private ResponseEntity<?> processJsonResponse(
            ResponseEntity<String> responseEntity,
            URI uri)
            throws IOException {

        HttpStatusCode status = responseEntity.getStatusCode();

        if (status.is2xxSuccessful()) {
            return buildJsonSuccessResponse(responseEntity);
        }

        validateAuthorization(status, uri);

        return buildErrorResponse(
                responseEntity.getBody(),
                status,
                uri);
    }

    private ResponseEntity<?> buildJsonSuccessResponse(
            ResponseEntity<String> responseEntity) {

        String responseValue = responseEntity.getBody();

        if (responseValue == null) {
            return new ResponseEntity<>(responseEntity.getStatusCode());
        }

        if (shouldSkipResponse(responseEntity.getHeaders())) {
            return responseEntity;
        }

        try {
            JsonNode actualObj = mapper.readTree(responseValue);
            return ResponseEntity.ok(actualObj);
        } catch (JacksonException ex) {
            return ResponseEntity.ok(responseValue);
        }
    }

    private ResponseEntity<ClientErrorResponse> buildErrorResponse(
            String responseValue,
            HttpStatusCode status,
            URI uri) throws MalformedURLException {

        ClientErrorResponse clientErrorResponse;

        if (responseValue != null) {
            clientErrorResponse =
                    mapper.readValue(responseValue, ClientErrorResponse.class);
        } else {
            clientErrorResponse = defaultErrorResponse(status, uri);
        }

        return new ResponseEntity<>(clientErrorResponse, status);
    }

    private ResponseEntity<ClientErrorResponse> buildErrorResponse(
            byte[] responseValue,
            HttpStatusCode status,
            URI uri) throws MalformedURLException {

        ClientErrorResponse clientErrorResponse;

        if (responseValue != null) {
            clientErrorResponse =
                    mapper.readValue(responseValue, ClientErrorResponse.class);
        } else {
            clientErrorResponse = defaultErrorResponse(status, uri);
        }

        return new ResponseEntity<>(clientErrorResponse, status);
    }

    private ClientErrorResponse defaultErrorResponse(
            HttpStatusCode status,
            URI uri) throws MalformedURLException {

        return ClientErrorResponse.builder()
                .status(status.value())
                .errorMessage(
                        "Response: Error message is not provided. URL: "
                                + uri.toURL())
                .build();
    }

    private ResponseEntity<?> processBinaryResponse(
            ResponseEntity<byte[]> responseEntity,
            URI uri)
            throws IOException {

        HttpStatusCode status = responseEntity.getStatusCode();

        if (status.is2xxSuccessful()) {
            return buildBinarySuccessResponse(responseEntity);
        }

        validateAuthorization(status, uri);

        return buildErrorResponse(
                responseEntity.getBody(),
                status,
                uri);
    }

    private ResponseEntity<byte[]> buildBinarySuccessResponse(
            ResponseEntity<byte[]> responseEntity) {

        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    private void validateAuthorization(HttpStatusCode httpStatus, URI uri) throws MalformedURLException {
        if (httpStatus == HttpStatus.FORBIDDEN
                || httpStatus == HttpStatus.UNAUTHORIZED) {

            String message = format(
                    "Action: process response from url: %s before redirection to FE",
                    uri.toURL());

            log.error(message);
            throw new CredentialsExpiredException(message);
        }
    }

    public <T, V> ResponseEntity<T> executeRequest(
            String url,
            HttpHeaders headers,
            Optional<V> body,
            HttpMethod httpMethod,
            Class<T> targetType) {
        HttpEntity httpEntity = initHttpEntity(body, headers);
        try {
            return restTemplate.exchange(url, httpMethod, httpEntity, targetType);
        } catch (Throwable ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }

    public HttpHeaders getHttpHeaders(OidcIdToken idToken) {
        return new HttpHeadersBuilder()
                .idToken(idToken)
                .build();
    }

    public HttpHeaders getHttpHeaders(HttpServletRequest request, OidcIdToken idToken) {
        return new HttpHeadersBuilder()
                .idToken(idToken)
                .request(request)
                .acceptEncoding("identity")
                .build();
    }

    public HttpHeaders getHttpHeaders(HttpServletRequest request) {
        return new HttpHeadersBuilder()
                .request(request)
                .acceptEncoding("identity")
                .build();
    }

    public <V> HttpEntity<V> initHttpEntity(Optional<V> body, HttpHeaders headers) {
        Objects.requireNonNull(headers, "headers can't be null");

        if (headers.getAccept().isEmpty() && body.isPresent()) {
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        }

        // Handle form URL encoded requests
        // Example:
        //
        //Content-Type: application/x-www-form-urlencoded
        //with body:
        //username=test&password=123
        //This must preserve the body.
        if (!StringUtils.isBlank(headers.getFirst("Content-Type"))
                && MediaType.APPLICATION_FORM_URLENCODED_VALUE
                .equalsIgnoreCase(headers.getFirst("Content-Type"))
                && body.isPresent()) {
            HttpEntity<MultiValueMap<String, Object>> httpEntity =
                    new HttpEntity(body.get(), headers);
            return (HttpEntity<V>) httpEntity;
        }

        return body.map(v -> new HttpEntity(v, headers)).orElseGet(() -> new HttpEntity(headers));
    }

    public HttpEntity<?> getHttpEntity(Optional<String> body, HttpHeaders headers) {
        return initHttpEntity(body, headers);
    }

    @SneakyThrows
    public ResponseEntity<?> upload(URI uri, MultipartFile file, HttpHeaders headers) {
        Assert.notNull(file, "Upload file cannot be null");

        File tempFile = toTempFileWithUtf8IfText(file);
        FileSystemResource resource = new FileSystemResource(tempFile);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            return exchange(uri, HttpMethod.POST, requestEntity);
        } finally {
            try {
                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException e) {
                log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath(), e);
            }
        }
    }

}

