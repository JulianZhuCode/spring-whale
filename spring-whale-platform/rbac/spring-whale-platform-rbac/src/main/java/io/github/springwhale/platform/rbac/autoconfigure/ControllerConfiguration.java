package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.platform.rbac.controller.AuthController;
import io.github.springwhale.platform.rbac.controller.GroupController;
import io.github.springwhale.platform.rbac.controller.MenuController;
import io.github.springwhale.platform.rbac.controller.RoleController;
import io.github.springwhale.platform.rbac.controller.RoleDeptController;
import io.github.springwhale.platform.rbac.controller.RoleMenuController;
import io.github.springwhale.platform.rbac.controller.UserController;
import io.github.springwhale.platform.rbac.controller.page.RbacPageController;
import io.github.springwhale.platform.rbac.repository.*;
import io.github.springwhale.platform.rbac.service.AuthService;
import io.github.springwhale.platform.rbac.service.GroupService;
import io.github.springwhale.platform.rbac.service.MenuService;
import io.github.springwhale.platform.rbac.service.RoleMenuService;
import io.github.springwhale.platform.rbac.service.RoleDeptService;
import io.github.springwhale.platform.rbac.service.RoleService;
import io.github.springwhale.platform.rbac.service.UserService;
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

    @Bean
    @ConditionalOnMissingBean
    public RbacPageController rbacPageController(UserService userService,
                                                  RoleService roleService,
                                                  MenuService menuService,
                                                  GroupService groupService) {
        return new RbacPageController(userService, roleService, menuService, groupService);
    }
}