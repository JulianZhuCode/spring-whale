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
public interface RoleMenuRepository extends JpaRepository<RoleMenuEntity, Long> {

    /**
     * Find all menu associations by role ID
     */
    List<RoleMenuEntity> findByRoleId(Long roleId);

    /**
     * Find all menu associations by a list of role IDs
     */
    List<RoleMenuEntity> findByRoleIdIn(List<Long> roleIds);

    /**
     * Find by role ID and menu ID
     */
    Optional<RoleMenuEntity> findByRoleIdAndMenuId(Long roleId, Long menuId);

    int deleteByRoleIdAndMenuIdIn(Long roleId, List<Long> menuIds);

    /**
     * Find role IDs that have access to a menu by its code.
     * Single query: JOIN rbac_role_menu + rbac_menu.
     */
    @Query("SELECT rm.roleId FROM RoleMenuEntity rm JOIN MenuEntity m ON rm.menuId = m.id WHERE m.code = :code")
    List<Long> findRoleIdsByMenuCode(@Param("code") String code);
}