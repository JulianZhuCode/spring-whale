package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.platform.rbac.dao.entity.MenuEntity;
import io.github.springwhale.platform.rbac.dao.mapper.MenuMapper;
import io.github.springwhale.platform.rbac.dto.request.MenuRequest;
import io.github.springwhale.platform.rbac.dto.vo.MenuTreeVO;
import io.github.springwhale.platform.rbac.dto.vo.MenuVO;
import io.github.springwhale.platform.rbac.enums.MenuType;
import io.github.springwhale.platform.rbac.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu service
 */
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
    public Page<MenuVO> findWithFilter(String keyword, MenuType type, Integer status, Pageable pageable) {
        var spec = JpaQueryWrapper.of(MenuEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(MenuEntity::getCode, keyword)
                        .likeIgnoreCase(MenuEntity::getName, keyword)
                        .likeIgnoreCase(MenuEntity::getPath, keyword))
                .eq(type != null, MenuEntity::getType, type)
                .eq(status != null, MenuEntity::getStatus, status)
                .buildSpec();
        return menuRepository.findAll(spec, pageable).map(menuMapper::toVO);
    }

    /**
     * Find menu by ID
     */
    public Optional<MenuVO> findById(Integer id) {
        return menuRepository.findById(id).map(menuMapper::toVO);
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
        entity.setNameI18nKey(request.getNameI18nKey());
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
        menu.setNameI18nKey(request.getNameI18nKey());
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

    /**
     * Build menu tree filtered by allowed menu IDs.
     * If allowedMenuIds is null, returns all menus.
     */
    public List<MenuTreeVO> buildTree(Set<Integer> allowedMenuIds) {
        List<MenuEntity> all = menuRepository.findAll();

        List<MenuEntity> filtered = all;
        if (allowedMenuIds != null) {
            filtered = all.stream()
                    .filter(m -> allowedMenuIds.contains(m.getId()))
                    .toList();
        }

        Map<Integer, List<MenuTreeVO>> parentMap = filtered.stream()
                .sorted(Comparator.comparing(MenuEntity::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toTreeVO)
                .collect(Collectors.groupingBy(
                        vo -> vo.getParentId() != null ? vo.getParentId() : 0,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<MenuTreeVO> roots = parentMap.getOrDefault(0, List.of());
        for (MenuTreeVO root : roots) {
            buildChildren(root, parentMap);
        }
        return roots;
    }

    private MenuTreeVO toTreeVO(MenuEntity entity) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setType(entity.getType());
        vo.setPermission(entity.getPermission());
        return vo;
    }

    private void buildChildren(MenuTreeVO parent, Map<Integer, List<MenuTreeVO>> parentMap) {
        List<MenuTreeVO> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (MenuTreeVO child : children) {
                buildChildren(child, parentMap);
            }
        }
    }
}