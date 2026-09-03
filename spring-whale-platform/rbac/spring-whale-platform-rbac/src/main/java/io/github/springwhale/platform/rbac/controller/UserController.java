package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.database.SortUtils;
import io.github.springwhale.database.datascope.annotation.DataScope;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dto.request.UserRequest;
import io.github.springwhale.platform.rbac.dto.vo.UserVO;
import io.github.springwhale.platform.rbac.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAnyAuthority('rbac:user', '*')")
    @DataScope(module = "rbac:user")
    @GetMapping
    public Page<UserVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return userService.findWithFilter(keyword, status, pageable);
    }

    /**
     * Find user by ID
     * GET /api/rbac/users/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:user', '*')")
    @DataScope(module = "rbac:user")
    @GetMapping("/{id}")
    public UserVO findById(@PathVariable Long id) {
        return userService.findById(id)
                .orElseThrow(() -> BusinessException.create("USER_NOT_FOUND", "User not found: " + id));
    }

    /**
     * Create user
     * POST /api/rbac/users
     */
    @PreAuthorize("hasAnyAuthority('rbac:user:create', '*')")
    @DataScope(module = "rbac:user")
    @PostMapping
    public UserVO create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    /**
     * Update user
     * PUT /api/rbac/users/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:user:update', '*')")
    @DataScope(module = "rbac:user")
    @PutMapping("/{id}")
    public UserVO update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    /**
     * Delete user
     * DELETE /api/rbac/users/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:user:delete', '*')")
    @DataScope(module = "rbac:user")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}