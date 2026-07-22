package io.github.springwhale.rbac.controller.page;

import io.github.springwhale.framework.thymeleaf.controller.AdminPage;
import io.github.springwhale.rbac.dto.vo.GroupVO;
import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.database.SortUtils;
import io.github.springwhale.rbac.service.GroupService;
import io.github.springwhale.rbac.service.MenuService;
import io.github.springwhale.rbac.service.RoleService;
import io.github.springwhale.rbac.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer status,
                        @RequestParam(required = false) String sort,
                        Model model) {
        Sort sortObj = SortUtils.buildSort(sort);
        Page<UserVO> userPage = userService.findWithFilter(keyword, status, PageRequest.of(page, size, sortObj));
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("page", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status != null ? status.toString() : null);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));
        return "admin/rbac/users";
    }

    // ---- Roles ----

    @GetMapping("/roles")
    public String roles(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer status,
                        @RequestParam(required = false) String sort,
                        Model model) {
        Sort sortObj = SortUtils.buildSort(sort);
        Page<RoleVO> rolePage = roleService.findWithFilter(keyword, status, PageRequest.of(page, size, sortObj));
        model.addAttribute("roles", rolePage.getContent());
        model.addAttribute("page", rolePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status != null ? status.toString() : null);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));
        return "admin/rbac/roles";
    }

    // ---- Menus ----

    @GetMapping("/menus")
    public String menus(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer type,
                        @RequestParam(required = false) Integer status,
                        @RequestParam(required = false) String sort,
                        Model model) {
        Sort sortObj = SortUtils.buildSort(sort);
        Page<MenuVO> menuPage = menuService.findWithFilter(keyword, type, status, PageRequest.of(page, size, sortObj));
        model.addAttribute("menus", menuPage.getContent());
        model.addAttribute("page", menuPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", type != null ? type.toString() : null);
        model.addAttribute("selectedStatus", status != null ? status.toString() : null);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));
        return "admin/rbac/menus";
    }

    // ---- Groups ----

    @GetMapping("/groups")
    public String groups(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Integer status,
                         @RequestParam(required = false) String sort,
                         Model model) {
        Sort sortObj = SortUtils.buildSort(sort);
        Page<GroupVO> groupPage = groupService.findWithFilter(keyword, status, PageRequest.of(page, size, sortObj));
        model.addAttribute("groups", groupPage.getContent());
        model.addAttribute("page", groupPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status != null ? status.toString() : null);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));
        return "admin/rbac/groups";
    }

}
