package io.github.springwhale.rbac.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.rbac.dto.vo.RoleMenuVO;
import io.github.springwhale.rbac.entity.RoleMenuEntity;
import io.github.springwhale.rbac.mapper.RoleMenuMapper;
import io.github.springwhale.rbac.repository.RoleMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Role-menu association service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;
    private final RoleMenuMapper roleMenuMapper;

    /**
     * Find all menu associations by role ID
     */
    public List<RoleMenuVO> findByRoleId(Integer roleId) {
        List<RoleMenuEntity> entities = roleMenuRepository.findByRoleId(roleId);
        return roleMenuMapper.toVOList(entities);
    }

    /**
     * Find all role associations by menu ID
     */
    public List<RoleMenuVO> findByMenuId(Integer menuId) {
        List<RoleMenuEntity> entities = roleMenuRepository.findByMenuId(menuId);
        return roleMenuMapper.toVOList(entities);
    }

    /**
     * Assign menu to role
     */
    @Transactional
    public void assignMenuToRole(Integer roleId, Integer menuId) {
        // Check if already exists
        if (roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId).isPresent()) {
            throw BusinessException.create("ROLE_MENU_ALREADY_EXISTS", "Role already has this menu permission");
        }

        RoleMenuEntity roleMenu = new RoleMenuEntity();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        roleMenuRepository.save(roleMenu);
    }

    /**
     * Batch assign menus to role
     */
    @Transactional
    public void assignMenusToRole(Integer roleId, List<Integer> menuIds) {
        // Remove all existing menu associations for role first
        roleMenuRepository.deleteByRoleId(roleId);

        // Reassign
        for (Integer menuId : menuIds) {
            RoleMenuEntity roleMenu = new RoleMenuEntity();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuRepository.save(roleMenu);
        }
    }

    /**
     * Remove menu permission from role
     */
    @Transactional
    public void removeMenuFromRole(Integer roleId, Integer menuId) {
        RoleMenuEntity roleMenu = roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId)
                .orElseThrow(() -> BusinessException.create("ROLE_MENU_NOT_FOUND", "Role-menu association not found"));
        roleMenuRepository.delete(roleMenu);
    }

    /**
     * Remove all menu permissions from role
     */
    @Transactional
    public void removeAllMenusFromRole(Integer roleId) {
        roleMenuRepository.deleteByRoleId(roleId);
    }
}
