package io.github.springwhale.platform.rbac.repository;

import io.github.springwhale.platform.rbac.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.util.List;
import java.util.Optional;

/**
 * Menu repository
 */
public interface MenuRepository extends JpaRepository<MenuEntity, Integer>, JpaSpecificationExecutor<MenuEntity> {

    /**
     * Find menu by exact code
     */
    Optional<MenuEntity> findByCode(String code);

    /**
     * Find menus by parent ID
     */
    List<MenuEntity> findByParentId(Integer parentId);

    /**
     * Find by menu name (fuzzy)
     */
    List<MenuEntity> findByNameContaining(String name);

    /**
     * Find by type
     */
    List<MenuEntity> findByType(Integer type);

    /**
     * Find by status
     */
    List<MenuEntity> findByStatus(Integer status);

    /**
     * Find by visibility
     */
    List<MenuEntity> findByVisible(Integer visible);
}