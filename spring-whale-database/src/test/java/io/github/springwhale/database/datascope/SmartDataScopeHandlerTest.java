package io.github.springwhale.database.datascope;

import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.context.AuthenticationContext;
import io.github.springwhale.framework.core.context.AuthenticationContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartDataScopeHandlerTest {

    @Mock
    private WhaleCacheManager cacheManager;

    @Mock
    private WhaleCache cache;

    @Mock
    private DataScopeRemoteApi remoteApi;

    private DataScopeProperties properties;
    private SmartDataScopeHandler handler;

    @BeforeEach
    void setUp() {
        properties = new DataScopeProperties();
        handler = new SmartDataScopeHandler(cacheManager, remoteApi, properties);
        lenient().when(cacheManager.getCache("dataScope")).thenReturn(cache);
    }

    @AfterEach
    void tearDown() {
        AuthenticationContextHolder.clearContext();
    }

    private void loginAs(Integer userId) {
        AuthenticationContextHolder.setContext(
                new AuthenticationContext(userId, "user" + userId, null));
    }

    // ========== skipDataScope tests ==========

    @Test
    @DisplayName("skipDataScope: cache hit returns cached value without remote call")
    void testSkipDataScopeCacheHit() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipDataScope(1), Boolean.class)).thenReturn(true);

        boolean result = handler.skipDataScope();

        assertThat(result).isTrue();
        verify(remoteApi, never()).skipDataScope(any());
    }

    @Test
    @DisplayName("skipDataScope: cache miss calls remote API and caches result")
    void testSkipDataScopeCacheMiss() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipDataScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipDataScope(1)).thenReturn(new DataScopeSkipResponse(true));

        boolean result = handler.skipDataScope();

        assertThat(result).isTrue();
        verify(cache).put(DataScopeCacheKey.skipDataScope(1), true, Duration.ofMinutes(5));
        verify(cache).put(DataScopeCacheKey.fallbackSkipDataScope(1), true, Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("skipDataScope: remote API failure falls back to fallback cache")
    void testSkipDataScopeFallbackCacheHit() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipDataScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipDataScope(1)).thenThrow(new RuntimeException("Network error"));
        when(cache.get(DataScopeCacheKey.fallbackSkipDataScope(1), Boolean.class)).thenReturn(true);

        boolean result = handler.skipDataScope();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("skipDataScope: remote API failure with no fallback returns false")
    void testSkipDataScopeFallbackCacheMiss() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipDataScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipDataScope(1)).thenThrow(new RuntimeException("Network error"));
        when(cache.get(DataScopeCacheKey.fallbackSkipDataScope(1), Boolean.class)).thenReturn(null);

        boolean result = handler.skipDataScope();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("skipDataScope: null userId returns false without cache access")
    void testSkipDataScopeNullUserId() {
        boolean result = handler.skipDataScope();

        assertThat(result).isFalse();
        verify(remoteApi, never()).skipDataScope(any());
    }

    @Test
    @DisplayName("skipDataScope: null response from remote API returns false")
    void testSkipDataScopeNullResponse() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipDataScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipDataScope(1)).thenReturn(null);

        boolean result = handler.skipDataScope();

        assertThat(result).isFalse();
    }

    // ========== skipTenantScope tests ==========

    @Test
    @DisplayName("skipTenantScope: cache hit returns cached value without remote call")
    void testSkipTenantScopeCacheHit() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipTenantScope(1), Boolean.class)).thenReturn(true);

        boolean result = handler.skipTenantScope();

        assertThat(result).isTrue();
        verify(remoteApi, never()).skipTenantScope(any());
    }

    @Test
    @DisplayName("skipTenantScope: cache miss calls remote API and caches result")
    void testSkipTenantScopeCacheMiss() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipTenantScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipTenantScope(1)).thenReturn(new DataScopeSkipResponse(true));

        boolean result = handler.skipTenantScope();

        assertThat(result).isTrue();
        verify(cache).put(DataScopeCacheKey.skipTenantScope(1), true, Duration.ofMinutes(5));
        verify(cache).put(DataScopeCacheKey.fallbackSkipTenantScope(1), true, Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("skipTenantScope: remote API failure falls back to fallback cache")
    void testSkipTenantScopeFallbackCacheHit() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipTenantScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipTenantScope(1)).thenThrow(new RuntimeException("Network error"));
        when(cache.get(DataScopeCacheKey.fallbackSkipTenantScope(1), Boolean.class)).thenReturn(true);

        boolean result = handler.skipTenantScope();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("skipTenantScope: remote API failure with no fallback returns false")
    void testSkipTenantScopeFallbackCacheMiss() {
        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipTenantScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipTenantScope(1)).thenThrow(new RuntimeException("Network error"));
        when(cache.get(DataScopeCacheKey.fallbackSkipTenantScope(1), Boolean.class)).thenReturn(null);

        boolean result = handler.skipTenantScope();

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("skipTenantScope: null userId returns false without cache access")
    void testSkipTenantScopeNullUserId() {
        boolean result = handler.skipTenantScope();

        assertThat(result).isFalse();
        verify(remoteApi, never()).skipTenantScope(any());
    }

    // ========== resolveDeptIds tests ==========

    @Test
    @DisplayName("resolveDeptIds: cache hit returns cached list without remote call")
    void testResolveDeptIdsCacheHit() {
        loginAs(1);
        String cacheKey = DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT, "order");
        List<Object> cachedList = List.of(1L, 2L);
        when(cache.getList(cacheKey)).thenReturn(cachedList);

        List<Object> result = handler.resolveDeptIds(DataScopeType.DEPT, "order");

        assertThat(result).containsExactly(1L, 2L);
        verify(remoteApi, never()).resolveDeptIds(any(), any(), any());
    }

    @Test
    @DisplayName("resolveDeptIds: cache miss calls remote API and caches result")
    void testResolveDeptIdsCacheMiss() {
        loginAs(1);
        String cacheKey = DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT, "order");
        List<Object> deptIds = List.of(1L, 2L, 3L);
        when(cache.getList(cacheKey)).thenReturn(null);
        when(remoteApi.resolveDeptIds(1, DataScopeType.DEPT, "order"))
                .thenReturn(new DataScopeResolveResponse(deptIds));

        List<Object> result = handler.resolveDeptIds(DataScopeType.DEPT, "order");

        assertThat(result).containsExactly(1L, 2L, 3L);
        verify(cache).put(cacheKey, deptIds, Duration.ofMinutes(2));
        verify(cache).put(DataScopeCacheKey.fallbackResolveDeptIds(1, DataScopeType.DEPT, "order"),
                deptIds, Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("resolveDeptIds: remote API failure falls back to fallback cache")
    void testResolveDeptIdsFallbackCacheHit() {
        loginAs(1);
        String cacheKey = DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT, "order");
        List<Object> fallbackList = List.of(1L);
        when(cache.getList(cacheKey)).thenReturn(null);
        when(remoteApi.resolveDeptIds(1, DataScopeType.DEPT, "order"))
                .thenThrow(new RuntimeException("Network error"));
        when(cache.getList(DataScopeCacheKey.fallbackResolveDeptIds(1, DataScopeType.DEPT, "order")))
                .thenReturn(fallbackList);

        List<Object> result = handler.resolveDeptIds(DataScopeType.DEPT, "order");

        assertThat(result).containsExactly(1L);
    }

    @Test
    @DisplayName("resolveDeptIds: remote API failure with no fallback returns empty list")
    void testResolveDeptIdsFallbackCacheMiss() {
        loginAs(1);
        String cacheKey = DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT, "order");
        when(cache.getList(cacheKey)).thenReturn(null);
        when(remoteApi.resolveDeptIds(1, DataScopeType.DEPT, "order"))
                .thenThrow(new RuntimeException("Network error"));
        when(cache.getList(DataScopeCacheKey.fallbackResolveDeptIds(1, DataScopeType.DEPT, "order")))
                .thenReturn(null);

        List<Object> result = handler.resolveDeptIds(DataScopeType.DEPT, "order");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("resolveDeptIds: null userId returns empty list without cache access")
    void testResolveDeptIdsNullUserId() {
        List<Object> result = handler.resolveDeptIds(DataScopeType.DEPT, "order");

        assertThat(result).isEmpty();
        verify(remoteApi, never()).resolveDeptIds(any(), any(), any());
    }

    @Test
    @DisplayName("resolveDeptIds: null response from remote API returns empty list")
    void testResolveDeptIdsNullResponse() {
        loginAs(1);
        String cacheKey = DataScopeCacheKey.resolveDeptIds(1, DataScopeType.DEPT, "order");
        when(cache.getList(cacheKey)).thenReturn(null);
        when(remoteApi.resolveDeptIds(1, DataScopeType.DEPT, "order")).thenReturn(null);

        List<Object> result = handler.resolveDeptIds(DataScopeType.DEPT, "order");

        assertThat(result).isEmpty();
    }

    // ========== constructor tests ==========

    @Test
    @DisplayName("constructor reads TTL values from properties")
    void testConstructorReadsTtlFromProperties() {
        DataScopeProperties customProps = new DataScopeProperties();
        DataScopeProperties.Cache cacheProps = new DataScopeProperties.Cache();
        cacheProps.setSkipTtl(Duration.ofMinutes(10));
        cacheProps.setDeptTtl(Duration.ofMinutes(5));
        cacheProps.setFallbackTtl(Duration.ofHours(1));
        customProps.setCache(cacheProps);

        SmartDataScopeHandler customHandler = new SmartDataScopeHandler(cacheManager, remoteApi, customProps);

        loginAs(1);
        when(cache.get(DataScopeCacheKey.skipDataScope(1), Boolean.class)).thenReturn(null);
        when(remoteApi.skipDataScope(1)).thenReturn(new DataScopeSkipResponse(true));

        customHandler.skipDataScope();

        verify(cache).put(eq(DataScopeCacheKey.skipDataScope(1)), eq(true), eq(Duration.ofMinutes(10)));
        verify(cache).put(eq(DataScopeCacheKey.fallbackSkipDataScope(1)), eq(true), eq(Duration.ofHours(1)));
    }
}