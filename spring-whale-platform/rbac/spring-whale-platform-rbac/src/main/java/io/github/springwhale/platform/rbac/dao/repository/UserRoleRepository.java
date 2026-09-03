package io.github.springwhale.platform.rbac.dao.repository;

import io.github.springwhale.platform.rbac.dao.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * User-role association repository
 */
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Integer> {

    /**
     * Find all role associations by user ID
     */
    List<UserRoleEntity> findByUserId(Integer userId);

    /**
     * Find all user associations by role ID
     */
    List<UserRoleEntity> findByRoleId(Integer roleId);

    /**
     * Find a specific user-role association
     */
    Optional<UserRoleEntity> findByUserIdAndRoleId(Integer userId, Integer roleId);
}