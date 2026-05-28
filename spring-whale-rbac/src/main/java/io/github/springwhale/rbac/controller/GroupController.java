package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.GroupDTO;
import io.github.springwhale.rbac.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<GroupDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(groupService.findAll(pageable));
    }

    /**
     * 根据ID查询部门
     * GET /api/rbac/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> findById(@PathVariable Integer id) {
        return groupService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据部门编码精确查询
     * GET /api/rbac/groups/by-code?code=IT
     */
    @GetMapping("/by-code")
    public ResponseEntity<GroupDTO> findByCode(@RequestParam String code) {
        return groupService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据父部门ID查询
     * GET /api/rbac/groups/by-parent?parentId=0
     */
    @GetMapping("/by-parent")
    public ResponseEntity<List<GroupDTO>> findByParentId(@RequestParam Integer parentId) {
        return ResponseEntity.ok(groupService.findByParentId(parentId));
    }

    /**
     * 搜索部门（模糊查询）
     * GET /api/rbac/groups/search?keyword=技术
     */
    @GetMapping("/search")
    public ResponseEntity<List<GroupDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(groupService.search(keyword));
    }

    /**
     * 根据状态查询
     * GET /api/rbac/groups/by-status?status=1
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<GroupDTO>> findByStatus(@RequestParam Integer status) {
        return ResponseEntity.ok(groupService.findByStatus(status));
    }

    /**
     * 获取所有根部门
     * GET /api/rbac/groups/root
     */
    @GetMapping("/root")
    public ResponseEntity<List<GroupDTO>> findRootGroups() {
        return ResponseEntity.ok(groupService.findRootGroups());
    }

    /**
     * 创建部门
     * POST /api/rbac/groups
     */
    @PostMapping
    public ResponseEntity<GroupDTO> create(@RequestBody GroupDTO groupDTO) {
        return ResponseEntity.ok(groupService.create(groupDTO));
    }

    /**
     * 更新部门
     * PUT /api/rbac/groups/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<GroupDTO> update(@PathVariable Integer id, @RequestBody GroupDTO groupDTO) {
        return ResponseEntity.ok(groupService.update(id, groupDTO));
    }

    /**
     * 删除部门
     * DELETE /api/rbac/groups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        groupService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
