package io.github.springwhale.platform.rbac.dao.repository;

import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * User-role association repository
 */
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {

    /**
     * Find all role associations by user ID
     */
    List<UserRoleEntity> findByUserId(Long userId);

    /**
     * Find all user associations by role ID
     */
    List<UserRoleEntity> findByRoleId(Long roleId);

    /**
     * Find a specific user-role association
     */
    Optional<UserRoleEntity> findByUserIdAndRoleId(Long userId, Long roleId);
}