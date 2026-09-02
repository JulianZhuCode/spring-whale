package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.platform.rbac.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Role-menu assignment controller
 */
@RestController
@RequestMapping("/api/rbac/roles/{roleId}/menus")
@RequiredArgsConstructor
public class RoleMenuController {

    private final RoleMenuService roleMenuService;

    /**
     * Get menu IDs assigned to a role
     * GET /api/rbac/roles/{roleId}/menus
     */
    @PreAuthorize("hasAnyAuthority('rbac:role', '*')")
    @GetMapping
    public List<Integer> getMenus(@PathVariable Integer roleId) {
        return roleMenuService.getMenuIdsByRoleId(roleId);
    }

    /**
     * Batch add menus to a role
     * POST /api/rbac/roles/{roleId}/menus
     */
    @PreAuthorize("hasAnyAuthority('rbac:role:update', '*')")
    @PostMapping
    public ResponseEntity<Map<String, String>> addMenus(@PathVariable Integer roleId, @RequestBody List<Integer> menuIds) {
        roleMenuService.addMenus(roleId, menuIds);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    /**
     * Batch remove menus from a role
     * DELETE /api/rbac/roles/{roleId}/menus
     */
    @PreAuthorize("hasAnyAuthority('rbac:role:update', '*')")
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeMenus(@PathVariable Integer roleId, @RequestBody List<Integer> menuIds) {
        roleMenuService.removeMenus(roleId, menuIds);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}