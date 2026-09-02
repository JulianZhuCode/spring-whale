package io.github.springwhale.platform.rbac.autoconfigure;

import io.github.springwhale.platform.rbac.controller.AuthController;
import io.github.springwhale.platform.rbac.controller.GroupController;
import io.github.springwhale.platform.rbac.controller.MenuController;
import io.github.springwhale.platform.rbac.controller.RoleController;
import io.github.springwhale.platform.rbac.controller.UserController;
import io.github.springwhale.platform.rbac.controller.page.RbacPageController;
import io.github.springwhale.platform.rbac.service.AuthService;
import io.github.springwhale.platform.rbac.service.GroupService;
import io.github.springwhale.platform.rbac.service.MenuService;
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
    public RbacPageController rbacPageController(UserService userService,
                                                  RoleService roleService,
                                                  MenuService menuService,
                                                  GroupService groupService) {
        return new RbacPageController(userService, roleService, menuService, groupService);
    }
}