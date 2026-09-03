package io.github.springwhale.platform.rbac.dao.repository;

import io.github.springwhale.platform.rbac.dao.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Menu repository
 */
public interface MenuRepository extends JpaRepository<MenuEntity, Long>, JpaSpecificationExecutor<MenuEntity> {

    /**
     * Find menu by exact code
     */
    Optional<MenuEntity> findByCode(String code);

    /**
     * Find all child menus by parent ID
     */
    List<MenuEntity> findByParentId(Long parentId);
}