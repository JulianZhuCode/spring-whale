package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.MenuDTO;
import io.github.springwhale.rbac.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/rbac/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 分页查询所有菜单
     * GET /api/rbac/menus?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<MenuDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(menuService.findAll(pageable));
    }

    /**
     * 根据ID查询菜单
     * GET /api/rbac/menus/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuDTO> findById(@PathVariable Integer id) {
        return menuService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据菜单编码精确查询
     * GET /api/rbac/menus/by-code?code=system:user
     */
    @GetMapping("/by-code")
    public ResponseEntity<MenuDTO> findByCode(@RequestParam String code) {
        return menuService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据父菜单ID查询
     * GET /api/rbac/menus/by-parent?parentId=0
     */
    @GetMapping("/by-parent")
    public ResponseEntity<List<MenuDTO>> findByParentId(@RequestParam Integer parentId) {
        return ResponseEntity.ok(menuService.findByParentId(parentId));
    }

    /**
     * 搜索菜单（模糊查询）
     * GET /api/rbac/menus/search?keyword=用户
     */
    @GetMapping("/search")
    public ResponseEntity<List<MenuDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(menuService.search(keyword));
    }

    /**
     * 根据类型查询
     * GET /api/rbac/menus/by-type?type=2
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<MenuDTO>> findByType(@RequestParam Integer type) {
        return ResponseEntity.ok(menuService.findByType(type));
    }

    /**
     * 根据状态查询
     * GET /api/rbac/menus/by-status?status=1
     */
    @GetMapping("/by-status")
    public ResponseEntity<List<MenuDTO>> findByStatus(@RequestParam Integer status) {
        return ResponseEntity.ok(menuService.findByStatus(status));
    }

    /**
     * 根据可见性查询
     * GET /api/rbac/menus/by-visible?visible=1
     */
    @GetMapping("/by-visible")
    public ResponseEntity<List<MenuDTO>> findByVisible(@RequestParam Integer visible) {
        return ResponseEntity.ok(menuService.findByVisible(visible));
    }

    /**
     * 获取所有根菜单
     * GET /api/rbac/menus/root
     */
    @GetMapping("/root")
    public ResponseEntity<List<MenuDTO>> findRootMenus() {
        return ResponseEntity.ok(menuService.findRootMenus());
    }

    /**
     * 创建菜单
     * POST /api/rbac/menus
     */
    @PostMapping
    public ResponseEntity<MenuDTO> create(@RequestBody MenuDTO menuDTO) {
        return ResponseEntity.ok(menuService.create(menuDTO));
    }

    /**
     * 更新菜单
     * PUT /api/rbac/menus/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MenuDTO> update(@PathVariable Integer id, @RequestBody MenuDTO menuDTO) {
        return ResponseEntity.ok(menuService.update(id, menuDTO));
    }

    /**
     * 删除菜单
     * DELETE /api/rbac/menus/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
