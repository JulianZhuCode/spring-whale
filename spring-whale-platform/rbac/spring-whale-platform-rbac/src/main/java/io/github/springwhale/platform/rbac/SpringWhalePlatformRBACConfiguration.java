package io.github.springwhale.platform.rbac;

import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.platform.rbac.config.AdminInitializer;
import io.github.springwhale.platform.rbac.controller.AuthController;
import io.github.springwhale.platform.rbac.controller.GroupController;
import io.github.springwhale.platform.rbac.controller.MenuController;
import io.github.springwhale.platform.rbac.controller.RoleController;
import io.github.springwhale.platform.rbac.controller.RoleMenuController;
import io.github.springwhale.platform.rbac.controller.UserController;
import io.github.springwhale.platform.rbac.controller.UserRoleController;
import io.github.springwhale.platform.rbac.controller.page.RbacPageController;
import io.github.springwhale.platform.rbac.mapper.GroupMapper;
import io.github.springwhale.platform.rbac.mapper.MenuMapper;
import io.github.springwhale.platform.rbac.mapper.RoleMapper;
import io.github.springwhale.platform.rbac.mapper.RoleMenuMapper;
import io.github.springwhale.platform.rbac.mapper.UserMapper;
import io.github.springwhale.platform.rbac.mapper.UserRoleMapper;
import io.github.springwhale.platform.rbac.repository.GroupRepository;
import io.github.springwhale.platform.rbac.repository.MenuRepository;
import io.github.springwhale.platform.rbac.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.repository.RoleRepository;
import io.github.springwhale.platform.rbac.repository.UserRepository;
import io.github.springwhale.platform.rbac.repository.UserRoleRepository;
import io.github.springwhale.platform.rbac.security.RbacSecurityConfigProvider;
import io.github.springwhale.platform.rbac.security.UserDetailsServiceImpl;
import io.github.springwhale.platform.rbac.service.AuthService;
import io.github.springwhale.platform.rbac.service.GroupService;
import io.github.springwhale.platform.rbac.service.MenuService;
import io.github.springwhale.platform.rbac.service.RoleMenuService;
import io.github.springwhale.platform.rbac.service.RoleService;
import io.github.springwhale.platform.rbac.service.UserRoleService;
import io.github.springwhale.platform.rbac.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Auto-configuration for Spring Whale Platform RBAC module.
 * <p>
 * All beans are explicitly registered via {@code @Bean} methods
 * rather than component scanning.
 * </p>
 */
@AutoConfiguration
@EnableCaching
@EnableJpaRepositories
@EntityScan
@Slf4j
public class SpringWhalePlatformRBACConfiguration {

    // ==================== Mappers ====================

    @Bean
    @ConditionalOnMissingBean
    public UserMapper userMapper() {
        return new UserMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMapper roleMapper() {
        return new RoleMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public MenuMapper menuMapper() {
        return new MenuMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupMapper groupMapper() {
        return new GroupMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMenuMapper roleMenuMapper() {
        return new RoleMenuMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserRoleMapper userRoleMapper() {
        return new UserRoleMapper();
    }

    // ==================== Services ====================

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

    @Bean
    @ConditionalOnMissingBean
    public UserRoleService userRoleService(UserRoleRepository userRoleRepository,
                                           UserRoleMapper userRoleMapper) {
        return new UserRoleService(userRoleRepository, userRoleMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMenuService roleMenuService(RoleMenuRepository roleMenuRepository,
                                           RoleMenuMapper roleMenuMapper) {
        return new RoleMenuService(roleMenuRepository, roleMenuMapper);
    }

    // ==================== Security ====================

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

    // ==================== Initializer ====================

    @Bean
    @ConditionalOnMissingBean
    public AdminInitializer adminInitializer(UserRepository userRepository,
                                             RoleRepository roleRepository,
                                             UserRoleRepository userRoleRepository,
                                             MenuRepository menuRepository,
                                             GroupRepository groupRepository,
                                             PasswordEncoder passwordEncoder) {
        return new AdminInitializer(userRepository, roleRepository, userRoleRepository,
                menuRepository, groupRepository, passwordEncoder);
    }

    // ==================== Controllers ====================

    @Bean
    @ConditionalOnMissingBean
    public AuthController authController(AuthService authService) {
        return new AuthController(authService);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserController userController(UserService userService) {
        return new UserController(userService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleController roleController(RoleService roleService) {
        return new RoleController(roleService);
    }

    @Bean
    @ConditionalOnMissingBean
    public MenuController menuController(MenuService menuService) {
        return new MenuController(menuService);
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupController groupController(GroupService groupService) {
        return new GroupController(groupService);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserRoleController userRoleController(UserRoleService userRoleService) {
        return new UserRoleController(userRoleService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMenuController roleMenuController(RoleMenuService roleMenuService) {
        return new RoleMenuController(roleMenuService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RbacPageController rbacPageController(UserService userService,
                                                  RoleService roleService,
                                                  MenuService menuService,
                                                  GroupService groupService) {
        return new RbacPageController(userService, roleService, menuService, groupService);
    }
}