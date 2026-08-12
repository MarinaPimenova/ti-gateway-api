package com.wk.ti.controller;

import com.wk.ti.security.model.UserDetail;
import com.wk.ti.security.service.UserDetailExtractorResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static java.lang.String.format;

@SuppressWarnings("SpringElInspection")
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserDetailExtractorResolver userDetailExtractorResolver;

    @GetMapping("favicon.ico")
    void returnNoFavicon() {
        // to avoid 404 for favicon
    }

    @GetMapping(value = "/api/v1/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> token(@AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        Map<String, String> body = Map.of("token", idToken.getTokenValue());
        return ResponseEntity.ok(new JSONObject(body));
    }

    @GetMapping("/api/v1/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal OidcUser user,
                                     @RequestParam(defaultValue = "knowledge-url") String redirectId) {
        log.info(format("Authentication is requested redirectId: %s", redirectId));
        UserDetail userDetail = userDetailExtractorResolver.extract(user);
        return ResponseEntity.ok().body(userDetail);
    }

}
