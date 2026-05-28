package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分组（部门）数据访问接口
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Integer> {

    /**
     * 根据部门编码精确查询
     */
    Optional<GroupEntity> findByCode(String code);

    /**
     * 根据父部门ID查询
     */
    List<GroupEntity> findByParentId(Integer parentId);

    /**
     * 根据部门名称模糊查询
     */
    List<GroupEntity> findByNameContaining(String name);

    /**
     * 根据状态查询
     */
    List<GroupEntity> findByStatus(Integer status);
}
