package io.github.springwhale.platform.rbac.security;

import io.github.springwhale.framework.webmvc.security.SecurityConfigProvider;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * Default security configuration provider for RBAC module
 */
public class RbacSecurityConfigProvider implements SecurityConfigProvider {

    @Override
    public List<String> getPermitAllUrls() {
        // Merge URLs from config file and default URLs
        return List.of(
                "/api/rbac/auth/**",      // Authentication endpoints
                "/api/rbac/public/**"     // Public endpoints
        );
    }

    @Override
    public int getOrder() {
        return 100; // Default priority
    }
}