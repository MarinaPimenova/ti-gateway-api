package com.wk.ti.security.util;

import com.wk.ti.security.model.UserDetail;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SecurityContextUtil {
    private static final String OKTA_USERNAME_PREFIX = "";
    public static final String OKTA_GROUP_SUFFIX = "";
    // insight_consumer_Merck-RWDEX
    public static final String USER_ROLE = "user";
    // citizen_data_scientist_Merck-RWDEX
    public static final String MODERATOR_ROLE = "moderator";
    // quantitative_data_scientist_Merck-RWDEX
    public static final String ADMIN_ROLE = "admin";


    private SecurityContextUtil() {
    }

    public static UserDetail getUserDetail(OidcUser user) {
        Map<String, Object> attributes = getAttributes(user);

        return getUserDetail(attributes);
    }

    private static Map<String, Object> getAttributes(OidcUser user) {
        Assert.notNull(user, "OidcUser can't be null");
        return user.getAttributes();
    }

    public static List<String> getRoles(OidcUser user) {
        Map<String, Object> attributes = getAttributes(user);
        return getAuthorities((List<String>) attributes.get("cognito:groups"), true);
    }

    public static UserDetail getUserDetail(Map<String, Object> attributes) {
        Assert.notNull(attributes, "attributes can't be null");
        // cognito:username -> Merck-PingFed-SAML_ISID
        String username = ((String) attributes.get("username")).replace(OKTA_USERNAME_PREFIX, "");
        List<String> authorities = getAuthorities((List<String>) attributes.get("cognito:groups"), false);
        return UserDetail.builder()
                .username(username)
                .email((String) attributes.get("email"))
                .givenName((String) attributes.get("given_name"))
                .familyName((String) attributes.get("family_name"))

                .roles(authorities.isEmpty() ? List.of(USER_ROLE) : authorities)
                .registered(!authorities.isEmpty())
                .build();
    }

    public static List<String> getAuthorities(List<String> grantedAuthorities, boolean includeAdminRole) {
        List<String> authorities = getNormalizedGroups(grantedAuthorities);
        if (includeAdminRole) {
            return authorities;
        }
        if (authorities.contains(USER_ROLE)) {
            return List.of(USER_ROLE);
        } else if (authorities.contains(MODERATOR_ROLE)) {
            return List.of(MODERATOR_ROLE);
        } else if (authorities.contains(ADMIN_ROLE)) {
            return List.of(ADMIN_ROLE);
        }
        return Collections.emptyList();
    }

    private static List<String> getNormalizedGroups(List<String> grantedAuthorities) {
        return grantedAuthorities.stream()
                .filter(authority -> authority.contains(OKTA_GROUP_SUFFIX))
                .map(authority -> authority.replace(OKTA_GROUP_SUFFIX, ""))
                .toList();
    }
}

