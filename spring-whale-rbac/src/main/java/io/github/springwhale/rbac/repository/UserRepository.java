package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    /**
     * 根据用户名精确查询
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 根据邮箱精确查询
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 根据手机号精确查询
     */
    Optional<UserEntity> findByPhone(String phone);

    /**
     * 根据用户名模糊查询
     */
    List<UserEntity> findByUsernameContaining(String username);

    /**
     * 根据真实姓名模糊查询
     */
    List<UserEntity> findByRealNameContaining(String realName);

    /**
     * 根据部门ID查询
     */
    List<UserEntity> findByGroupId(Integer groupId);

    /**
     * 根据状态查询
     */
    List<UserEntity> findByStatus(Integer status);
}
