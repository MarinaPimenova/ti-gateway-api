package com.wk.ti.config;

import com.wk.ti.redirection.RedirectionHandler;
import com.wk.ti.throttling.RateLimitingFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@Slf4j
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600)
public class SecurityConfig {
    private static final String LOGOUT_URL = "/logout";
    private final String issuer;
    private final String allowedOrigins;
    private final String clientId;

    private final CustomizeAuthenticationSuccessHandler customizeAuthenticationSuccessHandler;
    private final GeneralAuthorizationRequestRepository generalAuthorizationRequestRepository;

    private final RedirectionHandler redirectionHandler;
    private final RateLimitingFilter rateLimitingFilter;

    public SecurityConfig(
            @Value("${spring.security.oauth2.client.provider.okta.issuer-uri}") String issuer,
            @Value("${spring.security.oauth2.client.registration.okta.client-id}") String clientId,
            @Value("${ms.cors.allowed-origins}") String allowedOrigins,

            CustomizeAuthenticationSuccessHandler customizeAuthenticationSuccessHandler,
            GeneralAuthorizationRequestRepository generalAuthorizationRequestRepository,
            RedirectionHandler redirectionHandler,
            RateLimitingFilter rateLimitingFilter) {
        this.issuer = issuer;
        this.clientId = clientId;
        this.allowedOrigins = allowedOrigins;

        this.customizeAuthenticationSuccessHandler = customizeAuthenticationSuccessHandler;
        this.generalAuthorizationRequestRepository = generalAuthorizationRequestRepository;

        this.redirectionHandler = redirectionHandler;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .invalidSessionStrategy(new SimpleRedirectInvalidSessionStrategy(LOGOUT_URL))
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().none()
        );

        http.exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), // Return 401 for APIs
                        PathPatternRequestMatcher.withDefaults().matcher("/api/**")
                )
        );
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/v3/**",
                                //"/rest/v1/**/v3/api-docs",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                LOGOUT_URL,
                                "/version",
                                "/oauth2/**",
                                "/rest/v1/**",
                                "/actuator/**",
                                "/index.html",
                                "/static/**",
                                "/fonts/**",
                                "/styles/**",
                                "/icons/**",
                                "/error/**",
                                "/*.ico",
                                "/*.json",
                                "/*.png",
                                "/images/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(
                        oauth -> oauth
                                .authorizationEndpoint(authorizationEndpointConfig -> authorizationEndpointConfig
                                        .authorizationRequestRepository(generalAuthorizationRequestRepository)
                                )
                                .userInfoEndpoint(
                                        userInfoEndpointConfig -> userInfoEndpointConfig.userAuthoritiesMapper(userAuthoritiesMapper()))
                                .successHandler(customizeAuthenticationSuccessHandler)
                                .failureHandler(authenticationFailureHandler())
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "ORIGINAL", "SESSION")
                        .addLogoutHandler(logoutHandler()))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http
                .addFilterAfter(
                        rateLimitingFilter,
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public static ServletListenerRegistrationBean httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomSimpleUrlAuthenticationFailureHandler(redirectionHandler);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public RequestCache refererRequestCache() {
        return new HttpSessionRequestCache() {
            @Override
            public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
                String referrer = request.getHeader("referer");
                if (referrer == null) {
                    referrer = request.getRequestURL().toString();
                }
                request.getSession().setAttribute("SPRING_SECURITY_SAVED_REQUEST",
                        new SimpleSavedRequest(referrer));
            }
        };
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            try {
                mappedAuthorities.add(new SimpleGrantedAuthority("admin"));
            } catch (Exception ex) {
                log.error("Action: authentication. Result: Not Authorized! {} ",
                        ex.getMessage());
            }
            return mappedAuthorities;
        };
    }

    @Bean
    public Filter sessionLoggerFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                HttpSession session = request.getSession(false);
                String uri = request.getRequestURI();
                if (session != null) {
                    log.info("Session ID: {}", session.getId());
                    Collections.list(session.getAttributeNames())
                            .forEach(name -> {
                                if (name.contains("cognito:username")
                                        || name.contains("userId")
                                        || name.contains("SPRING_SECURITY_SAVED_REQUEST")) {
                                    log.info(" - " + name + ": " + session.getAttribute(name));
                                }
                            });
                } else {
                    log.warn("🚫 No session found for [{}]", uri);
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    private LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            try {
                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                response.sendRedirect(issuer + "v2/logout?client_id=" + clientId + "&returnTo=" + baseUrl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
