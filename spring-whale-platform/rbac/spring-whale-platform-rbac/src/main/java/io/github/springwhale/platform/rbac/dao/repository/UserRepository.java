package io.github.springwhale.platform.rbac.dao.repository;

import io.github.springwhale.platform.rbac.dao.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * User repository
 */
public interface UserRepository extends JpaRepository<UserEntity, Integer>, JpaSpecificationExecutor<UserEntity> {

    /**
     * Find by exact username
     */
    Optional<UserEntity> findByUsername(String username);
}