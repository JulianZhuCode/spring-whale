package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关联数据访问接口
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Integer> {

    /**
     * 根据用户ID查询所有角色关联
     */
    List<UserRoleEntity> findByUserId(Integer userId);

    /**
     * 根据角色ID查询所有用户关联
     */
    List<UserRoleEntity> findByRoleId(Integer roleId);

    /**
     * 根据用户ID和角色ID查询
     */
    Optional<UserRoleEntity> findByUserIdAndRoleId(Integer userId, Integer roleId);

    /**
     * 删除用户的所有角色关联
     */
    void deleteByUserId(Integer userId);

    /**
     * 删除角色的所有用户关联
     */
    void deleteByRoleId(Integer roleId);
}
