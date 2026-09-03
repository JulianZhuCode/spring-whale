package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.database.datascope.DataScopeProperties;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.platform.rbac.dao.repository.*;
import io.github.springwhale.platform.rbac.security.RBACDataScopeHandler;
import io.github.springwhale.platform.rbac.security.RbacSecurityConfigProvider;
import io.github.springwhale.platform.rbac.security.UserDetailsServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class SecurityConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService userDetailsService(UserRepository userRepository,
                                                 UserRoleRepository userRoleRepository,
                                                 RoleRepository roleRepository,
                                                 RoleMenuRepository roleMenuRepository,
                                                 MenuRepository menuRepository) {
        return new UserDetailsServiceImpl(userRepository, userRoleRepository,
                roleRepository, roleMenuRepository, menuRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RbacSecurityConfigProvider rbacSecurityConfigProvider() {
        return new RbacSecurityConfigProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public RBACDataScopeHandler rbacDataScopeHandler(WhaleCacheManager cacheManager,
                                                     UserRepository userRepository,
                                                     UserRoleScopeViewRepository userRoleScopeViewRepository,
                                                     RoleMenuRepository roleMenuRepository,
                                                     GroupRepository groupRepository,
                                                     DataScopeProperties properties) {
        return new RBACDataScopeHandler(cacheManager, userRepository, userRoleScopeViewRepository, roleMenuRepository, groupRepository, properties);
    }
}