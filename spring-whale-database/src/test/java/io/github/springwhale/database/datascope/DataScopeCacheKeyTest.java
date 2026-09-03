package io.github.springwhale.database.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeCacheKeyTest {

    @Test
    @DisplayName("skipDataScope key format: skip:{userId}")
    void testSkipDataScope() {
        assertThat(DataScopeCacheKey.skipDataScope(1L)).isEqualTo("skip:1");
        assertThat(DataScopeCacheKey.skipDataScope(999L)).isEqualTo("skip:999");
    }

    @Test
    @DisplayName("fallbackSkipDataScope key format: fallback:skip:{userId}")
    void testFallbackSkipDataScope() {
        assertThat(DataScopeCacheKey.fallbackSkipDataScope(1L)).isEqualTo("fallback:skip:1");
        assertThat(DataScopeCacheKey.fallbackSkipDataScope(999L)).isEqualTo("fallback:skip:999");
    }

    @Test
    @DisplayName("skipTenantScope key format: skipTenant:{userId}")
    void testSkipTenantScope() {
        assertThat(DataScopeCacheKey.skipTenantScope(1L)).isEqualTo("skipTenant:1");
        assertThat(DataScopeCacheKey.skipTenantScope(999L)).isEqualTo("skipTenant:999");
    }

    @Test
    @DisplayName("fallbackSkipTenantScope key format: fallback:skipTenant:{userId}")
    void testFallbackSkipTenantScope() {
        assertThat(DataScopeCacheKey.fallbackSkipTenantScope(1L)).isEqualTo("fallback:skipTenant:1");
        assertThat(DataScopeCacheKey.fallbackSkipTenantScope(999L)).isEqualTo("fallback:skipTenant:999");
    }

    @Test
    @DisplayName("resolveDeptIds key format: dept:{userId}:{scopeType}:{module}")
    void testResolveDeptIds() {
        assertThat(DataScopeCacheKey.resolveDeptIds(1L, DataScopeType.DEPT, "order"))
                .isEqualTo("dept:1:DEPT:order");
        assertThat(DataScopeCacheKey.resolveDeptIds(42L, DataScopeType.SELF, null))
                .isEqualTo("dept:42:SELF:");
    }

    @Test
    @DisplayName("resolveDeptIds with empty module uses empty string")
    void testResolveDeptIdsEmptyModule() {
        assertThat(DataScopeCacheKey.resolveDeptIds(1L, DataScopeType.DEPT, ""))
                .isEqualTo("dept:1:DEPT:");
    }

    @Test
    @DisplayName("fallbackResolveDeptIds key format: fallback:dept:{userId}:{scopeType}:{module}")
    void testFallbackResolveDeptIds() {
        assertThat(DataScopeCacheKey.fallbackResolveDeptIds(1L, DataScopeType.DEPT, "order"))
                .isEqualTo("fallback:dept:1:DEPT:order");
        assertThat(DataScopeCacheKey.fallbackResolveDeptIds(42L, DataScopeType.SELF, null))
                .isEqualTo("fallback:dept:42:SELF:");
    }

    @Test
    @DisplayName("skipDataScope and skipTenantScope keys are distinct")
    void testSkipKeysAreDistinct() {
        assertThat(DataScopeCacheKey.skipDataScope(1L))
                .isNotEqualTo(DataScopeCacheKey.skipTenantScope(1L));
    }

    @Test
    @DisplayName("primary and fallback keys are distinct")
    void testPrimaryAndFallbackAreDistinct() {
        assertThat(DataScopeCacheKey.skipDataScope(1L))
                .isNotEqualTo(DataScopeCacheKey.fallbackSkipDataScope(1L));
        assertThat(DataScopeCacheKey.resolveDeptIds(1L, DataScopeType.DEPT, "order"))
                .isNotEqualTo(DataScopeCacheKey.fallbackResolveDeptIds(1L, DataScopeType.DEPT, "order"));
    }

    @Test
    @DisplayName("resolveDeptIds keys for different scope types are distinct")
    void testResolveDeptIdsDifferentScopeTypes() {
        assertThat(DataScopeCacheKey.resolveDeptIds(1L, DataScopeType.DEPT, "order"))
                .isNotEqualTo(DataScopeCacheKey.resolveDeptIds(1L, DataScopeType.SELF, "order"));
    }

    @Test
    @DisplayName("resolveDeptIds keys for different modules are distinct")
    void testResolveDeptIdsDifferentModules() {
        assertThat(DataScopeCacheKey.resolveDeptIds(1L, DataScopeType.DEPT, "order"))
                .isNotEqualTo(DataScopeCacheKey.resolveDeptIds(1L, DataScopeType.DEPT, "product"));
    }
}