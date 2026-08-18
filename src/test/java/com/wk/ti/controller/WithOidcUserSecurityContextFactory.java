package com.wk.ti.controller;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WithOidcUserSecurityContextFactory
        implements WithSecurityContextFactory<WithOidcUser> {

    @Override
    public SecurityContext createSecurityContext(WithOidcUser annotation) {

        OidcIdToken idToken = new OidcIdToken(
                "id-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of(
                        "sub", annotation.subject(),
                        "email", annotation.email(),
                        "given_name", annotation.givenName(),
                        "family_name", annotation.familyName()
                )
        );
        List<? extends GrantedAuthority> authorities = Arrays.stream(annotation.authorities())
                .map(SimpleGrantedAuthority::new)
                .toList();

        OidcUser oidcUser = new DefaultOidcUser(
                authorities,
                idToken,
                "sub"
        );

        OAuth2AuthenticationToken authentication =
                new OAuth2AuthenticationToken(
                        oidcUser,
                        oidcUser.getAuthorities(),
                        "okta"
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}