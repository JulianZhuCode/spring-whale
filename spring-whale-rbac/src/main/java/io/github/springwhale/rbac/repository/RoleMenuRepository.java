package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.RoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role-menu association repository
 */
@Repository
public interface RoleMenuRepository extends JpaRepository<RoleMenuEntity, Integer> {

    /**
     * Find all menu associations by role ID
     */
    List<RoleMenuEntity> findByRoleId(Integer roleId);

    /**
     * Find all role associations by menu ID
     */
    List<RoleMenuEntity> findByMenuId(Integer menuId);

    /**
     * Find by role ID and menu ID
     */
    Optional<RoleMenuEntity> findByRoleIdAndMenuId(Integer roleId, Integer menuId);

    /**
     * Delete all menu associations for role
     */
    void deleteByRoleId(Integer roleId);

    /**
     * Delete all role associations for menu
     */
    void deleteByMenuId(Integer menuId);
}
