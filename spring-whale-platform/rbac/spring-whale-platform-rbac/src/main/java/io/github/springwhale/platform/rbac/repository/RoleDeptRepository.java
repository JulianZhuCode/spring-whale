package io.github.springwhale.platform.rbac.repository;

import io.github.springwhale.platform.rbac.dao.entity.RoleDeptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleDeptRepository extends JpaRepository<RoleDeptEntity, Integer> {

    List<RoleDeptEntity> findByRoleId(Integer roleId);

    List<RoleDeptEntity> findByRoleIdIn(List<Integer> roleIds);

    List<RoleDeptEntity> findByGroupId(Integer groupId);

    Optional<RoleDeptEntity> findByRoleIdAndGroupId(Integer roleId, Integer groupId);

    void deleteByRoleId(Integer roleId);
}