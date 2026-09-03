package io.github.springwhale.database.datascope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeContextTest {

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    @Test
    @DisplayName("pushScope adds scope to stack and hasScope returns true")
    void testPushAndHasScope() {
        DataScopeResult scope = new DataScopeResult();
        scope.setScopeType(DataScopeType.DEPT);

        DataScopeContext.pushScope(scope);

        assertThat(DataScopeContext.hasScope()).isTrue();
        assertThat(DataScopeContext.getDepth()).isEqualTo(1);
    }

    @Test
    @DisplayName("hasScope returns false when no scope pushed")
    void testHasScopeWhenEmpty() {
        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("getScope returns the top scope without removing it")
    void testGetScopePeeks() {
        DataScopeResult scope1 = new DataScopeResult();
        scope1.setScopeType(DataScopeType.DEPT);

        DataScopeResult scope2 = new DataScopeResult();
        scope2.setScopeType(DataScopeType.SELF);

        DataScopeContext.pushScope(scope1);
        DataScopeContext.pushScope(scope2);

        assertThat(DataScopeContext.getScope()).isSameAs(scope2);
        assertThat(DataScopeContext.getDepth()).isEqualTo(2);
    }

    @Test
    @DisplayName("getScope returns null when stack is empty")
    void testGetScopeWhenEmpty() {
        assertThat(DataScopeContext.getScope()).isNull();
    }

    @Test
    @DisplayName("popScope removes and returns the top scope")
    void testPopScope() {
        DataScopeResult scope = new DataScopeResult();
        scope.setScopeType(DataScopeType.DEPT);

        DataScopeContext.pushScope(scope);
        DataScopeResult popped = DataScopeContext.popScope();

        assertThat(popped).isSameAs(scope);
        assertThat(DataScopeContext.hasScope()).isFalse();
    }

    @Test
    @DisplayName("depth tracks nested scope pushes correctly")
    void testDepth() {
        assertThat(DataScopeContext.getDepth()).isEqualTo(0);

        DataScopeContext.pushScope(new DataScopeResult());
        assertThat(DataScopeContext.getDepth()).isEqualTo(1);

        DataScopeContext.pushScope(new DataScopeResult());
        assertThat(DataScopeContext.getDepth()).isEqualTo(2);

        DataScopeContext.popScope();
        assertThat(DataScopeContext.getDepth()).isEqualTo(1);

        DataScopeContext.popScope();
        assertThat(DataScopeContext.getDepth()).isEqualTo(0);
    }

    @Test
    @DisplayName("clear removes all scopes and entity info")
    void testClear() {
        DataScopeContext.pushScope(new DataScopeResult());
        DataScopeContext.setEntityClass(Object.class);
        DataScopeContext.setDeptFields(List.of("dept_id"));
        DataScopeContext.setUserFields(List.of("user_id"));
        DataScopeContext.setTenantId(1L);
        DataScopeContext.setTenantFields(List.of("tenant_id"));

        DataScopeContext.clear();

        assertThat(DataScopeContext.hasScope()).isFalse();
        assertThat(DataScopeContext.getEntityClass()).isNull();
        assertThat(DataScopeContext.getDeptFields()).isNull();
        assertThat(DataScopeContext.getUserFields()).isNull();
        assertThat(DataScopeContext.getTenantId()).isNull();
        assertThat(DataScopeContext.getTenantFields()).isNull();
    }

    @Test
    @DisplayName("clearEntityInfo clears entity class and field holders but not scope")
    void testClearEntityInfo() {
        DataScopeContext.pushScope(new DataScopeResult());
        DataScopeContext.setEntityClass(Object.class);
        DataScopeContext.setDeptFields(List.of("dept_id"));

        DataScopeContext.clearEntityInfo();

        assertThat(DataScopeContext.hasScope()).isTrue();
        assertThat(DataScopeContext.getEntityClass()).isNull();
        assertThat(DataScopeContext.getDeptFields()).isNull();
    }

    @Test
    @DisplayName("setEntityClass and getEntityClass work correctly")
    void testEntityClass() {
        DataScopeContext.setEntityClass(String.class);
        assertThat(DataScopeContext.getEntityClass()).isEqualTo(String.class);

        DataScopeContext.setEntityClass(Integer.class);
        assertThat(DataScopeContext.getEntityClass()).isEqualTo(Integer.class);
    }

    @Test
    @DisplayName("setDeptFields and getDeptFields work correctly")
    void testDeptFields() {
        List<String> fields = List.of("dept_id", "owner_id");
        DataScopeContext.setDeptFields(fields);
        assertThat(DataScopeContext.getDeptFields()).containsExactly("dept_id", "owner_id");
    }

    @Test
    @DisplayName("setUserFields and getUserFields work correctly")
    void testUserFields() {
        List<String> fields = List.of("user_id");
        DataScopeContext.setUserFields(fields);
        assertThat(DataScopeContext.getUserFields()).containsExactly("user_id");
    }

    @Test
    @DisplayName("setTenantId and getTenantId work correctly")
    void testTenantId() {
        DataScopeContext.setTenantId(100L);
        assertThat(DataScopeContext.getTenantId()).isEqualTo(100L);

        DataScopeContext.setTenantId("tenant-abc");
        assertThat(DataScopeContext.getTenantId()).isEqualTo("tenant-abc");
    }

    @Test
    @DisplayName("setTenantFields and getTenantFields work correctly")
    void testTenantFields() {
        List<String> fields = List.of("tenant_id");
        DataScopeContext.setTenantFields(fields);
        assertThat(DataScopeContext.getTenantFields()).containsExactly("tenant_id");
    }

    @Test
    @DisplayName("skipTenant defaults to false")
    void testSkipTenantDefault() {
        assertThat(DataScopeContext.isSkipTenant()).isFalse();
    }

    @Test
    @DisplayName("setSkipTenant changes skip tenant flag")
    void testSkipTenant() {
        DataScopeContext.setSkipTenant(true);
        assertThat(DataScopeContext.isSkipTenant()).isTrue();

        DataScopeContext.setSkipTenant(false);
        assertThat(DataScopeContext.isSkipTenant()).isFalse();
    }

    @Test
    @DisplayName("clear resets skipTenant to false")
    void testClearResetsSkipTenant() {
        DataScopeContext.setSkipTenant(true);
        DataScopeContext.clear();
        assertThat(DataScopeContext.isSkipTenant()).isFalse();
    }
}