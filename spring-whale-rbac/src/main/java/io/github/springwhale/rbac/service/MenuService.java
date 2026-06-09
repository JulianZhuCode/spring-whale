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
 * 菜单服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    /**
     * 分页查询所有菜单
     */
    public Page<MenuVO> findAll(Pageable pageable) {
        return menuRepository.findAll(pageable).map(menuMapper::toVO);
    }

    /**
     * 根据ID查询菜单
     */
    public Optional<MenuVO> findById(Integer id) {
        return menuRepository.findById(id).map(menuMapper::toVO);
    }

    /**
     * 根据菜单编码精确查询
     */
    public Optional<MenuVO> findByCode(String code) {
        return menuRepository.findByCode(code).map(menuMapper::toVO);
    }

    /**
     * 根据父菜单ID查询
     */
    public List<MenuVO> findByParentId(Integer parentId) {
        return menuMapper.toVOList(menuRepository.findByParentId(parentId));
    }

    /**
     * 搜索菜单（支持菜单名称模糊查询）
     */
    public List<MenuVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return menuMapper.toVOList(menuRepository.findByNameContaining(keyword));
    }

    /**
     * 根据类型查询
     */
    public List<MenuVO> findByType(Integer type) {
        return menuMapper.toVOList(menuRepository.findByType(type));
    }

    /**
     * 根据状态查询
     */
    public List<MenuVO> findByStatus(Integer status) {
        return menuMapper.toVOList(menuRepository.findByStatus(status));
    }

    /**
     * 根据可见性查询
     */
    public List<MenuVO> findByVisible(Integer visible) {
        return menuMapper.toVOList(menuRepository.findByVisible(visible));
    }

    /**
     * 获取所有根菜单（parentId为null）
     */
    public List<MenuVO> findRootMenus() {
        return menuMapper.toVOList(menuRepository.findByParentId(null));
    }

    /**
     * 创建菜单
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
     * 更新菜单
     */
    @Transactional
    public MenuVO update(Integer id, MenuRequest request) {
        MenuEntity menu = menuRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "菜单不存在，ID: " + id));

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
     * 删除菜单
     */
    @Transactional
    public void delete(Integer id) {
        MenuEntity menu = menuRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("MENU_NOT_FOUND", "菜单不存在，ID: " + id));
        menuRepository.delete(menu);
    }
}
