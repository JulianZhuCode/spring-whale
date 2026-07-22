package io.github.springwhale.rbac.controller;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.database.SortUtils;
import io.github.springwhale.rbac.dto.request.MenuRequest;
import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu controller
 */
@RestController
@RequestMapping("/api/rbac/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * Find all menus with pagination and filter
     * GET /api/rbac/menus?page=0&size=20&keyword=&type=&status=
     */
    @GetMapping
    public Page<MenuVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sort) {
        Sort sortObj = SortUtils.buildSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return menuService.findWithFilter(keyword, type, status, pageable);
    }

    /**
     * Find menu by ID
     * GET /api/rbac/menus/{id}
     */
    @GetMapping("/{id}")
    public MenuVO findById(@PathVariable Integer id) {
        return menuService.findById(id)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "Menu not found: " + id));
    }

    /**
     * Find menu by exact code
     * GET /api/rbac/menus/by-code?code=system:user
     */
    @GetMapping("/by-code")
    public MenuVO findByCode(@RequestParam String code) {
        return menuService.findByCode(code)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "Menu not found: " + code));
    }

    /**
     * Find menus by parent ID
     * GET /api/rbac/menus/by-parent?parentId=0
     */
    @GetMapping("/by-parent")
    public List<MenuVO> findByParentId(@RequestParam Integer parentId) {
        return menuService.findByParentId(parentId);
    }

    /**
     * Search menus (fuzzy)
     * GET /api/rbac/menus/search?keyword=user
     */
    @GetMapping("/search")
    public List<MenuVO> search(@RequestParam String keyword) {
        return menuService.search(keyword);
    }

    /**
     * Find by type
     * GET /api/rbac/menus/by-type?type=2
     */
    @GetMapping("/by-type")
    public List<MenuVO> findByType(@RequestParam Integer type) {
        return menuService.findByType(type);
    }

    /**
     * Find by status
     * GET /api/rbac/menus/by-status?status=1
     */
    @GetMapping("/by-status")
    public List<MenuVO> findByStatus(@RequestParam Integer status) {
        return menuService.findByStatus(status);
    }

    /**
     * Find by visibility
     * GET /api/rbac/menus/by-visible?visible=1
     */
    @GetMapping("/by-visible")
    public List<MenuVO> findByVisible(@RequestParam Integer visible) {
        return menuService.findByVisible(visible);
    }

    /**
     * Get all root menus
     * GET /api/rbac/menus/root
     */
    @GetMapping("/root")
    public List<MenuVO> findRootMenus() {
        return menuService.findRootMenus();
    }

    /**
     * Create menu
     * POST /api/rbac/menus
     */
    @PostMapping
    public MenuVO create(@Valid @RequestBody MenuRequest request) {
        return menuService.create(request);
    }

    /**
     * Update menu
     * PUT /api/rbac/menus/{id}
     */
    @PutMapping("/{id}")
    public MenuVO update(@PathVariable Integer id, @Valid @RequestBody MenuRequest request) {
        return menuService.update(id, request);
    }

    /**
     * Delete menu
     * DELETE /api/rbac/menus/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        menuService.delete(id);
    }
}
