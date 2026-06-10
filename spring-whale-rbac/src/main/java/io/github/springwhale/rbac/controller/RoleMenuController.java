package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.request.AssignMenuRequest;
import io.github.springwhale.rbac.dto.request.AssignMenusRequest;
import io.github.springwhale.rbac.dto.request.RemoveMenuRequest;
import io.github.springwhale.rbac.dto.vo.RoleMenuVO;
import io.github.springwhale.rbac.service.RoleMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role-menu association controller
 */
@RestController
@RequestMapping("/api/rbac/role-menus")
@RequiredArgsConstructor
public class RoleMenuController {

    private final RoleMenuService roleMenuService;

    /**
     * Query role's menu permission list
     * GET /api/rbac/role-menus/by-role?roleId=1
     */
    @GetMapping("/by-role")
    public List<RoleMenuVO> findByRoleId(@RequestParam Integer roleId) {
        return roleMenuService.findByRoleId(roleId);
    }

    /**
     * Query menu's role list
     * GET /api/rbac/role-menus/by-menu?menuId=1
     */
    @GetMapping("/by-menu")
    public List<RoleMenuVO> findByMenuId(@RequestParam Integer menuId) {
        return roleMenuService.findByMenuId(menuId);
    }

    /**
     * Assign menu permission to role
     * POST /api/rbac/role-menus/assign
     */
    @PostMapping("/assign")
    public void assignMenuToRole(@Valid @RequestBody AssignMenuRequest request) {
        roleMenuService.assignMenuToRole(request.getRoleId(), request.getMenuId());
    }

    /**
     * Batch assign menu permissions to role
     * POST /api/rbac/role-menus/assign-batch
     */
    @PostMapping("/assign-batch")
    public void assignMenusToRole(@Valid @RequestBody AssignMenusRequest request) {
        roleMenuService.assignMenusToRole(request.getRoleId(), request.getMenuIds());
    }

    /**
     * Remove menu permission from role
     * DELETE /api/rbac/role-menus/remove
     */
    @DeleteMapping("/remove")
    public void removeMenuFromRole(@Valid @RequestBody RemoveMenuRequest request) {
        roleMenuService.removeMenuFromRole(request.getRoleId(), request.getMenuId());
    }

    /**
     * Remove all menu permissions from role
     * DELETE /api/rbac/role-menus/remove-all?roleId=1
     */
    @DeleteMapping("/remove-all")
    public void removeAllMenusFromRole(@RequestParam Integer roleId) {
        roleMenuService.removeAllMenusFromRole(roleId);
    }
}
