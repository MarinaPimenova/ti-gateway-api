package com.wk.ti.route.service;

import com.wk.ti.exception.RouteNotSupportedException;
import com.wk.ti.route.config.MsUriConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
@Component
public class DownstreamService {
    private static final String TARGET_DELIMITER = "/";
    private final MsUriConfiguration msUriConfiguration;

    public DownstreamService(MsUriConfiguration msUriConfiguration) {
        this.msUriConfiguration = msUriConfiguration;
    }

    public URI getDownstreamServiceUrl(HttpServletRequest request) throws URISyntaxException {
        String path = request.getRequestURI();
        Map<String, String> serviceNameToUriMap = getServiceNameToUriMap();
        for (Map.Entry<String, String> entry : serviceNameToUriMap.entrySet()) {
            String serviceName = entry.getKey();
            if (path.contains(serviceName)) {
                path = path.replaceFirst(TARGET_DELIMITER + serviceName, "");
                return new URI(serviceNameToUriMap.get(serviceName) + path + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            }
        }
        String message = format("Action: Forward request. Params: path - %s", path);
        log.error(message);
        throw new RouteNotSupportedException(message);
    }

    protected Map<String, String> getServiceNameToUriMap() {
        return msUriConfiguration.getServiceNameToUri();
    }
}

