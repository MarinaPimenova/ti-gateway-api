package com.wk.ti.redirection;

import com.wk.ti.security.model.UserDetail;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Component
public class RedirectionService {

    private final RedirectionHandler redirectionHandler;
    private final String applicationUrl;

    public RedirectionService(
            RedirectionHandler redirectionHandler,
            @Value("${app.application-url}") String applicationUrl) {
        this.redirectionHandler = redirectionHandler;
        this.applicationUrl = applicationUrl;
    }

    public void handleAuthSuccess(HttpServletRequest request,
                                  HttpServletResponse response,
                                  OidcUser oidcUser, UserDetail userDetail) throws IOException {
        log.info("Action: authentication. Parameters: user: {}, role: {}",
                userDetail.getUsername(),
                userDetail.getRoles().stream()
                        .collect(Collectors.joining(", ", "{", "}")));

        String savedRequestURI;
        if (request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST") != null) {
            savedRequestURI = ((SimpleSavedRequest) request.getSession()
                    .getAttribute("SPRING_SECURITY_SAVED_REQUEST")).getRedirectUrl();
            log.info(format("SPRING_SECURITY_SAVED_REQUEST: %s", savedRequestURI));
        }

        setJSessionIdCookie(response);
        String redirectionUrl = handleAuthenticatedRootRedirection(
                request.getSession().getId(), oidcUser, userDetail);
        response.sendRedirect(redirectionUrl);
    }

    private String handleAuthenticatedRootRedirection(String sessionId, OidcUser oidcUser, UserDetail userDetail) {
        if (userDetail.getRegistered()) {
            log.info("Action: redirection according with session id: {}", sessionId);
            return redirectionHandler.handleLoginRedirection(sessionId);
        }
        // TODO: handle onboard redirection
        return redirectionHandler.handleLoginRedirection(sessionId);
    }

    public void setJSessionIdCookie(HttpServletResponse httpServletResponse) {
        Collection<String> headerNames = httpServletResponse.getHeaderNames();
        boolean isJSessionId = false;
        for (String headerName : headerNames) {
            if (headerName.equals("Set-Cookie")) {
                log.info("Response header name={}, header value={}", headerName, httpServletResponse.getHeader(headerName));
                isJSessionId = httpServletResponse.getHeader(headerName).contains("JSESSIONID");
            }
        }
        if (!isJSessionId) {
            // get JSESSIONID
            String jsessionID = RequestContextHolder.currentRequestAttributes().getSessionId();
            Cookie jsessionIdCookie = new Cookie("JSESSIONID", jsessionID);
            httpServletResponse.addCookie(jsessionIdCookie);
        }

        Cookie originalCookie = new Cookie("ORIGINAL", applicationUrl);
        originalCookie.setPath("/");
        httpServletResponse.addCookie(originalCookie);
    }

}

