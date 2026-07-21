package io.github.springwhale.database;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestEntity {
    private String username;
    private String realName;
    private String email;
    private Integer status;

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
}

class JpaQueryWrapperTest {

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
    void testLikeIgnoreCaseCondition() {
        String keyword = "test";
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .likeIgnoreCase(!ObjectUtils.isEmpty(keyword), TestEntity::getUsername, keyword)
                .buildSpec();

        assertNotNull(spec);
    }

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
    void testIsNullCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .isNull(true, TestEntity::getStatus)
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

    @Test
    void testInCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .in(true, TestEntity::getStatus, 1, 2, 3)
                .buildSpec();

        assertNotNull(spec);
    }

    @Test
    void testBetweenCondition() {
        Specification<TestEntity> spec = JpaQueryWrapper.of(TestEntity.class)
                .between(true, TestEntity::getStatus, 1, 10)
                .buildSpec();

        assertNotNull(spec);
    }
}
