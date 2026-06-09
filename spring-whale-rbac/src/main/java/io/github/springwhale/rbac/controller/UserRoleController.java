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
    public List<UserRoleVO> findByUserId(@RequestParam Integer userId) {
        return userRoleService.findByUserId(userId);
    }

    /**
     * 查询角色的用户列表
     * GET /api/rbac/user-roles/by-role?roleId=1
     */
    @GetMapping("/by-role")
    public List<UserRoleVO> findByRoleId(@RequestParam Integer roleId) {
        return userRoleService.findByRoleId(roleId);
    }

    /**
     * 为用户分配角色
     * POST /api/rbac/user-roles/assign
     */
    @PostMapping("/assign")
    public void assignRoleToUser(@Valid @RequestBody AssignRoleRequest request) {
        userRoleService.assignRoleToUser(request.getUserId(), request.getRoleId());
    }

    /**
     * 批量为用户分配角色
     * POST /api/rbac/user-roles/assign-batch
     */
    @PostMapping("/assign-batch")
    public void assignRolesToUser(@Valid @RequestBody AssignRolesRequest request) {
        userRoleService.assignRolesToUser(request.getUserId(), request.getRoleIds());
    }

    /**
     * 移除用户的角色
     * DELETE /api/rbac/user-roles/remove
     */
    @DeleteMapping("/remove")
    public void removeRoleFromUser(@Valid @RequestBody RemoveRoleRequest request) {
        userRoleService.removeRoleFromUser(request.getUserId(), request.getRoleId());
    }

    /**
     * 移除用户的所有角色
     * DELETE /api/rbac/user-roles/remove-all?userId=1
     */
    @DeleteMapping("/remove-all")
    public void removeAllRolesFromUser(@RequestParam Integer userId) {
        userRoleService.removeAllRolesFromUser(userId);
    }
}
