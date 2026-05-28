package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.RoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色菜单关联数据访问接口
 */
@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenuEntity, Integer> {

    /**
     * 根据角色ID查询所有菜单关联
     */
    List<RoleMenuEntity> findByRoleId(Integer roleId);

    /**
     * 根据菜单ID查询所有角色关联
     */
    List<RoleMenuEntity> findByMenuId(Integer menuId);

    /**
     * 根据角色ID和菜单ID查询
     */
    Optional<RoleMenuEntity> findByRoleIdAndMenuId(Integer roleId, Integer menuId);

    /**
     * 删除角色的所有菜单关联
     */
    void deleteByRoleId(Integer roleId);

    /**
     * 删除菜单的所有角色关联
     */
    void deleteByMenuId(Integer menuId);
}
