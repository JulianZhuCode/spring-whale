package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.vo.RoleVO;
import io.github.springwhale.rbac.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<RoleVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roleService.findAll(pageable);
    }

    /**
     * 根据ID查询角色
     * GET /api/rbac/roles/{id}
     */
    @GetMapping("/{id}")
    public RoleVO findById(@PathVariable Integer id) {
        return roleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + id));
    }

    /**
     * 根据角色编码精确查询
     * GET /api/rbac/roles/by-code?code=ADMIN
     */
    @GetMapping("/by-code")
    public RoleVO findByCode(@RequestParam String code) {
        return roleService.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + code));
    }

    /**
     * 搜索角色（模糊查询）
     * GET /api/rbac/roles/search?keyword=管理员
     */
    @GetMapping("/search")
    public List<RoleVO> search(@RequestParam String keyword) {
        return roleService.search(keyword);
    }

    /**
     * 根据状态查询
     * GET /api/rbac/roles/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<RoleVO> findByStatus(@RequestParam Integer status) {
        return roleService.findByStatus(status);
    }

    /**
     * 创建角色
     * POST /api/rbac/roles
     */
    @PostMapping
    public RoleVO create(@RequestBody RoleVO roleVO) {
        return roleService.create(roleVO);
    }

    /**
     * 更新角色
     * PUT /api/rbac/roles/{id}
     */
    @PutMapping("/{id}")
    public RoleVO update(@PathVariable Integer id, @RequestBody RoleVO roleVO) {
        return roleService.update(id, roleVO);
    }

    /**
     * 删除角色
     * DELETE /api/rbac/roles/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        roleService.delete(id);
    }
}
