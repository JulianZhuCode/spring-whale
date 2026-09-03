package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.framework.webmvc.security.SecurityProperties;
import io.github.springwhale.platform.rbac.dao.repository.*;
import io.github.springwhale.platform.rbac.dto.mapper.GroupMapper;
import io.github.springwhale.platform.rbac.dto.mapper.MenuMapper;
import io.github.springwhale.platform.rbac.dto.mapper.RoleMapper;
import io.github.springwhale.platform.rbac.dto.mapper.UserMapper;
import io.github.springwhale.platform.rbac.listener.DataScopeCacheInvalidationListener;
import io.github.springwhale.platform.rbac.security.RBACDataScopeHandler;
import io.github.springwhale.platform.rbac.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
                                   UserMapper userMapper,
                                   EventPublisher eventPublisher,
                                   PasswordEncoder passwordEncoder) {
        return new UserService(userRepository, groupRepository, userMapper, eventPublisher, passwordEncoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleService roleService(RoleRepository roleRepository,
                                   GroupRepository groupRepository,
                                   RoleMapper roleMapper,
                                   EventPublisher eventPublisher) {
        return new RoleService(roleRepository, groupRepository, roleMapper, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public MenuService menuService(MenuRepository menuRepository,
                                   MenuMapper menuMapper) {
        return new MenuService(menuRepository, menuMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupService groupService(GroupRepository groupRepository,
                                     UserRepository userRepository,
                                     GroupMapper groupMapper,
                                     EventPublisher eventPublisher) {
        return new GroupService(groupRepository, userRepository, groupMapper, eventPublisher);
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
    public DataScopeCacheInvalidationListener.UserRoleChangedCacheListener userRoleChangedCacheListener(
            DataScopeCacheInvalidationListener listener) {
        return listener.new UserRoleChangedCacheListener();
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
}