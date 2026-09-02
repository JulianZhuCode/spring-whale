package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.platform.rbac.dao.entity.RoleMenuEntity;
import io.github.springwhale.platform.rbac.repository.RoleMenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Role-menu association service
 */
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;

    /**
     * Get menu IDs assigned to a role
     */
    public List<Integer> getMenuIdsByRoleId(Integer roleId) {
        return roleMenuRepository.findByRoleId(roleId).stream()
                .map(RoleMenuEntity::getMenuId)
                .toList();
    }

    /**
     * Batch add menus to a role — skips already existing associations
     */
    @Transactional
    public void addMenus(Integer roleId, List<Integer> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Integer menuId : menuIds) {
            if (roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId).isEmpty()) {
                RoleMenuEntity entity = new RoleMenuEntity();
                entity.setRoleId(roleId);
                entity.setMenuId(menuId);
                roleMenuRepository.save(entity);
            }
        }
    }

    /**
     * Batch remove menus from a role — only removes existing associations
     */
    @Transactional
    public void removeMenus(Integer roleId, List<Integer> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Integer menuId : menuIds) {
            roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId)
                    .ifPresent(roleMenuRepository::delete);
        }
    }
}