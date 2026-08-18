package com.wk.ti.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static com.wk.ti.security.service.UserDetailExtractorStrategy.USER_ROLE;

/**
 * @noinspection ConstantConditions, unused
 */
public class TestUtils {
    private static final ObjectMapper mapper =
            new ObjectMapper().registerModule(new JavaTimeModule());
    private static final MediaType contentType =
            new MediaType(
                    MediaType.APPLICATION_JSON.getType(),
                    MediaType.APPLICATION_JSON.getSubtype(),
                    StandardCharsets.UTF_8);

    public static MediaType getJsonUtf8ContentType() {
        return contentType;
    }

    public static <T> T byte2Object(MockHttpServletResponse response, Class<T> targetType)
            throws IOException {
        MockHttpInputMessage mockHttpInputMessage =
                new MockHttpInputMessage(response.getContentAsByteArray());
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        return mapper.readValue(mockHttpInputMessage.getBody(), targetType);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getFileFromResourceAsStream(String fileName) {
        // The class loader that loaded the class
        InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }
    }

    public static <T> T jsonToObject(InputStream inputStream, Class<T> targetType) {
        try {
            return mapper.readValue(inputStream, targetType);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> T byte2Objects(byte[] data, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(data, typeReference);

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static SecurityContext createTestSecurityContext() {
        Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(USER_ROLE);
                return List.of(authority);
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return getDefaultOidcUser();
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return null;
            }
        };
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }

    public static OidcUser getDefaultOidcUser() {
        return new DefaultOidcUser(
                AuthorityUtils.createAuthorityList(USER_ROLE),
                OidcIdToken.withTokenValue("id-token")
                        .claim("nickname", "test")
                        .claim("given_name", "given_name")
                        .claim("family_name", "family_name")
                        .claim("email", "email")

                        .build(),
                "given_name");
    }

    public static RequestPostProcessor sessionOidc() {
        return request -> {
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    TestUtils.createTestSecurityContext()
            );
            return request;
        };
    }
}
