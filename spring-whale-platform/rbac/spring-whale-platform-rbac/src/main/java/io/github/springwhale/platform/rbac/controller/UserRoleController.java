package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.platform.rbac.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * User-role assignment controller
 */
@RestController
@RequestMapping("/api/rbac/users/{userId}/roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * Get role IDs assigned to a user
     * GET /api/rbac/users/{userId}/roles
     */
    @PreAuthorize("hasAnyAuthority('rbac:user', '*')")
    @GetMapping
    public List<Long> getRoles(@PathVariable Long userId) {
        return userRoleService.getRoleIdsByUserId(userId);
    }

    /**
     * Batch add roles to a user
     * POST /api/rbac/users/{userId}/roles
     */
    @PreAuthorize("hasAnyAuthority('rbac:user:update', '*')")
    @PostMapping
    public ResponseEntity<Map<String, String>> addRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userRoleService.addRoles(userId, roleIds);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    /**
     * Batch remove roles from a user
     * DELETE /api/rbac/users/{userId}/roles
     */
    @PreAuthorize("hasAnyAuthority('rbac:user:update', '*')")
    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userRoleService.removeRoles(userId, roleIds);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }
}