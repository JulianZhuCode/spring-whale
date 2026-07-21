package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role repository
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {

    /**
     * Find role by exact code
     */
    Optional<RoleEntity> findByCode(String code);

    /**
     * Find by role name (fuzzy)
     */
    List<RoleEntity> findByNameContaining(String name);

    /**
     * Find by status
     */
    List<RoleEntity> findByStatus(Integer status);

    /**
     * Search with keyword and status, with pagination
     */
    Page<RoleEntity> findByCodeContainingOrNameContainingOrDescriptionContaining(
            String code, String name, String description, Pageable pageable);

    /**
     * Search with keyword, status, with pagination
     */
    Page<RoleEntity> findByCodeContainingOrNameContainingOrDescriptionContainingAndStatus(
            String code, String name, String description, Integer status, Pageable pageable);

    /**
     * Find by status with pagination
     */
    Page<RoleEntity> findByStatus(Integer status, Pageable pageable);
}
