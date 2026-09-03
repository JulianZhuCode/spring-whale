package io.github.springwhale.database.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeResultTest {

    @Test
    @DisplayName("new DataScopeResult has empty deptIds and null userId")
    void testDefaultState() {
        DataScopeResult result = new DataScopeResult();

        assertThat(result.getDeptIds()).isNotNull().isEmpty();
        assertThat(result.getUserId()).isNull();
        assertThat(result.getScopeType()).isNull();
        assertThat(result.getModule()).isNull();
        assertThat(result.isDenied()).isFalse();
    }

    @Test
    @DisplayName("isEmpty returns true when no deptIds and no userId")
    void testIsEmptyWhenNoData() {
        DataScopeResult result = new DataScopeResult();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("isEmpty returns false when deptIds are present")
    void testIsEmptyWhenHasDeptIds() {
        DataScopeResult result = new DataScopeResult();
        result.setDeptIds(List.of(1L, 2L));
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("isEmpty returns false when userId is present")
    void testIsEmptyWhenHasUserId() {
        DataScopeResult result = new DataScopeResult();
        result.setUserId(42);
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("hasDeptIds returns true when deptIds list is not empty")
    void testHasDeptIds() {
        DataScopeResult result = new DataScopeResult();
        assertThat(result.hasDeptIds()).isFalse();

        result.setDeptIds(List.of(1L));
        assertThat(result.hasDeptIds()).isTrue();
    }

    @Test
    @DisplayName("hasDeptIds returns false when deptIds is null")
    void testHasDeptIdsWhenNull() {
        DataScopeResult result = new DataScopeResult();
        result.setDeptIds(null);
        assertThat(result.hasDeptIds()).isFalse();
    }

    @Test
    @DisplayName("hasDeptIds returns false when deptIds is empty list")
    void testHasDeptIdsWhenEmpty() {
        DataScopeResult result = new DataScopeResult();
        result.setDeptIds(new ArrayList<>());
        assertThat(result.hasDeptIds()).isFalse();
    }

    @Test
    @DisplayName("hasUserId returns true when userId is set")
    void testHasUserId() {
        DataScopeResult result = new DataScopeResult();
        assertThat(result.hasUserId()).isFalse();

        result.setUserId(42);
        assertThat(result.hasUserId()).isTrue();
    }

    @Test
    @DisplayName("denied flag is settable")
    void testDeniedFlag() {
        DataScopeResult result = new DataScopeResult();
        result.setDenied(true);
        assertThat(result.isDenied()).isTrue();

        result.setDenied(false);
        assertThat(result.isDenied()).isFalse();
    }

    @Test
    @DisplayName("all-args constructor sets all fields")
    void testAllArgsConstructor() {
        DataScopeResult result = new DataScopeResult(
                DataScopeType.DEPT, "order", List.of(1L, 2L), 42, false);

        assertThat(result.getScopeType()).isEqualTo(DataScopeType.DEPT);
        assertThat(result.getModule()).isEqualTo("order");
        assertThat(result.getDeptIds()).containsExactly(1L, 2L);
        assertThat(result.getUserId()).isEqualTo(42);
        assertThat(result.isDenied()).isFalse();
    }
}