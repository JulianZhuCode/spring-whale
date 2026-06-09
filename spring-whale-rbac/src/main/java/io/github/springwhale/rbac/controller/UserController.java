package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.vo.UserVO;
import io.github.springwhale.rbac.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<UserVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.findAll(pageable);
    }

    /**
     * 根据ID查询用户
     * GET /api/rbac/users/{id}
     */
    @GetMapping("/{id}")
    public UserVO findById(@PathVariable Integer id) {
        return userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));
    }

    /**
     * 根据用户名精确查询
     * GET /api/rbac/users/by-username?username=admin
     */
    @GetMapping("/by-username")
    public UserVO findByUsername(@RequestParam String username) {
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + username));
    }

    /**
     * 根据邮箱精确查询
     * GET /api/rbac/users/by-email?email=admin@example.com
     */
    @GetMapping("/by-email")
    public UserVO findByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + email));
    }

    /**
     * 根据手机号精确查询
     * GET /api/rbac/users/by-phone?phone=13800138000
     */
    @GetMapping("/by-phone")
    public UserVO findByPhone(@RequestParam String phone) {
        return userService.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + phone));
    }

    /**
     * 搜索用户（模糊查询）
     * GET /api/rbac/users/search?keyword=admin
     */
    @GetMapping("/search")
    public List<UserVO> search(@RequestParam String keyword) {
        return userService.search(keyword);
    }

    /**
     * 根据部门ID查询
     * GET /api/rbac/users/by-group?groupId=1
     */
    @GetMapping("/by-group")
    public List<UserVO> findByGroupId(@RequestParam Integer groupId) {
        return userService.findByGroupId(groupId);
    }

    /**
     * 根据状态查询
     * GET /api/rbac/users/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<UserVO> findByStatus(@RequestParam Integer status) {
        return userService.findByStatus(status);
    }

    /**
     * 创建用户
     * POST /api/rbac/users
     */
    @PostMapping
    public UserVO create(@RequestBody UserVO userVO) {
        return userService.create(userVO);
    }

    /**
     * 更新用户
     * PUT /api/rbac/users/{id}
     */
    @PutMapping("/{id}")
    public UserVO update(@PathVariable Integer id, @RequestBody UserVO userVO) {
        return userService.update(id, userVO);
    }

    /**
     * 删除用户
     * DELETE /api/rbac/users/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }
}
