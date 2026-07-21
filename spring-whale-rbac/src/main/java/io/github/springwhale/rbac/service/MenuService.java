package io.github.springwhale.rbac.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.request.MenuRequest;
import io.github.springwhale.rbac.dto.vo.MenuVO;
import io.github.springwhale.rbac.entity.MenuEntity;
import io.github.springwhale.rbac.mapper.MenuMapper;
import io.github.springwhale.rbac.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Menu service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    /**
     * Find all menus with pagination
     */
    public Page<MenuVO> findAll(Pageable pageable) {
        return menuRepository.findAll(pageable).map(menuMapper::toVO);
    }

    /**
     * Find menus with filter
     */
    public Page<MenuVO> findWithFilter(String keyword, Integer type, Integer status, Pageable pageable) {
        Page<MenuVO> page;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasType = type != null;
        boolean hasStatus = status != null;

        if (hasKeyword && hasType && hasStatus) {
            page = menuRepository.findByCodeContainingOrNameContainingOrPathContainingAndTypeAndStatus(
                    keyword, keyword, keyword, type, status, pageable).map(menuMapper::toVO);
        } else if (hasKeyword && hasType) {
            page = menuRepository.findByCodeContainingOrNameContainingOrPathContainingAndType(
                    keyword, keyword, keyword, type, pageable).map(menuMapper::toVO);
        } else if (hasKeyword && hasStatus) {
            page = menuRepository.findByCodeContainingOrNameContainingOrPathContainingAndStatus(
                    keyword, keyword, keyword, status, pageable).map(menuMapper::toVO);
        } else if (hasType && hasStatus) {
            page = menuRepository.findByTypeAndStatus(type, status, pageable).map(menuMapper::toVO);
        } else if (hasKeyword) {
            page = menuRepository.findByCodeContainingOrNameContainingOrPathContaining(
                    keyword, keyword, keyword, pageable).map(menuMapper::toVO);
        } else if (hasType) {
            page = menuRepository.findByType(type, pageable).map(menuMapper::toVO);
        } else if (hasStatus) {
            page = menuRepository.findByStatus(status, pageable).map(menuMapper::toVO);
        } else {
            page = menuRepository.findAll(pageable).map(menuMapper::toVO);
        }
        return page;
    }

    /**
     * Find menu by ID
     */
    public Optional<MenuVO> findById(Integer id) {
        return menuRepository.findById(id).map(menuMapper::toVO);
    }

    /**
     * Find menu by exact code
     */
    public Optional<MenuVO> findByCode(String code) {
        return menuRepository.findByCode(code).map(menuMapper::toVO);
    }

    /**
     * Find menus by parent ID
     */
    public List<MenuVO> findByParentId(Integer parentId) {
        return menuMapper.toVOList(menuRepository.findByParentId(parentId));
    }

    /**
     * Search menus by name (fuzzy)
     */
    public List<MenuVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return menuMapper.toVOList(menuRepository.findByNameContaining(keyword));
    }

    /**
     * Find by type
     */
    public List<MenuVO> findByType(Integer type) {
        return menuMapper.toVOList(menuRepository.findByType(type));
    }

    /**
     * Find by status
     */
    public List<MenuVO> findByStatus(Integer status) {
        return menuMapper.toVOList(menuRepository.findByStatus(status));
    }

    /**
     * Find by visibility
     */
    public List<MenuVO> findByVisible(Integer visible) {
        return menuMapper.toVOList(menuRepository.findByVisible(visible));
    }

    /**
     * Get all root menus (parentId is null)
     */
    public List<MenuVO> findRootMenus() {
        return menuMapper.toVOList(menuRepository.findByParentId(null));
    }

    /**
     * Create menu
     */
    @Transactional
    public MenuVO create(MenuRequest request) {
        MenuEntity entity = new MenuEntity();
        entity.setParentId(request.getParentId());
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setPath(request.getPath());
        entity.setComponent(request.getComponent());
        entity.setPermission(request.getPermission());
        entity.setIcon(request.getIcon());
        entity.setSort(request.getSort());
        entity.setVisible(request.getVisible());
        entity.setStatus(request.getStatus());
        return menuMapper.toVO(menuRepository.save(entity));
    }

    /**
     * Update menu
     */
    @Transactional
    public MenuVO update(Integer id, MenuRequest request) {
        MenuEntity menu = menuRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "Menu not found, ID: " + id));

        menu.setParentId(request.getParentId());
        menu.setCode(request.getCode());
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setIcon(request.getIcon());
        menu.setSort(request.getSort());
        menu.setVisible(request.getVisible());
        menu.setStatus(request.getStatus());

        return menuMapper.toVO(menuRepository.save(menu));
    }

    /**
     * Delete menu
     */
    @Transactional
    public void delete(Integer id) {
        MenuEntity menu = menuRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "Menu not found, ID: " + id));
        menuRepository.delete(menu);
    }
}
