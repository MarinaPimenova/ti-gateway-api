package com.wk.ti.controller;

import com.wk.ti.security.util.SecurityContextUtil;
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

@Slf4j
@RestController
public class UserController {
//    private final RedirectionService redirectionService;
//
//    public UserController(RedirectionService redirectionService) {
//        this.redirectionService = redirectionService;
//    }
//
//    @GetMapping
//    public void defaultRedirect(HttpServletRequest request,
//                                HttpServletResponse response,
//                                @AuthenticationPrincipal OidcUser user) throws IOException {
//        if (user != null) {
//            log.info(format("Action: process request: %s", request.getRequestURL().toString()));
//            UserDetail userDetail = SecurityContextUtil.getUserDetail(user);
//            redirectionService.handleAuthSuccess(request, response, user, userDetail);
//        }
//    }

    @GetMapping("favicon.ico") void returnNoFavicon() {
        // to avoid 404 for favicon
    }

    @GetMapping(value = "/api/v1/token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> token(@AuthenticationPrincipal(expression = "idToken") OidcIdToken idToken) {
        Map<String, String> body = Map.of("token", idToken.getTokenValue());
        return ResponseEntity.ok(new JSONObject(body));
    }

    @GetMapping("/api/v1/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal OidcUser user,
                                     @RequestParam(defaultValue = "landing-url") String redirectId) {
        log.info(format("Authentication is requested redirectId: %s", redirectId));
        return ResponseEntity.ok().body(SecurityContextUtil.getUserDetail(user));
    }

    @GetMapping("/api/v1/user/roles")
    public ResponseEntity<?> getUserRoles(@AuthenticationPrincipal OidcUser user) {
        return ResponseEntity.ok().body(SecurityContextUtil.getRoles(user));
    }
}
