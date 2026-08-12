package com.wk.ti.security.service;

import com.wk.ti.security.model.UserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserDetailExtractorResolver {
    private final List<UserDetailExtractorStrategy> extractors;

    public UserDetail extract(OidcUser user) {
        Map<String, Object> attributes = user.getAttributes();
        return extractors.stream()
                .filter(extractor -> extractor.supports(attributes))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported authentication"))
                .extractUserDetail(attributes);
    }
}
