package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.platform.rbac.dao.repository.*;
import io.github.springwhale.platform.rbac.dto.mapper.GroupMapper;
import io.github.springwhale.platform.rbac.dto.mapper.MenuMapper;
import io.github.springwhale.platform.rbac.dto.mapper.RoleMapper;
import io.github.springwhale.platform.rbac.dto.mapper.UserMapper;
import io.github.springwhale.platform.rbac.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

@Configuration
public class ServiceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuthService authService(AuthenticationManager authenticationManager,
                                   UserRepository userRepository,
                                   JwtUtil jwtUtil) {
        return new AuthService(authenticationManager, userRepository, jwtUtil);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserService userService(UserRepository userRepository,
                                   GroupRepository groupRepository,
                                   UserMapper userMapper) {
        return new UserService(userRepository, groupRepository, userMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleService roleService(RoleRepository roleRepository,
                                   GroupRepository groupRepository,
                                   RoleMapper roleMapper) {
        return new RoleService(roleRepository, groupRepository, roleMapper);
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
                                     GroupMapper groupMapper) {
        return new GroupService(groupRepository, groupMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMenuService roleMenuService(RoleMenuRepository roleMenuRepository) {
        return new RoleMenuService(roleMenuRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleDeptService roleDeptService(RoleDeptRepository roleDeptRepository) {
        return new RoleDeptService(roleDeptRepository);
    }
}