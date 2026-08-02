package com.wk.ti.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class GeneralAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final RedisTemplate<String, OAuth2AuthorizationRequest> redisTemplate;
    private final long timeout;

    public GeneralAuthorizationRequestRepository(
            RedisTemplate redisTemplate,
            @Value("${app.authorization-request-timeout}") long timeout) {
        this.redisTemplate = redisTemplate;
        this.timeout = timeout;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        return redisTemplate.opsForValue().get(state);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        String state = authorizationRequest.getState();
        // it can set a timeout to a key-value pair, so as soon as the time expires,
        // the pair is deleted from the database.
        redisTemplate.opsForValue().set(state, authorizationRequest,
                Duration.ofMinutes(timeout));
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        OAuth2AuthorizationRequest authorizationRequest = redisTemplate.opsForValue().get(state);
        redisTemplate.delete(state);
        return authorizationRequest;
    }
}
