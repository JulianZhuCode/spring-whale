package io.github.springwhale.rbac.controller;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.GroupRequest;
import io.github.springwhale.rbac.dto.vo.GroupVO;
import io.github.springwhale.rbac.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
     * Find all departments with pagination
     * GET /api/rbac/groups?page=0&size=20
     */
    @GetMapping
    public Page<GroupVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return groupService.findAll(pageable);
    }

    /**
     * Find department by ID
     * GET /api/rbac/groups/{id}
     */
    @GetMapping("/{id}")
    public GroupVO findById(@PathVariable Integer id) {
        return groupService.findById(id)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found: " + id));
    }

    /**
     * Find department by exact code
     * GET /api/rbac/groups/by-code?code=IT
     */
    @GetMapping("/by-code")
    public GroupVO findByCode(@RequestParam String code) {
        return groupService.findByCode(code)
                .orElseThrow(() -> BusinessException.create("GROUP_NOT_FOUND", "Department not found: " + code));
    }

    /**
     * Find departments by parent ID
     * GET /api/rbac/groups/by-parent?parentId=0
     */
    @GetMapping("/by-parent")
    public List<GroupVO> findByParentId(@RequestParam Integer parentId) {
        return groupService.findByParentId(parentId);
    }

    /**
     * Search departments (fuzzy)
     * GET /api/rbac/groups/search?keyword=IT
     */
    @GetMapping("/search")
    public List<GroupVO> search(@RequestParam String keyword) {
        return groupService.search(keyword);
    }

    /**
     * Find by status
     * GET /api/rbac/groups/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<GroupVO> findByStatus(@RequestParam Integer status) {
        return groupService.findByStatus(status);
    }

    /**
     * Get all root departments
     * GET /api/rbac/groups/root
     */
    @GetMapping("/root")
    public List<GroupVO> findRootGroups() {
        return groupService.findRootGroups();
    }

    /**
     * Create department
     * POST /api/rbac/groups
     */
    @PostMapping
    public GroupVO create(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request);
    }

    /**
     * Update department
     * PUT /api/rbac/groups/{id}
     */
    @PutMapping("/{id}")
    public GroupVO update(@PathVariable Integer id, @Valid @RequestBody GroupRequest request) {
        return groupService.update(id, request);
    }

    /**
     * Delete department
     * DELETE /api/rbac/groups/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        groupService.delete(id);
    }
}
