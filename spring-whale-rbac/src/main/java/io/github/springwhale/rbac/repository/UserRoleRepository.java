package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User-role association repository
 */
@Repository
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
     * Find by user ID and role ID
     */
    Optional<UserRoleEntity> findByUserIdAndRoleId(Integer userId, Integer roleId);

    /**
     * Delete all role associations for user
     */
    void deleteByUserId(Integer userId);

    /**
     * Delete all user associations for role
     */
    void deleteByRoleId(Integer roleId);
}
