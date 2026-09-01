package io.github.springwhale.platform.rbac.ui;

import io.github.springwhale.platform.rbac.ui.menu.RbacMenuProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Spring Whale Platform RBAC UI module.
 * <p>
 * All beans are explicitly registered via {@code @Bean} methods
 * rather than component scanning.
 * </p>
 */
@AutoConfiguration
public class SpringWhalePlatformRBACUIConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RbacMenuProvider rbacMenuProvider() {
        return new RbacMenuProvider();
    }
}