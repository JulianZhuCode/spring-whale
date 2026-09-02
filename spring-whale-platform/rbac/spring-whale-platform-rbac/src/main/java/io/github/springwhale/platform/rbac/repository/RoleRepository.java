package io.github.springwhale.platform.rbac.repository;

import io.github.springwhale.platform.rbac.dao.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.util.List;
import java.util.Optional;

/**
 * Role repository
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Integer>, JpaSpecificationExecutor<RoleEntity> {

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
}