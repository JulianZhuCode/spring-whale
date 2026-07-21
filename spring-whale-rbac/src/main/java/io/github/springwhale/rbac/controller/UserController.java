package io.github.springwhale.rbac.controller;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.UserRequest;
import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User controller
 */
@RestController
@RequestMapping("/api/rbac/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Find all users with pagination and filter
     * GET /api/rbac/users?page=0&size=20&keyword=&status=
     */
    @GetMapping
    public Page<UserVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.findWithFilter(keyword, status, pageable);
    }

    /**
     * Find user by ID
     * GET /api/rbac/users/{id}
     */
    @GetMapping("/{id}")
    public UserVO findById(@PathVariable Integer id) {
        return userService.findById(id)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found: " + id));
    }

    /**
     * Find by exact username
     * GET /api/rbac/users/by-username?username=admin
     */
    @GetMapping("/by-username")
    public UserVO findByUsername(@RequestParam String username) {
        return userService.findByUsername(username)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found: " + username));
    }

    /**
     * Find by exact email
     * GET /api/rbac/users/by-email?email=admin@example.com
     */
    @GetMapping("/by-email")
    public UserVO findByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found: " + email));
    }

    /**
     * Find by exact phone
     * GET /api/rbac/users/by-phone?phone=13800138000
     */
    @GetMapping("/by-phone")
    public UserVO findByPhone(@RequestParam String phone) {
        return userService.findByPhone(phone)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found: " + phone));
    }

    /**
     * Search users (fuzzy)
     * GET /api/rbac/users/search?keyword=admin
     */
    @GetMapping("/search")
    public List<UserVO> search(@RequestParam String keyword) {
        return userService.search(keyword);
    }

    /**
     * Find users by department ID
     * GET /api/rbac/users/by-group?groupId=1
     */
    @GetMapping("/by-group")
    public List<UserVO> findByGroupId(@RequestParam Integer groupId) {
        return userService.findByGroupId(groupId);
    }

    /**
     * Find by status
     * GET /api/rbac/users/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<UserVO> findByStatus(@RequestParam Integer status) {
        return userService.findByStatus(status);
    }

    /**
     * Create user
     * POST /api/rbac/users
     */
    @PostMapping
    public UserVO create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    /**
     * Update user
     * PUT /api/rbac/users/{id}
     */
    @PutMapping("/{id}")
    public UserVO update(@PathVariable Integer id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    /**
     * Delete user
     * DELETE /api/rbac/users/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }
}
