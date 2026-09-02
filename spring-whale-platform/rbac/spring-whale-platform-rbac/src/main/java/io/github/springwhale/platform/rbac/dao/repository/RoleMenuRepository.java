package io.github.springwhale.platform.rbac.dao.repository;

import io.github.springwhale.platform.rbac.dao.entity.RoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Role-menu association repository
 */
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

    /**
     * Find role IDs that have access to a menu by its code.
     * Single query: JOIN rbac_role_menu + rbac_menu.
     */
    @Query("SELECT rm.roleId FROM RoleMenuEntity rm JOIN MenuEntity m ON rm.menuId = m.id WHERE m.code = :code")
    List<Integer> findRoleIdsByMenuCode(@Param("code") String code);
}