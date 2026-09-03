package io.github.springwhale.database.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDataScopeHandlerTest {

    private final DefaultDataScopeHandler handler = new DefaultDataScopeHandler();

    @Test
    @DisplayName("skipDataScope returns false by default")
    void testSkipDataScope() {
        assertThat(handler.skipDataScope()).isFalse();
    }

    @Test
    @DisplayName("skipTenantScope returns false by default")
    void testSkipTenantScope() {
        assertThat(handler.skipTenantScope()).isFalse();
    }

    @Test
    @DisplayName("resolveDeptIds returns null for any scope type")
    void testResolveDeptIds() {
        assertThat(handler.resolveDeptIds(DataScopeType.DEPT, "order")).isNull();
        assertThat(handler.resolveDeptIds(DataScopeType.SELF, null)).isNull();
        assertThat(handler.resolveDeptIds(DataScopeType.DEPT_AND_CHILD, "product")).isNull();
    }

    @Test
    @DisplayName("resolveUserId returns null (AuthUtil returns null without auth context)")
    void testResolveUserId() {
        assertThat(handler.resolveUserId()).isNull();
    }

    @Test
    @DisplayName("resolveTenantId returns null (AuthUtil returns null without auth context)")
    void testResolveTenantId() {
        assertThat(handler.resolveTenantId()).isNull();
    }
}