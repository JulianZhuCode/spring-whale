package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.MenuEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Search with keyword, with pagination
     */
    Page<MenuEntity> findByCodeContainingOrNameContainingOrPathContaining(
            String code, String name, String path, Pageable pageable);

    /**
     * Search with keyword and type, with pagination
     */
    Page<MenuEntity> findByCodeContainingOrNameContainingOrPathContainingAndType(
            String code, String name, String path, Integer type, Pageable pageable);

    /**
     * Search with keyword and status, with pagination
     */
    Page<MenuEntity> findByCodeContainingOrNameContainingOrPathContainingAndStatus(
            String code, String name, String path, Integer status, Pageable pageable);

    /**
     * Search with keyword, type and status, with pagination
     */
    Page<MenuEntity> findByCodeContainingOrNameContainingOrPathContainingAndTypeAndStatus(
            String code, String name, String path, Integer type, Integer status, Pageable pageable);

    /**
     * Find by type with pagination
     */
    Page<MenuEntity> findByType(Integer type, Pageable pageable);

    /**
     * Find by status with pagination
     */
    Page<MenuEntity> findByStatus(Integer status, Pageable pageable);

    /**
     * Find by type and status with pagination
     */
    Page<MenuEntity> findByTypeAndStatus(Integer type, Integer status, Pageable pageable);
}
