package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.platform.rbac.dao.mapper.GroupMapper;
import io.github.springwhale.platform.rbac.dao.mapper.MenuMapper;
import io.github.springwhale.platform.rbac.dao.mapper.RoleMapper;
import io.github.springwhale.platform.rbac.dao.mapper.UserMapper;
import io.github.springwhale.platform.rbac.repository.GroupRepository;
import io.github.springwhale.platform.rbac.repository.MenuRepository;
import io.github.springwhale.platform.rbac.repository.RoleRepository;
import io.github.springwhale.platform.rbac.repository.UserRepository;
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
                                   PasswordEncoder passwordEncoder,
                                   UserMapper userMapper) {
        return new AuthService(authenticationManager, userRepository, jwtUtil, passwordEncoder, userMapper);
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
}