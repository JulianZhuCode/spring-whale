package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色数据访问接口
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {

    /**
     * 根据角色编码精确查询
     */
    Optional<RoleEntity> findByCode(String code);

    /**
     * 根据角色名称模糊查询
     */
    List<RoleEntity> findByNameContaining(String name);

    /**
     * 根据状态查询
     */
    List<RoleEntity> findByStatus(Integer status);
}
