package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.request.AssignRoleRequest;
import io.github.springwhale.rbac.dto.request.AssignRolesRequest;
import io.github.springwhale.rbac.dto.request.RemoveRoleRequest;
import io.github.springwhale.rbac.dto.vo.UserRoleVO;
import io.github.springwhale.rbac.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-role association controller
 */
@RestController
@RequestMapping("/api/rbac/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * Query user's role list
     * GET /api/rbac/user-roles/by-user?userId=1
     */
    @GetMapping("/by-user")
    public List<UserRoleVO> findByUserId(@RequestParam Integer userId) {
        return userRoleService.findByUserId(userId);
    }

    /**
     * Query role's user list
     * GET /api/rbac/user-roles/by-role?roleId=1
     */
    @GetMapping("/by-role")
    public List<UserRoleVO> findByRoleId(@RequestParam Integer roleId) {
        return userRoleService.findByRoleId(roleId);
    }

    /**
     * Assign role to user
     * POST /api/rbac/user-roles/assign
     */
    @PostMapping("/assign")
    public void assignRoleToUser(@Valid @RequestBody AssignRoleRequest request) {
        userRoleService.assignRoleToUser(request.getUserId(), request.getRoleId());
    }

    /**
     * Batch assign roles to user
     * POST /api/rbac/user-roles/assign-batch
     */
    @PostMapping("/assign-batch")
    public void assignRolesToUser(@Valid @RequestBody AssignRolesRequest request) {
        userRoleService.assignRolesToUser(request.getUserId(), request.getRoleIds());
    }

    /**
     * Remove role from user
     * DELETE /api/rbac/user-roles/remove
     */
    @DeleteMapping("/remove")
    public void removeRoleFromUser(@Valid @RequestBody RemoveRoleRequest request) {
        userRoleService.removeRoleFromUser(request.getUserId(), request.getRoleId());
    }

    /**
     * Remove all roles from user
     * DELETE /api/rbac/user-roles/remove-all?userId=1
     */
    @DeleteMapping("/remove-all")
    public void removeAllRolesFromUser(@RequestParam Integer userId) {
        userRoleService.removeAllRolesFromUser(userId);
    }
}
