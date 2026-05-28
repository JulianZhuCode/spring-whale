package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.RoleDTO;
import io.github.springwhale.rbac.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/rbac/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询所有角色
     * GET /api/rbac/roles?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<RoleDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(roleService.findAll(pageable));
    }

    /**
     * 根据ID查询角色
     * GET /api/rbac/roles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> findById(@PathVariable Integer id) {
        return roleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据角色编码精确查询
     * GET /api/rbac/roles/by-code?code=ADMIN
     */
    @GetMapping("/by-code")
    public ResponseEntity<RoleDTO> findByCode(@RequestParam String code) {
        return roleService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 搜索角色（模糊查询）
     * GET /api/rbac/roles/search?keyword=管理员
     */
    @GetMapping("/search")
    public ResponseEntity<List<RoleDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(roleService.search(keyword));
    }

    /**
     * 根据状态查询
     * GET /api/rbac/roles/by-status?status=1
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<RoleDTO>> findByStatus(@RequestParam Integer status) {
        return ResponseEntity.ok(roleService.findByStatus(status));
    }

    /**
     * 创建角色
     * POST /api/rbac/roles
     */
    @PostMapping
    public ResponseEntity<RoleDTO> create(@RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.create(roleDTO));
    }

    /**
     * 更新角色
     * PUT /api/rbac/roles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> update(@PathVariable Integer id, @RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.update(id, roleDTO));
    }

    /**
     * 删除角色
     * DELETE /api/rbac/roles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
