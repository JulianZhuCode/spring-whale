package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Menu repository
 */
@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Integer> {

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
