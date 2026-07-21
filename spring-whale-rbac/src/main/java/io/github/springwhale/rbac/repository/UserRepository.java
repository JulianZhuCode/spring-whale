package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User repository
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer>, JpaSpecificationExecutor<UserEntity> {

    /**
     * Find by exact username
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Find by exact email
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Find by exact phone
     */
    Optional<UserEntity> findByPhone(String phone);

    /**
     * Find by username (fuzzy)
     */
    List<UserEntity> findByUsernameContaining(String username);

    /**
     * Find by real name (fuzzy)
     */
    List<UserEntity> findByRealNameContaining(String realName);

    /**
     * Find users by department ID
     */
    List<UserEntity> findByGroupId(Integer groupId);

    /**
     * Find by status
     */
    List<UserEntity> findByStatus(Integer status);
}