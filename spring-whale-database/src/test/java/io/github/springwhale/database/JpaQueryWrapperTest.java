package io.github.springwhale.database;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestEntity {
    private String username;
    private String realName;
    private String email;
    private Integer status;
    private String department;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}

class JpaQueryWrapperTest {

    // ==================== eq / ne ====================

    @Test
    void testEqConditionWithLambda() {
        Integer status = 1;
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(status != null, TestEntity::getStatus, status)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testEqConditionWithString() {
        Integer status = 1;
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(status != null, "status", status)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNeConditionWithLambda() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .ne(TestEntity::getStatus, 0)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNeConditionWithString() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .ne("status", 0)
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== eqIgnoreCase / neIgnoreCase ====================

    @Test
    void testEqIgnoreCaseWithLambda() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eqIgnoreCase(TestEntity::getUsername, "ADMIN")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testEqIgnoreCaseWithString() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eqIgnoreCase("username", "ADMIN")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testEqIgnoreCaseConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eqIgnoreCase(false, TestEntity::getUsername, "ADMIN")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNeIgnoreCaseWithLambda() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .neIgnoreCase(TestEntity::getUsername, "admin")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNeIgnoreCaseWithString() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .neIgnoreCase("username", "admin")
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== gt / ge / lt / le ====================

    @Test
    void testGtCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .gt(TestEntity::getStatus, 0)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testGeCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .ge(TestEntity::getStatus, 1)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testLtCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .lt(TestEntity::getStatus, 100)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testLeCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .le(TestEntity::getStatus, 99)
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== like / likeIgnoreCase / likeLeft / likeRight ====================

    @Test
    void testLikeCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .like(TestEntity::getUsername, "admin")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testLikeIgnoreCaseCondition() {
        String keyword = "test";
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .likeIgnoreCase(!ObjectUtils.isEmpty(keyword), TestEntity::getUsername, keyword)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testLikeLeftCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .likeLeft(TestEntity::getUsername, "n")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testLikeRightCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .likeRight(TestEntity::getUsername, "adm")
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== notLike / notLikeIgnoreCase / notLikeLeft / notLikeRight ====================

    @Test
    void testNotLikeCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notLike(TestEntity::getUsername, "deleted")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotLikeWithString() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notLike("username", "deleted")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotLikeConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notLike(false, TestEntity::getUsername, "deleted")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotLikeIgnoreCaseCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notLikeIgnoreCase(TestEntity::getUsername, "DELETED")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotLikeLeftCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notLikeLeft(TestEntity::getUsername, "d")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotLikeRightCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notLikeRight(TestEntity::getUsername, "del")
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== isNull / isNotNull ====================

    @Test
    void testIsNullCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .isNull(true, TestEntity::getStatus)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testIsNullConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .isNull(false, TestEntity::getStatus)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testIsNotNullCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .isNotNull(true, TestEntity::getStatus)
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== in / notIn ====================

    @Test
    void testInCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .in(true, TestEntity::getStatus, 1, 2, 3)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testInConditionWithList() {
        List<Integer> statusList = Arrays.asList(1, 2, 3);
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .in(TestEntity::getStatus, statusList)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testInConditionWithEmptyArray() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .in("status", new Object[0])
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testInConditionWithEmptyList() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .in("status", Collections.emptyList())
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotInCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notIn(TestEntity::getStatus, 0, -1)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotInConditionWithList() {
        List<Integer> excluded = Arrays.asList(0, -1);
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notIn(TestEntity::getStatus, excluded)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotInConditionWithEmptyArray() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notIn("status", new Object[0])
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotInConditionWithEmptyList() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notIn("status", Collections.emptyList())
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotInConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notIn(false, TestEntity::getStatus, 0, -1)
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== between / notBetween ====================

    @Test
    void testBetweenCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .between(true, TestEntity::getStatus, 1, 10)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotBetweenCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notBetween(TestEntity::getStatus, 0, 0)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testNotBetweenConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .notBetween(false, TestEntity::getStatus, 0, 0)
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== or(Consumer) / and(Consumer) ====================

    @Test
    void testOrConditionWithMultipleLikes() {
        String keyword = "test";
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(TestEntity::getUsername, keyword)
                        .likeIgnoreCase(TestEntity::getRealName, keyword)
                        .likeIgnoreCase(TestEntity::getEmail, keyword))
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testOrConditionWithEmptyKeyword() {
        String keyword = "";
        Integer status = 1;
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(TestEntity::getUsername, keyword)
                        .likeIgnoreCase(TestEntity::getRealName, keyword)
                        .likeIgnoreCase(TestEntity::getEmail, keyword))
                .eq(status != null, TestEntity::getStatus, status)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testOrConditionWithNullKeyword() {
        String keyword = null;
        Integer status = null;
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(TestEntity::getUsername, keyword)
                        .likeIgnoreCase(TestEntity::getRealName, keyword)
                        .likeIgnoreCase(TestEntity::getEmail, keyword))
                .eq(status != null, TestEntity::getStatus, status)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testAndCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .and(true, w -> w
                        .eq(TestEntity::getStatus, 1)
                        .like(TestEntity::getUsername, "test"))
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testAndConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .and(false, w -> w
                        .eq(TestEntity::getStatus, 1))
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== or() top-level ====================

    @Test
    void testTopLevelOr() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(TestEntity::getStatus, 1)
                .or()
                .eq(TestEntity::getStatus, 2)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testTopLevelOrWithMultipleConditions() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(TestEntity::getStatus, 1)
                .eq(TestEntity::getUsername, "admin")
                .or()
                .eq(TestEntity::getStatus, 2)
                .eq(TestEntity::getUsername, "user")
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== orderBy ====================

    @Test
    void testOrderByAsc() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .orderByAsc(TestEntity::getUsername)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testOrderByDesc() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .orderByDesc(TestEntity::getStatus)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testMultipleOrderBy() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .orderByDesc(TestEntity::getStatus)
                .orderByAsc(TestEntity::getUsername)
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== distinct ====================

    @Test
    void testDistinct() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(TestEntity::getStatus, 1)
                .distinct()
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== groupBy / having ====================

    @Test
    void testGroupBySingleField() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .groupBy("status")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testGroupByMultipleFields() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .groupBy("status", "department")
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testGroupByWithLambda() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .groupBy(TestEntity::getStatus)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testHaving() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .groupBy("status")
                .having((root, cb) -> cb.gt(cb.count(root), 1))
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testHavingConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .groupBy("status")
                .having(false, (root, cb) -> cb.gt(cb.count(root), 1))
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== raw ====================

    @Test
    void testRawCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .raw((root, cb) -> cb.equal(root.get("username"), "test"))
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testRawConditionWithJoin() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .raw((root, cb) -> {
                    var join = root.join("department");
                    return cb.equal(join.get("name"), "Engineering");
                })
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testRawConditionDisabled() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .raw(false, (root, cb) -> cb.equal(root.get("username"), "test"))
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== build / buildSpec ====================

    @Test
    void testBuildMethod() {
        Integer status = 1;
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(status != null, TestEntity::getStatus, status)
                .orderByDesc(TestEntity::getStatus)
                .build();
        assertNotNull(spec);
    }

    @Test
    void testExplicitConditionControl() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(true, TestEntity::getStatus, 1)
                .eq(false, TestEntity::getStatus, 0)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testCombinedConditions() {
        String keyword = "admin";
        Integer status = 1;
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(TestEntity::getUsername, keyword)
                        .likeIgnoreCase(TestEntity::getRealName, keyword))
                .eq(status != null, TestEntity::getStatus, status)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testEmptyConditions() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .buildSpec();
        assertNotNull(spec);
    }

    // ==================== complex combinations ====================

    @Test
    void testComplexQueryWithDistinctAndOrderBy() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .eq(TestEntity::getStatus, 1)
                .distinct()
                .orderByAsc(TestEntity::getUsername)
                .orderByDesc(TestEntity::getStatus)
                .buildSpec();
        assertNotNull(spec);
    }

    @Test
    void testComplexQueryWithOrAndNotIn() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .or(w -> w
                        .eq(TestEntity::getStatus, 1)
                        .eq(TestEntity::getStatus, 2))
                .notIn(TestEntity::getStatus, 0, -1)
                .buildSpec();
        assertNotNull(spec);
    }
}