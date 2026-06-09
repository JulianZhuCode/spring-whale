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
 * 角色菜单关联服务
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleMenuService {

    private final RoleMenuRepository roleMenuRepository;
    private final RoleMenuMapper roleMenuMapper;

    /**
     * 根据角色ID查询所有菜单关联
     */
    public List<RoleMenuVO> findByRoleId(Integer roleId) {
        List<RoleMenuEntity> entities = roleMenuRepository.findByRoleId(roleId);
        return roleMenuMapper.toVOList(entities);
    }

    /**
     * 根据菜单ID查询所有角色关联
     */
    public List<RoleMenuVO> findByMenuId(Integer menuId) {
        List<RoleMenuEntity> entities = roleMenuRepository.findByMenuId(menuId);
        return roleMenuMapper.toVOList(entities);
    }

    /**
     * 为角色分配菜单
     */
    @Transactional
    public void assignMenuToRole(Integer roleId, Integer menuId) {
        // 检查是否已存在
        if (roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId).isPresent()) {
            throw BusinessException.create("ROLE_MENU_ALREADY_EXISTS", "角色已拥有该菜单权限");
        }
        
        RoleMenuEntity roleMenu = new RoleMenuEntity();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        roleMenuRepository.save(roleMenu);
    }

    /**
     * 批量为角色分配菜单
     */
    @Transactional
    public void assignMenusToRole(Integer roleId, List<Integer> menuIds) {
        // 先删除角色的所有菜单关联
        roleMenuRepository.deleteByRoleId(roleId);
        
        // 重新分配
        for (Integer menuId : menuIds) {
            RoleMenuEntity roleMenu = new RoleMenuEntity();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuRepository.save(roleMenu);
        }
    }

    /**
     * 移除角色的菜单权限
     */
    @Transactional
    public void removeMenuFromRole(Integer roleId, Integer menuId) {
        RoleMenuEntity roleMenu = roleMenuRepository.findByRoleIdAndMenuId(roleId, menuId)
                .orElseThrow(() -> BusinessException.create("ROLE_MENU_NOT_FOUND", "角色菜单关联不存在"));
        roleMenuRepository.delete(roleMenu);
    }

    /**
     * 移除角色的所有菜单权限
     */
    @Transactional
    public void removeAllMenusFromRole(Integer roleId) {
        roleMenuRepository.deleteByRoleId(roleId);
    }
}
