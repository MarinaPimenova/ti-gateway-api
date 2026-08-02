package com.wk.ti.config;

import com.wk.ti.redirection.RedirectionService;
import com.wk.ti.security.model.UserDetail;
import com.wk.ti.security.util.SecurityContextUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static java.lang.String.format;

/**
 * @noinspection RedundantThrows
 */
@Slf4j
@Component
public class CustomizeAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final RedirectionService redirectionService;

    public CustomizeAuthenticationSuccessHandler(
            RedirectionService redirectionService) {
        this.redirectionService = redirectionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        DefaultOidcUser defaultOidcUser = (DefaultOidcUser) authentication.getPrincipal();
        UserDetail userDetail = SecurityContextUtil.getUserDetail(defaultOidcUser);

        log.info(format("Action: handle Success Authentication. User: %s, registered: %s", userDetail.getUsername(),
                userDetail.getRegistered().toString()));
        redirectionService.handleAuthSuccess(request, response, defaultOidcUser, userDetail); // getRequestURL: http://localhost:8080/login/oauth2/code/okta
    }
}


