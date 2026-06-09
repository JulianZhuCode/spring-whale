package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.vo.GroupVO;
import io.github.springwhale.rbac.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分组（部门）控制器
 */
@RestController
@RequestMapping("/api/rbac/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 分页查询所有部门
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
     * 根据ID查询部门
     * GET /api/rbac/groups/{id}
     */
    @GetMapping("/{id}")
    public GroupVO findById(@PathVariable Integer id) {
        return groupService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + id));
    }

    /**
     * 根据部门编码精确查询
     * GET /api/rbac/groups/by-code?code=IT
     */
    @GetMapping("/by-code")
    public GroupVO findByCode(@RequestParam String code) {
        return groupService.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在: " + code));
    }

    /**
     * 根据父部门ID查询
     * GET /api/rbac/groups/by-parent?parentId=0
     */
    @GetMapping("/by-parent")
    public List<GroupVO> findByParentId(@RequestParam Integer parentId) {
        return groupService.findByParentId(parentId);
    }

    /**
     * 搜索部门（模糊查询）
     * GET /api/rbac/groups/search?keyword=技术
     */
    @GetMapping("/search")
    public List<GroupVO> search(@RequestParam String keyword) {
        return groupService.search(keyword);
    }

    /**
     * 根据状态查询
     * GET /api/rbac/groups/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<GroupVO> findByStatus(@RequestParam Integer status) {
        return groupService.findByStatus(status);
    }

    /**
     * 获取所有根部门
     * GET /api/rbac/groups/root
     */
    @GetMapping("/root")
    public List<GroupVO> findRootGroups() {
        return groupService.findRootGroups();
    }

    /**
     * 创建部门
     * POST /api/rbac/groups
     */
    @PostMapping
    public GroupVO create(@RequestBody GroupVO groupVO) {
        return groupService.create(groupVO);
    }

    /**
     * 更新部门
     * PUT /api/rbac/groups/{id}
     */
    @PutMapping("/{id}")
    public GroupVO update(@PathVariable Integer id, @RequestBody GroupVO groupVO) {
        return groupService.update(id, groupVO);
    }

    /**
     * 删除部门
     * DELETE /api/rbac/groups/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        groupService.delete(id);
    }
}
