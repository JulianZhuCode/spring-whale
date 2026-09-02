package io.github.springwhale.platform.rbac.controller;

import io.github.springwhale.database.SortUtils;
import io.github.springwhale.database.datascope.DataScope;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dto.request.MenuRequest;
import io.github.springwhale.platform.rbac.dto.vo.MenuVO;
import io.github.springwhale.platform.rbac.enums.MenuType;
import io.github.springwhale.platform.rbac.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAnyAuthority('rbac:menu', '*')")
    @DataScope(module = "rbac")
    @GetMapping
    public Page<MenuVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MenuType type,
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
    @PreAuthorize("hasAnyAuthority('rbac:menu', '*')")
    @DataScope(module = "rbac:menu")
    @GetMapping("/{id}")
    public MenuVO findById(@PathVariable Integer id) {
        return menuService.findById(id)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "Menu not found: " + id));
    }

    /**
     * Create menu
     * POST /api/rbac/menus
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu:create', '*')")
    @DataScope(module = "rbac:menu")
    @PostMapping
    public MenuVO create(@Valid @RequestBody MenuRequest request) {
        return menuService.create(request);
    }

    /**
     * Update menu
     * PUT /api/rbac/menus/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu:update', '*')")
    @DataScope(module = "rbac:menu")
    @PutMapping("/{id}")
    public MenuVO update(@PathVariable Integer id, @Valid @RequestBody MenuRequest request) {
        return menuService.update(id, request);
    }

    /**
     * Delete menu
     * DELETE /api/rbac/menus/{id}
     */
    @PreAuthorize("hasAnyAuthority('rbac:menu:delete', '*')")
    @DataScope(module = "rbac:menu")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        menuService.delete(id);
    }
}