package io.github.springwhale.platform.rbac.service;

import io.github.springwhale.platform.rbac.dao.entity.RoleMenuEntity;
import io.github.springwhale.platform.rbac.dao.repository.RoleMenuRepository;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.platform.rbac.event.RoleChangedEvent;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Role-menu association service
 */
@Transactional(readOnly = true)
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;
    private final EventPublisher eventPublisher;

    public RoleMenuService(RoleMenuRepository roleMenuRepository,
                           EventPublisher eventPublisher) {
        this.roleMenuRepository = roleMenuRepository;
        this.eventPublisher = eventPublisher;
    }

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
        boolean changed = false;
        for (Integer menuId : menuIds) {
            if (roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId).isEmpty()) {
                RoleMenuEntity entity = new RoleMenuEntity();
                entity.setRoleId(roleId);
                entity.setMenuId(menuId);
                roleMenuRepository.save(entity);
                changed = true;
            }
        }
        if (changed) {
            eventPublisher.publishAfterCommit(new RoleChangedEvent(roleId));
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
        boolean changed = false;
        for (Integer menuId : menuIds) {
            Optional<RoleMenuEntity> existing = roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId);
            if (existing.isPresent()) {
                roleMenuRepository.delete(existing.get());
                changed = true;
            }
        }
        if (changed) {
            eventPublisher.publishAfterCommit(new RoleChangedEvent(roleId));
        }
    }
}