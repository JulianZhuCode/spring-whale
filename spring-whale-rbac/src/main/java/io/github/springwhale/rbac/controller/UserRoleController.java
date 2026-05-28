package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.entity.UserRoleEntity;
import io.github.springwhale.rbac.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户角色关联控制器
 */
@RestController
@RequestMapping("/api/rbac/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * 查询用户的角色列表
     * GET /api/rbac/user-roles/by-user?userId=1
     */
    @GetMapping("/by-user")
    public ResponseEntity<List<UserRoleEntity>> findByUserId(@RequestParam Integer userId) {
        return ResponseEntity.ok(userRoleService.findByUserId(userId));
    }

    /**
     * 查询角色的用户列表
     * GET /api/rbac/user-roles/by-role?roleId=1
     */
    @GetMapping("/by-role")
    public ResponseEntity<List<UserRoleEntity>> findByRoleId(@RequestParam Integer roleId) {
        return ResponseEntity.ok(userRoleService.findByRoleId(roleId));
    }

    /**
     * 为用户分配角色
     * POST /api/rbac/user-roles/assign
     */
    @PostMapping("/assign")
    public ResponseEntity<Void> assignRoleToUser(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");
        Integer roleId = params.get("roleId");
        userRoleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量为用户分配角色
     * POST /api/rbac/user-roles/assign-batch
     */
    @PostMapping("/assign-batch")
    public ResponseEntity<Void> assignRolesToUser(@RequestBody Map<String, Object> params) {
        Integer userId = (Integer) params.get("userId");
        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) params.get("roleIds");
        userRoleService.assignRolesToUser(userId, roleIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 移除用户的角色
     * DELETE /api/rbac/user-roles/remove
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeRoleFromUser(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");
        Integer roleId = params.get("roleId");
        userRoleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 移除用户的所有角色
     * DELETE /api/rbac/user-roles/remove-all?userId=1
     */
    @DeleteMapping("/remove-all")
    public ResponseEntity<Void> removeAllRolesFromUser(@RequestParam Integer userId) {
        userRoleService.removeAllRolesFromUser(userId);
        return ResponseEntity.noContent().build();
    }
}
