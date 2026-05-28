package io.github.springwhale.rbac.service;

import io.github.springwhale.rbac.dto.MenuDTO;
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
    public Page<MenuDTO> findAll(Pageable pageable) {
        return menuRepository.findAll(pageable).map(menuMapper::toDTO);
    }

    /**
     * 根据ID查询菜单
     */
    public Optional<MenuDTO> findById(Integer id) {
        return menuRepository.findById(id).map(menuMapper::toDTO);
    }

    /**
     * 根据菜单编码精确查询
     */
    public Optional<MenuDTO> findByCode(String code) {
        return menuRepository.findByCode(code).map(menuMapper::toDTO);
    }

    /**
     * 根据父菜单ID查询
     */
    public List<MenuDTO> findByParentId(Integer parentId) {
        return menuMapper.toDTOList(menuRepository.findByParentId(parentId));
    }

    /**
     * 搜索菜单（支持菜单名称模糊查询）
     */
    public List<MenuDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return menuMapper.toDTOList(menuRepository.findByNameContaining(keyword));
    }

    /**
     * 根据类型查询
     */
    public List<MenuDTO> findByType(Integer type) {
        return menuMapper.toDTOList(menuRepository.findByType(type));
    }

    /**
     * 根据状态查询
     */
    public List<MenuDTO> findByStatus(Integer status) {
        return menuMapper.toDTOList(menuRepository.findByStatus(status));
    }

    /**
     * 根据可见性查询
     */
    public List<MenuDTO> findByVisible(Integer visible) {
        return menuMapper.toDTOList(menuRepository.findByVisible(visible));
    }

    /**
     * 获取所有根菜单（parentId为null）
     */
    public List<MenuDTO> findRootMenus() {
        return menuMapper.toDTOList(menuRepository.findByParentId(null));
    }

    /**
     * 创建菜单
     */
    @Transactional
    public MenuDTO create(MenuDTO menuDTO) {
        MenuEntity entity = menuMapper.toEntity(menuDTO);
        return menuMapper.toDTO(menuRepository.save(entity));
    }

    /**
     * 更新菜单
     */
    @Transactional
    public MenuDTO update(Integer id, MenuDTO menuDTO) {
        MenuEntity menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜单不存在，ID: " + id));

        menu.setParentId(menuDTO.getParentId());
        menu.setCode(menuDTO.getCode());
        menu.setName(menuDTO.getName());
        menu.setType(menuDTO.getType());
        menu.setPath(menuDTO.getPath());
        menu.setComponent(menuDTO.getComponent());
        menu.setPermission(menuDTO.getPermission());
        menu.setIcon(menuDTO.getIcon());
        menu.setSort(menuDTO.getSort());
        menu.setVisible(menuDTO.getVisible());
        menu.setStatus(menuDTO.getStatus());

        return menuMapper.toDTO(menuRepository.save(menu));
    }

    /**
     * 删除菜单
     */
    @Transactional
    public void delete(Integer id) {
        MenuEntity menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜单不存在，ID: " + id));
        menuRepository.delete(menu);
    }
}
