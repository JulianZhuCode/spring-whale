package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.database.SortUtils;
import io.github.springwhale.database.datascope.annotation.DataScope;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dto.request.GroupRequest;
import io.github.springwhale.platform.rbac.dto.vo.GroupTreeVO;
import io.github.springwhale.platform.rbac.dto.vo.GroupVO;
import io.github.springwhale.platform.rbac.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Group (department) controller
 */
@RestController
@RequestMapping("/api/rbac/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * Get department tree
     * GET /api/rbac/groups/tree
     */
    @PreAuthorize("hasAnyAuthority('rbac:group', '*')")
    @DataScope(module = "rbac:group")
    @GetMapping("/tree")
    public List<GroupTreeVO> tree() {
        return groupService.buildDeptTree();
    }

    /**
     * Find all departments with pagination and filter
     * GET /api/rbac/groups?page=0&size=20&keyword=&status=
     */
    @PreAuthorize("hasAnyAuthority('rbac:group', '*')")
    @DataScope(module = "rbac:group")
    @GetMapping
    public Page<GroupVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return groupService.findWithFilter(keyword, status, pageable);
    }

    /**
     * Find department by ID
     * GET /api/rbac/groups/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:group', '*')")
    @DataScope(module = "rbac:group")
    @GetMapping("/{id}")
    public GroupVO findById(@PathVariable Integer id) {
        return groupService.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found: " + id));
    }

    /**
     * Create department
     * POST /api/rbac/groups
     */
    @PreAuthorize("hasAnyAuthority('rbac:group:create', '*')")
    @DataScope(module = "rbac:group")
    @PostMapping
    public GroupVO create(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request);
    }

    /**
     * Update department
     * PUT /api/rbac/groups/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:group:update', '*')")
    @DataScope(module = "rbac:group")
    @PutMapping("/{id}")
    public GroupVO update(@PathVariable Integer id, @Valid @RequestBody GroupRequest request) {
        return groupService.update(id, request);
    }

    /**
     * Delete department
     * DELETE /api/rbac/groups/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:group:delete', '*')")
    @DataScope(module = "rbac:group")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        groupService.delete(id);
    }
}