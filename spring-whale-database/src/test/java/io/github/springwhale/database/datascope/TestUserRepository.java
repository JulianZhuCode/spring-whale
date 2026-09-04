package io.github.springwhale.database.datascope;

import io.github.springwhale.database.datascope.annotation.SkipSqlInspector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestUserRepository extends JpaRepository<TestUser, Long>, JpaSpecificationExecutor<TestUser> {

    List<TestUser> findByName(String name);

    List<TestUser> findByDeptId(Long deptId);

    @Query("SELECT u FROM TestUser u WHERE u.deptId = :deptId")
    List<TestUser> findByDeptIdQuery(Long deptId);

    @Query("SELECT u FROM TestUser u WHERE u.createBy = :createBy")
    List<TestUser> findByCreateByQuery(Long createBy);

    @SkipSqlInspector
    @Query("SELECT u FROM TestUser u WHERE u.deptId = :deptId OR u.createBy = :createBy")
    List<TestUser> findByDeptIdOrCreateBy(Long deptId, Long createBy);
}