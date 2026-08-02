package com.wk.ti.config;

import com.wk.ti.redirection.RedirectionHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final RedirectionHandler redirectionHandler;

    public CustomSimpleUrlAuthenticationFailureHandler(RedirectionHandler redirectionHandler) {
        this.redirectionHandler = redirectionHandler;
    }

    @Override
    public void setDefaultFailureUrl(String defaultFailureUrl) {
        log.info("Default FailureUrl {}", defaultFailureUrl);
        super.setDefaultFailureUrl(defaultFailureUrl);
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        log.info("AuthenticationException: {}", exception.getMessage());
        setDefaultFailureUrl(redirectionHandler.getFailureUrl());
        super.onAuthenticationFailure(request, response, exception);
    }
}
