package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.database.SortUtils;
import io.github.springwhale.database.datascope.DataScope;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dto.request.RoleRequest;
import io.github.springwhale.platform.rbac.dto.vo.RoleVO;
import io.github.springwhale.platform.rbac.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Role controller
 */
@RestController
@RequestMapping("/api/rbac/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Find all roles with pagination and filter
     * GET /api/rbac/roles?page=0&size=20&keyword=&status=
     */
    @PreAuthorize("hasAnyAuthority('rbac:role', '*')")
    @DataScope(module = "rbac:role")
    @GetMapping
    public Page<RoleVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return roleService.findWithFilter(keyword, status, pageable);
    }

    /**
     * Find role by ID
     * GET /api/rbac/roles/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:role', '*')")
    @DataScope(module = "rbac:role")
    @GetMapping("/{id}")
    public RoleVO findById(@PathVariable Integer id) {
        return roleService.findById(id)
                .orElseThrow(() -> BusinessException.create("ROLE_NOT_FOUND", "Role not found: " + id));
    }

    /**
     * Create role
     * POST /api/rbac/roles
     */
    @PreAuthorize("hasAnyAuthority('rbac:role:create', '*')")
    @DataScope(module = "rbac:role")
    @PostMapping
    public RoleVO create(@Valid @RequestBody RoleRequest request) {
        return roleService.create(request);
    }

    /**
     * Update role
     * PUT /api/rbac/roles/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:role:update', '*')")
    @DataScope(module = "rbac:role")
    @PutMapping("/{id}")
    public RoleVO update(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        return roleService.update(id, request);
    }

    /**
     * Delete role
     * DELETE /api/rbac/roles/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:role:delete', '*')")
    @DataScope(module = "rbac:role")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleService.delete(id);
    }
}