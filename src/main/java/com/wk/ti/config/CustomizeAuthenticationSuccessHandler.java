package com.wk.ti.config;

import com.wk.ti.redirection.RedirectionService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @noinspection RedundantThrows
 */
@Component
@RequiredArgsConstructor
public class CustomizeAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final RedirectionService redirectionService;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull Authentication authentication) throws IOException, ServletException {
        redirectionService.handleAuthSuccess(request, response);
    }
}


