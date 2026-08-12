package com.wk.ti.security.service;

import com.wk.ti.security.model.UserDetail;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public interface UserDetailExtractorStrategy {
    String USER_ROLE = "user";
    String MODERATOR_ROLE = "moderator";
    String ADMIN_ROLE = "admin";
    String USER_ID = "sub";
    String DEFAULT_EMAIL = "";
    String DEFAULT_GIVEN_NAME = "google";
    String DEFAULT_FAMILY_NAME = "";
    String EMAIL_ATTRIBUTE = "email";
    String GIVEN_NAME_ATTRIBUTE = "given_name";
    String FAMILY_NAME_ATTRIBUTE = "family_name";
    String GROUPS_ATTRIBUTE = "groups";

    /**
     * Determines whether this strategy handles the given provider/attributes.
     */
    boolean supports(Map<String, Object> attributes);

    /**
     * Extracts application-specific UserDetail from raw OIDC attributes.
     */
    UserDetail extractUserDetail(Map<String, Object> attributes);

    default List<String> extractRoles(Map<String, Object> attributes) {
        List<String> authorities = (List<String>) attributes.get(GROUPS_ATTRIBUTE);

        return authorities == null ? List.of() : authorities;
    }

    default String getEmail(Map<String, Object> attributes) {
        return attributes.get(EMAIL_ATTRIBUTE) == null ? DEFAULT_EMAIL : (String) attributes.get(EMAIL_ATTRIBUTE);
    }

    default String getGivenName(Map<String, Object> attributes) {
        return attributes.get(GIVEN_NAME_ATTRIBUTE) == null ? DEFAULT_GIVEN_NAME : (String) attributes.get(GIVEN_NAME_ATTRIBUTE);
    }

    default String getFamilyName(Map<String, Object> attributes) {
        return attributes.get(FAMILY_NAME_ATTRIBUTE) == null ? DEFAULT_FAMILY_NAME : (String) attributes.get(FAMILY_NAME_ATTRIBUTE);
    }

    default String getUserId(Map<String, Object> attributes) {
        return attributes.get(USER_ID) == null ? "" : (String) attributes.get(USER_ID);
    }
}
