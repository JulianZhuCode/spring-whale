package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.database.SortUtils;
import io.github.springwhale.database.datascope.annotation.DataScope;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.constant.RbacConstants;
import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import io.github.springwhale.platform.rbac.dao.entity.RoleMenuEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.platform.rbac.dao.repository.RoleRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRepository;
import io.github.springwhale.platform.rbac.dao.repository.UserRoleRepository;
import io.github.springwhale.platform.rbac.dto.request.MenuRequest;
import io.github.springwhale.platform.rbac.dto.vo.MenuTreeVO;
import io.github.springwhale.platform.rbac.dto.vo.MenuVO;
import io.github.springwhale.platform.rbac.enums.MenuType;
import io.github.springwhale.platform.rbac.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Menu controller
 */
@RestController
@RequestMapping("/api/rbac/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;

    /**
     * Find all menus with pagination and filter
     * GET /api/rbac/menus?page=0&size=20&keyword=&type=&status=
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu', '*')")
    @DataScope(module = "rbac")
    @GetMapping
    public Page<MenuVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MenuType type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return menuService.findWithFilter(keyword, type, status, pageable);
    }

    /**
     * Find menu by ID
     * GET /api/rbac/menus/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu', '*')")
    @DataScope(module = "rbac:menu")
    @GetMapping("/{id}")
    public MenuVO findById(@PathVariable Integer id) {
        return menuService.findById(id)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "Menu not found: " + id));
    }

    /**
     * Create menu
     * POST /api/rbac/menus
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu:create', '*')")
    @DataScope(module = "rbac:menu")
    @PostMapping
    public MenuVO create(@Valid @RequestBody MenuRequest request) {
        return menuService.create(request);
    }

    /**
     * Update menu
     * PUT /api/rbac/menus/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu:update', '*')")
    @DataScope(module = "rbac:menu")
    @PutMapping("/{id}")
    public MenuVO update(@PathVariable Integer id, @Valid @RequestBody MenuRequest request) {
        return menuService.update(id, request);
    }

    /**
     * Delete menu
     * DELETE /api/rbac/menus/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu:delete', '*')")
    @DataScope(module = "rbac:menu")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        menuService.delete(id);
    }

    /**
     * Get menu tree filtered by current user's permissions
     * GET /api/rbac/menus/tree
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu', '*')")
    @GetMapping("/tree")
    public List<MenuTreeVO> tree() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return List.of();
        }

        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
        List<Integer> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).toList();
        List<RoleEntity> roles = roleRepository.findAllById(roleIds);

        boolean isSuperAdmin = roles.stream().anyMatch(
                r -> r.getStatus() == 1 && RbacConstants.SUPER_ADMIN_ROLE_CODE.equals(r.getCode()));
        if (isSuperAdmin) {
            return menuService.buildTree(null);
        }

        Set<Integer> menuIds = new HashSet<>();
        for (Integer roleId : roleIds) {
            roleMenuRepository.findByRoleId(roleId).stream()
                    .map(RoleMenuEntity::getMenuId)
                    .forEach(menuIds::add);
        }
        return menuService.buildTree(menuIds);
    }
}