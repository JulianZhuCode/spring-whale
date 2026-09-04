package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.framework.webmvc.security.SecurityProperties;
import io.github.springwhale.platform.rbac.dao.repository.*;
import io.github.springwhale.platform.rbac.listener.DataScopeCacheInvalidationListener;
import io.github.springwhale.platform.rbac.listener.UserDetailsCacheInvalidationListener;
import io.github.springwhale.platform.rbac.security.RBACDataScopeHandler;
import io.github.springwhale.platform.rbac.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthService authService(AuthenticationManager authenticationManager,
                                   UserRepository userRepository,
                                   JwtUtil jwtUtil,
                                   SecurityProperties securityProperties) {
        return new AuthService(authenticationManager, userRepository, jwtUtil, securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserService userService(UserRepository userRepository,
                                   GroupRepository groupRepository,
                                   EventPublisher eventPublisher,
                                   PasswordEncoder passwordEncoder) {
        return new UserService(userRepository, groupRepository, eventPublisher, passwordEncoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleService roleService(RoleRepository roleRepository,
                                   GroupRepository groupRepository,
                                   EventPublisher eventPublisher) {
        return new RoleService(roleRepository, groupRepository, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public MenuService menuService(MenuRepository menuRepository, MessageSource messageSource) {
        return new MenuService(menuRepository, messageSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupService groupService(GroupRepository groupRepository,
                                     UserRepository userRepository,
                                     EventPublisher eventPublisher) {
        return new GroupService(groupRepository, userRepository, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMenuService roleMenuService(RoleMenuRepository roleMenuRepository,
                                           EventPublisher eventPublisher) {
        return new RoleMenuService(roleMenuRepository, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleDeptService roleDeptService(RoleDeptRepository roleDeptRepository,
                                           EventPublisher eventPublisher) {
        return new RoleDeptService(roleDeptRepository, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserRoleService userRoleService(UserRoleRepository userRoleRepository,
                                           EventPublisher eventPublisher) {
        return new UserRoleService(userRoleRepository, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataScopeCacheInvalidationListener dataScopeCacheInvalidationListener(
            RBACDataScopeHandler handler,
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            RoleDeptRepository roleDeptRepository) {
        return new DataScopeCacheInvalidationListener(handler, userRoleRepository, userRepository, groupRepository, roleDeptRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataScopeCacheInvalidationListener.UserChangedCacheListener userChangedCacheListener(
            DataScopeCacheInvalidationListener listener) {
        return listener.new UserChangedCacheListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataScopeCacheInvalidationListener.RoleChangedCacheListener roleChangedCacheListener(
            DataScopeCacheInvalidationListener listener) {
        return listener.new RoleChangedCacheListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataScopeCacheInvalidationListener.GroupChangedCacheListener groupChangedCacheListener(
            DataScopeCacheInvalidationListener listener) {
        return listener.new GroupChangedCacheListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsCacheInvalidationListener userDetailsCacheInvalidationListener(
            UserDetailsService userDetailsService,
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            GroupRepository groupRepository) {
        return new UserDetailsCacheInvalidationListener(userDetailsService, userRoleRepository, userRepository, groupRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsCacheInvalidationListener.RoleChangedCacheListener userDetailsRoleChangedCacheListener(
            UserDetailsCacheInvalidationListener listener) {
        return listener.new RoleChangedCacheListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsCacheInvalidationListener.GroupChangedCacheListener userDetailsGroupChangedCacheListener(
            UserDetailsCacheInvalidationListener listener) {
        return listener.new GroupChangedCacheListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserDetailsCacheInvalidationListener.UserChangedCacheListener userDetailsUserChangedCacheListener(
            UserDetailsCacheInvalidationListener listener) {
        return listener.new UserChangedCacheListener();
    }
}