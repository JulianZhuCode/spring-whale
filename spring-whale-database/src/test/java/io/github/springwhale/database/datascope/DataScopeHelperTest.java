package io.github.springwhale.database.datascope;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeHelperTest {

    private final DataScopeHelper helper = new DataScopeHelper();

    @Entity
    @DeptIdScope
    static class DeptEntity {
        @Id
        private Integer id;
    }

    @Entity
    @DeptIdScope({"deptCode", "ownerId"})
    static class MultiDeptEntity {
        @Id
        private Integer id;

        @Column(name = "dept_code")
        private String deptCode;

        @Column(name = "owner_id")
        private Integer ownerId;
    }

    @Entity
    @UserIdScope
    static class UserScopeEntity {
        @Id
        private Integer id;
    }

    @Entity
    @TenantIdScope
    static class TenantScopeEntity {
        @Id
        private Integer id;
    }

    @Entity
    @DeptIdScope
    static class ParentIdEntity extends DeptEntity {
        @DeptIdField
        private Integer parentDeptId;
    }

    @Entity
    @DeptIdScope({"nonExistent"})
    static class BadRefEntity {
        @Id
        private Integer id;
    }

    @Test
    @DisplayName("@DeptIdScope with default value resolves 'id'")
    void testDeptIdScopeDefault() {
        List<String> fields = helper.resolveDeptIdFields(DeptEntity.class);
        assertThat(fields).containsExactly("id");
    }

    @Test
    @DisplayName("@DeptIdScope with custom fields resolves specified column names")
    void testDeptIdScopeCustomFields() {
        List<String> fields = helper.resolveDeptIdFields(MultiDeptEntity.class);
        assertThat(fields).containsExactlyInAnyOrder("dept_code", "owner_id");
    }

    @Test
    @DisplayName("@UserIdScope with default value resolves 'id'")
    void testUserIdScopeDefault() {
        List<String> fields = helper.resolveUserIdFields(UserScopeEntity.class);
        assertThat(fields).containsExactly("id");
    }

    @Test
    @DisplayName("@TenantIdScope with default value resolves 'id'")
    void testTenantIdScopeDefault() {
        List<String> fields = helper.resolveTenantIdFields(TenantScopeEntity.class);
        assertThat(fields).containsExactly("id");
    }

    @Test
    @DisplayName("Class-level + field-level annotations are unioned")
    void testClassAndFieldLevelCombined() {
        List<String> fields = helper.resolveDeptIdFields(ParentIdEntity.class);
        assertThat(fields).containsExactlyInAnyOrder("id", "parent_dept_id");
    }

    @Test
    @DisplayName("Field-level only (no class-level) still works")
    void testFieldLevelOnly() {
        List<String> fields = helper.resolveDeptIdFields(TestUser.class);
        assertThat(fields).containsExactly("dept_id");
    }

    @Test
    @DisplayName("No annotations at all returns empty list")
    void testNoAnnotations() {
        List<String> fields = helper.resolveDeptIdFields(Object.class);
        assertThat(fields).isEmpty();
    }

    @Test
    @DisplayName("Non-existent field reference logs warning and skips")
    void testNonExistentField() {
        List<String> fields = helper.resolveDeptIdFields(BadRefEntity.class);
        assertThat(fields).isEmpty();
    }
}