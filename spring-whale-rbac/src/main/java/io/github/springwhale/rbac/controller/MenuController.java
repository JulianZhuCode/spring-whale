package io.github.springwhale.rbac.controller;

import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public Page<MenuVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return menuService.findAll(pageable);
    }

    /**
     * 根据ID查询菜单
     * GET /api/rbac/menus/{id}
     */
    @GetMapping("/{id}")
    public MenuVO findById(@PathVariable Integer id) {
        return menuService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + id));
    }

    /**
     * 根据菜单编码精确查询
     * GET /api/rbac/menus/by-code?code=system:user
     */
    @GetMapping("/by-code")
    public MenuVO findByCode(@RequestParam String code) {
        return menuService.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + code));
    }

    /**
     * 根据父菜单ID查询
     * GET /api/rbac/menus/by-parent?parentId=0
     */
    @GetMapping("/by-parent")
    public List<MenuVO> findByParentId(@RequestParam Integer parentId) {
        return menuService.findByParentId(parentId);
    }

    /**
     * 搜索菜单（模糊查询）
     * GET /api/rbac/menus/search?keyword=用户
     */
    @GetMapping("/search")
    public List<MenuVO> search(@RequestParam String keyword) {
        return menuService.search(keyword);
    }

    /**
     * 根据类型查询
     * GET /api/rbac/menus/by-type?type=2
     */
    @GetMapping("/by-type")
    public List<MenuVO> findByType(@RequestParam Integer type) {
        return menuService.findByType(type);
    }

    /**
     * 根据状态查询
     * GET /api/rbac/menus/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<MenuVO> findByStatus(@RequestParam Integer status) {
        return menuService.findByStatus(status);
    }

    /**
     * 根据可见性查询
     * GET /api/rbac/menus/by-visible?visible=1
     */
    @GetMapping("/by-visible")
    public List<MenuVO> findByVisible(@RequestParam Integer visible) {
        return menuService.findByVisible(visible);
    }

    /**
     * 获取所有根菜单
     * GET /api/rbac/menus/root
     */
    @GetMapping("/root")
    public List<MenuVO> findRootMenus() {
        return menuService.findRootMenus();
    }

    /**
     * 创建菜单
     * POST /api/rbac/menus
     */
    @PostMapping
    public MenuVO create(@RequestBody MenuVO menuVO) {
        return menuService.create(menuVO);
    }

    /**
     * 更新菜单
     * PUT /api/rbac/menus/{id}
     */
    @PutMapping("/{id}")
    public MenuVO update(@PathVariable Integer id, @RequestBody MenuVO menuVO) {
        return menuService.update(id, menuVO);
    }

    /**
     * 删除菜单
     * DELETE /api/rbac/menus/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        menuService.delete(id);
    }
}
