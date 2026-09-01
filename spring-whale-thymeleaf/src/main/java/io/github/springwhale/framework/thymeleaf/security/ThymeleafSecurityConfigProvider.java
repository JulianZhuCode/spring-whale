package io.github.springwhale.framework.thymeleaf.security;

import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.List;

/**
 * Security configuration for the admin console.
 * <p>
 * Permits access to static resources and the login page.
 * Individual module pages are protected by Spring Security's method-level
 * annotations ({@code @PreAuthorize}) or URL rules.
 * </p>
 * <p>
 * Also registers the custom {@link AuthenticationEntryPoint} that redirects
 * unauthenticated admin page requests to the login page with diagnostic info.
 * </p>
 */
public class ThymeleafSecurityConfigProvider implements SecurityConfigProvider {

    private final AuthenticationEntryPoint adminConsoleEntryPoint;

    public ThymeleafSecurityConfigProvider(AuthenticationEntryPoint adminConsoleEntryPoint) {
        this.adminConsoleEntryPoint = adminConsoleEntryPoint;
    }

    @Override
    public List<String> getPermitAllUrls() {
        return List.of(
                "/admin/login",
                "/admin/css/**",
                "/admin/js/**",
                "/admin/favicon.ico",
                "/admin/favicon.svg",
                "/favicon.ico",
                "/webjars/**",
                "/error"
        );
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(adminConsoleEntryPoint));
    }

    @Override
    public int getOrder() {
        return 50; // Before RBAC (100) so static resources are evaluated first
    }
}