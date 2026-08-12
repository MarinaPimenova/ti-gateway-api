package com.wk.ti.redirection;

import com.wk.ti.redirection.model.RedirectionAttribute;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RedirectCaptureChainFilter extends OncePerRequestFilter {
    public static final String LOGOUT_SESSION_ID = "logout";
    public static final String CLIENT_ID_ATTR_NAME = "redirectId";
    private final RedirectionAttribute redirectionAttribute;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException, ServletException {
        String redirect = request.getParameter(CLIENT_ID_ATTR_NAME);
        HttpSession httpSession = request.getSession();
        if (httpSession != null) {
            if (redirect != null) {
                redirectionAttribute.setRedirectId(httpSession.getId(), redirect);
            }
            if (request.getRequestURI().contains("/logout")
                    && redirectionAttribute.getRedirectId(httpSession.getId()) != null) {
                redirectionAttribute.setRedirectId(LOGOUT_SESSION_ID, httpSession.getId());
            }
        }
        filterChain.doFilter(request, response);
    }
}

