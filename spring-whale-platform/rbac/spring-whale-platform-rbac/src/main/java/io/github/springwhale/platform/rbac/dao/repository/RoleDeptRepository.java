package io.github.springwhale.platform.rbac.dao.repository;

import io.github.springwhale.platform.rbac.dao.entity.RoleDeptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleDeptRepository extends JpaRepository<RoleDeptEntity, Long> {

    List<RoleDeptEntity> findByRoleId(Long roleId);

    List<RoleDeptEntity> findByGroupId(Long groupId);

    Optional<RoleDeptEntity> findByRoleIdAndGroupId(Long roleId, Long groupId);
}