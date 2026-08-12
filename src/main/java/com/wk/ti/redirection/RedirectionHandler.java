package com.wk.ti.redirection;

import com.wk.ti.redirection.config.RedirectionConfiguration;
import com.wk.ti.redirection.model.RedirectionAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.wk.ti.redirection.RedirectCaptureChainFilter.LOGOUT_SESSION_ID;
import static com.wk.ti.redirection.model.RedirectionAttribute.DEFAULT_REDIRECTION;
import static java.lang.String.format;

@Component
@Slf4j
public class RedirectionHandler {

    private final RedirectionAttribute redirectionAttribute;
    private final RedirectionConfiguration redirectionConfiguration;
    private final String logoutRedirectUrl;

    public RedirectionHandler(
            RedirectionAttribute redirectionAttribute,
            RedirectionConfiguration redirectionConfiguration,
            @Value("${app.logout-url}") String logoutRedirectUrl) {
        this.redirectionAttribute = redirectionAttribute;
        this.redirectionConfiguration = redirectionConfiguration;
        this.logoutRedirectUrl = logoutRedirectUrl;
    }

    public String handleRedirection(String sessionId) {
        // Get redirect_uri from session (set by filter or parameter)
        String redirect = redirectionAttribute.getRedirectId(sessionId);
        String redirectId = redirect == null ? DEFAULT_REDIRECTION : redirect;
        Map<String, String> clientUrlMap = redirectionConfiguration.getClientToUrl();
        for (Map.Entry<String, String> entry : clientUrlMap.entrySet()) {
            String clientKey = entry.getKey();
            if (redirectId.contains(clientKey)) {
                return entry.getValue();
            }
        }
        return redirectionConfiguration.getClientToUrl().get(DEFAULT_REDIRECTION);
    }

    public String handleLogoutRedirection(String sessionId) {
        String redirect = handleRedirection(sessionId);
        log.info(format("Action: Logout redirection with attribute redirectId: %s", redirect));
        redirectionAttribute.remove(sessionId);
        return redirect;
    }

    public String getFailureUrl() {
        String sessionId = redirectionAttribute.getRedirectId(LOGOUT_SESSION_ID);
        redirectionAttribute.remove(LOGOUT_SESSION_ID);

        return handleLogoutRedirection(sessionId) + logoutRedirectUrl;
    }
}


