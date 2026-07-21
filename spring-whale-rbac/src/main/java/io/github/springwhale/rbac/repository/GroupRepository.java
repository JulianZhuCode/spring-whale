package io.github.springwhale.rbac.repository;

import io.github.springwhale.rbac.entity.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Group (department) repository
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Integer> {

    /**
     * Find department by exact code
     */
    Optional<GroupEntity> findByCode(String code);

    /**
     * Find departments by parent ID
     */
    List<GroupEntity> findByParentId(Integer parentId);

    /**
     * Find by department name (fuzzy)
     */
    List<GroupEntity> findByNameContaining(String name);

    /**
     * Find by status
     */
    List<GroupEntity> findByStatus(Integer status);

    /**
     * Find all groups by IDs (batch query)
     */
    List<GroupEntity> findAllByIdIn(List<Integer> ids);

    /**
     * Search with keyword and status, with pagination
     */
    Page<GroupEntity> findByCodeContainingOrNameContainingOrLeaderContaining(
            String code, String name, String leader, Pageable pageable);

    /**
     * Search with keyword, status, with pagination
     */
    Page<GroupEntity> findByCodeContainingOrNameContainingOrLeaderContainingAndStatus(
            String code, String name, String leader, Integer status, Pageable pageable);

    /**
     * Find by status with pagination
     */
    Page<GroupEntity> findByStatus(Integer status, Pageable pageable);
}
