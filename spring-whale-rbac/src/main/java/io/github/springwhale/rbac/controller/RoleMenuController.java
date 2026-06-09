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
 * 角色菜单关联控制器
 */
@RestController
@RequestMapping("/api/rbac/role-menus")
@RequiredArgsConstructor
public class RoleMenuController {

    private final RoleMenuService roleMenuService;

    /**
     * 查询角色的菜单权限列表
     * GET /api/rbac/role-menus/by-role?roleId=1
     */
    @GetMapping("/by-role")
    public List<RoleMenuVO> findByRoleId(@RequestParam Integer roleId) {
        return roleMenuService.findByRoleId(roleId);
    }

    /**
     * 查询菜单的角色列表
     * GET /api/rbac/role-menus/by-menu?menuId=1
     */
    @GetMapping("/by-menu")
    public List<RoleMenuVO> findByMenuId(@RequestParam Integer menuId) {
        return roleMenuService.findByMenuId(menuId);
    }

    /**
     * 为角色分配菜单权限
     * POST /api/rbac/role-menus/assign
     */
    @PostMapping("/assign")
    public void assignMenuToRole(@Valid @RequestBody AssignMenuRequest request) {
        roleMenuService.assignMenuToRole(request.getRoleId(), request.getMenuId());
    }

    /**
     * 批量为角色分配菜单权限
     * POST /api/rbac/role-menus/assign-batch
     */
    @PostMapping("/assign-batch")
    public void assignMenusToRole(@Valid @RequestBody AssignMenusRequest request) {
        roleMenuService.assignMenusToRole(request.getRoleId(), request.getMenuIds());
    }

    /**
     * 移除角色的菜单权限
     * DELETE /api/rbac/role-menus/remove
     */
    @DeleteMapping("/remove")
    public void removeMenuFromRole(@Valid @RequestBody RemoveMenuRequest request) {
        roleMenuService.removeMenuFromRole(request.getRoleId(), request.getMenuId());
    }

    /**
     * 移除角色的所有菜单权限
     * DELETE /api/rbac/role-menus/remove-all?roleId=1
     */
    @DeleteMapping("/remove-all")
    public void removeAllMenusFromRole(@RequestParam Integer roleId) {
        roleMenuService.removeAllMenusFromRole(roleId);
    }
}
