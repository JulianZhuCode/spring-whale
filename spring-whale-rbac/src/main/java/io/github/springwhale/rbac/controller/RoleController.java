package io.github.springwhale.rbac.controller;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.RoleRequest;
import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping
    public Page<RoleVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Pageable pageable = PageRequest.of(page, size);
        return roleService.findWithFilter(keyword, status, pageable);
    }

    /**
     * Find role by ID
     * GET /api/rbac/roles/{id}
     */
    @GetMapping("/{id}")
    public RoleVO findById(@PathVariable Integer id) {
        return roleService.findById(id)
                .orElseThrow(() -> BusinessException.create("ROLE_NOT_FOUND", "Role not found: " + id));
    }

    /**
     * Find role by exact code
     * GET /api/rbac/roles/by-code?code=ADMIN
     */
    @GetMapping("/by-code")
    public RoleVO findByCode(@RequestParam String code) {
        return roleService.findByCode(code)
                .orElseThrow(() -> BusinessException.create("ROLE_NOT_FOUND", "Role not found: " + code));
    }

    /**
     * Search roles (fuzzy)
     * GET /api/rbac/roles/search?keyword=admin
     */
    @GetMapping("/search")
    public List<RoleVO> search(@RequestParam String keyword) {
        return roleService.search(keyword);
    }

    /**
     * Find by status
     * GET /api/rbac/roles/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<RoleVO> findByStatus(@RequestParam Integer status) {
        return roleService.findByStatus(status);
    }

    /**
     * Create role
     * POST /api/rbac/roles
     */
    @PostMapping
    public RoleVO create(@Valid @RequestBody RoleRequest request) {
        return roleService.create(request);
    }

    /**
     * Update role
     * PUT /api/rbac/roles/{id}
     */
    @PutMapping("/{id}")
    public RoleVO update(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        return roleService.update(id, request);
    }

    /**
     * Delete role
     * DELETE /api/rbac/roles/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleService.delete(id);
    }
}
