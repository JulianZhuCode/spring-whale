package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.platform.rbac.controller.*;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import io.github.springwhale.platform.rbac.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControllerConfiguration {

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
    public MenuController menuController(MenuService menuService,
                                         UserRepository userRepository,
                                         UserRoleRepository userRoleRepository,
                                         RoleRepository roleRepository,
                                         RoleMenuRepository roleMenuRepository) {
        return new MenuController(menuService, userRepository, userRoleRepository, roleRepository, roleMenuRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleMenuController roleMenuController(RoleMenuService roleMenuService) {
        return new RoleMenuController(roleMenuService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleDeptController roleDeptController(RoleDeptService roleDeptService) {
        return new RoleDeptController(roleDeptService);
    }

    @Bean
    @ConditionalOnMissingBean
    public GroupController groupController(GroupService groupService) {
        return new GroupController(groupService);
    }

}