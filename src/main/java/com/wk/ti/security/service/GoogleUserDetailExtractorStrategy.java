package com.wk.ti.security.service;

import com.wk.ti.security.model.UserDetail;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GoogleUserDetailExtractorStrategy implements UserDetailExtractorStrategy {

    @Override
    public boolean supports(Map<String, Object> attributes) {
        return ((String)attributes.get(USER_ID)).contains("google");
    }

    @Override
    public UserDetail extractUserDetail(Map<String, Object> attributes) {
        List<String> authorities = extractRoles(attributes);
        return UserDetail.builder()
                .username(getUserId(attributes))
                .email(getEmail(attributes))
                .givenName(getGivenName(attributes))
                .familyName(getFamilyName(attributes))
                .roles(authorities.isEmpty() ? List.of(USER_ROLE) : authorities)
                .registered(false)
                .build();
    }

}
