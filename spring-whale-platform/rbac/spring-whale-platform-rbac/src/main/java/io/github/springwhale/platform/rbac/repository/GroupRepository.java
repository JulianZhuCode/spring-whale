package io.github.springwhale.platform.rbac.repository;

import io.github.springwhale.platform.rbac.dao.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Group (department) repository
 */
public interface GroupRepository extends JpaRepository<GroupEntity, Integer>, JpaSpecificationExecutor<GroupEntity> {

    /**
     * Find department by exact code
     */
    Optional<GroupEntity> findByCode(String code);

    /**
     * Find all groups by IDs (batch query)
     */
    List<GroupEntity> findAllByIdIn(List<Integer> ids);
}