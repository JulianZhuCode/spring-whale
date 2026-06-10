package io.github.springwhale.framework.thymeleaf.security;

import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Security configuration for the admin console.
 * <p>
 * Permits access to static resources and the login page.
 * Individual module pages are protected by Spring Security's method-level
 * annotations ({@code @PreAuthorize}) or URL rules.
 * </p>
 */
@Component
public class ThymeleafSecurityConfigProvider implements SecurityConfigProvider {

    @Override
    public List<String> getPermitAllUrls() {
        return List.of(
                "/admin/login",
                "/admin/css/**",
                "/admin/js/**",
                "/admin/favicon.ico",
                "/admin/favicon.svg",
                "/favicon.ico",
                "/error"
        );
    }

    @Override
    public int getOrder() {
        return 50; // Before RBAC (100) so static resources are evaluated first
    }
}
