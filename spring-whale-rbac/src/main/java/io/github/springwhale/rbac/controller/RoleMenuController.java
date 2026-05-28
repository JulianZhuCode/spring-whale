package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.entity.RoleMenuEntity;
import io.github.springwhale.rbac.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<RoleMenuEntity>> findByRoleId(@RequestParam Integer roleId) {
        return ResponseEntity.ok(roleMenuService.findByRoleId(roleId));
    }

    /**
     * 查询菜单的角色列表
     * GET /api/rbac/role-menus/by-menu?menuId=1
     */
    @GetMapping("/by-menu")
    public ResponseEntity<List<RoleMenuEntity>> findByMenuId(@RequestParam Integer menuId) {
        return ResponseEntity.ok(roleMenuService.findByMenuId(menuId));
    }

    /**
     * 为角色分配菜单权限
     * POST /api/rbac/role-menus/assign
     */
    @PostMapping("/assign")
    public ResponseEntity<Void> assignMenuToRole(@RequestBody Map<String, Integer> params) {
        Integer roleId = params.get("roleId");
        Integer menuId = params.get("menuId");
        roleMenuService.assignMenuToRole(roleId, menuId);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量为角色分配菜单权限
     * POST /api/rbac/role-menus/assign-batch
     */
    @PostMapping("/assign-batch")
    public ResponseEntity<Void> assignMenusToRole(@RequestBody Map<String, Object> params) {
        Integer roleId = (Integer) params.get("roleId");
        @SuppressWarnings("unchecked")
        List<Integer> menuIds = (List<Integer>) params.get("menuIds");
        roleMenuService.assignMenusToRole(roleId, menuIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 移除角色的菜单权限
     * DELETE /api/rbac/role-menus/remove
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeMenuFromRole(@RequestBody Map<String, Integer> params) {
        Integer roleId = params.get("roleId");
        Integer menuId = params.get("menuId");
        roleMenuService.removeMenuFromRole(roleId, menuId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 移除角色的所有菜单权限
     * DELETE /api/rbac/role-menus/remove-all?roleId=1
     */
    @DeleteMapping("/remove-all")
    public ResponseEntity<Void> removeAllMenusFromRole(@RequestParam Integer roleId) {
        roleMenuService.removeAllMenusFromRole(roleId);
        return ResponseEntity.noContent().build();
    }
}
