package io.github.springwhale.rbac.controller.page;

import io.github.springwhale.framework.thymeleaf.controller.AdminPage;
import io.github.springwhale.rbac.dto.vo.GroupVO;
import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.service.GroupService;
import io.github.springwhale.rbac.service.MenuService;
import io.github.springwhale.rbac.service.RoleService;
import io.github.springwhale.rbac.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin console page controller for RBAC module.
 * <p>
 * Serves Thymeleaf templates under {@code templates/admin/rbac/}.
 * REST API endpoints remain under {@code /api/rbac/**}.
 * </p>
 */
@AdminPage
@Controller
@RequestMapping("/admin/rbac")
@RequiredArgsConstructor
public class RbacPageController {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final GroupService groupService;

    // ---- Users ----

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Page<UserVO> userPage = userService.findAll(PageRequest.of(page, size));
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("page", userPage);
        return "admin/rbac/users";
    }

    // ---- Roles ----

    @GetMapping("/roles")
    public String roles(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Page<RoleVO> rolePage = roleService.findAll(PageRequest.of(page, size));
        model.addAttribute("roles", rolePage.getContent());
        model.addAttribute("page", rolePage);
        return "admin/rbac/roles";
    }

    // ---- Menus ----

    @GetMapping("/menus")
    public String menus(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Page<MenuVO> menuPage = menuService.findAll(PageRequest.of(page, size));
        model.addAttribute("menus", menuPage.getContent());
        model.addAttribute("page", menuPage);
        return "admin/rbac/menus";
    }

    // ---- Groups ----

    @GetMapping("/groups")
    public String groups(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {
        Page<GroupVO> groupPage = groupService.findAll(PageRequest.of(page, size));
        model.addAttribute("groups", groupPage.getContent());
        model.addAttribute("page", groupPage);
        return "admin/rbac/groups";
    }

}
