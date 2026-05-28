package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.UserDTO;
import io.github.springwhale.rbac.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/rbac/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询所有用户
     * GET /api/rbac/users?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    /**
     * 根据ID查询用户
     * GET /api/rbac/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(@PathVariable Integer id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据用户名精确查询
     * GET /api/rbac/users/by-username?username=admin
     */
    @GetMapping("/by-username")
    public ResponseEntity<UserDTO> findByUsername(@RequestParam String username) {
        return userService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据邮箱精确查询
     * GET /api/rbac/users/by-email?email=admin@example.com
     */
    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> findByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据手机号精确查询
     * GET /api/rbac/users/by-phone?phone=13800138000
     */
    @GetMapping("/by-phone")
    public ResponseEntity<UserDTO> findByPhone(@RequestParam String phone) {
        return userService.findByPhone(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 搜索用户（模糊查询）
     * GET /api/rbac/users/search?keyword=admin
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(userService.search(keyword));
    }

    /**
     * 根据部门ID查询
     * GET /api/rbac/users/by-group?groupId=1
     */
    @GetMapping("/by-group")
    public ResponseEntity<List<UserDTO>> findByGroupId(@RequestParam Integer groupId) {
        return ResponseEntity.ok(userService.findByGroupId(groupId));
    }

    /**
     * 根据状态查询
     * GET /api/rbac/users/by-status?status=1
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<UserDTO>> findByStatus(@RequestParam Integer status) {
        return ResponseEntity.ok(userService.findByStatus(status));
    }

    /**
     * 创建用户
     * POST /api/rbac/users
     */
    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.create(userDTO));
    }

    /**
     * 更新用户
     * PUT /api/rbac/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.update(id, userDTO));
    }

    /**
     * 删除用户
     * DELETE /api/rbac/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
