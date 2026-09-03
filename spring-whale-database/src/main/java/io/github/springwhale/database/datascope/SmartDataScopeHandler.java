package io.github.springwhale.database.datascope;

import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.utils.AuthUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Cache-first {@link DataScopeHandler} implementation for downstream services
 * in a microservice architecture.
 *
 * <h3>Resolution strategy</h3>
 * <ol>
 *   <li>Check local/Redis cache via {@link WhaleCacheManager}</li>
 *   <li>On cache miss, call remote RBAC service via {@link DataScopeRemoteApi}</li>
 *   <li>Cache the result with primary TTL and fallback TTL for subsequent requests</li>
 * </ol>
 *
 * <h3>Fallback strategy</h3>
 * <p>When the RBAC service is unreachable, the handler falls back to a
 * separate cache entry with a longer TTL. This prevents cascading
 * failures: if RBAC is temporarily down, the last known-good permission
 * state is used instead of denying all access.</p>
 *
 * <pre>
 * Cache hit (primary) ──→ return cached value
 * Cache miss ──→ call RBAC API
 *   ├── success ──→ write primary + fallback ──→ return value
 *   └── failure ──→ read fallback
 *         ├── hit  ──→ return stale value
 *         └── miss ──→ return default (false/empty)
 * </pre>
 *
 * <p>Activated when {@code spring.whale.database.datascope.remote-rbac-url} is configured
 * and no other {@link DataScopeHandler} bean is present.</p>
 */
@Slf4j
public class SmartDataScopeHandler implements DataScopeHandler {

    private final WhaleCacheManager cacheManager;
    private final DataScopeRemoteApi remoteApi;
    private final Duration skipTtl;
    private final Duration deptTtl;
    private final Duration fallbackTtl;

    public SmartDataScopeHandler(WhaleCacheManager cacheManager,
                                 DataScopeRemoteApi remoteApi,
                                 DataScopeProperties properties) {
        this.cacheManager = cacheManager;
        this.remoteApi = remoteApi;
        DataScopeProperties.Cache cacheProps = properties.getCache();
        this.skipTtl = cacheProps.getSkipTtl();
        this.deptTtl = cacheProps.getDeptTtl();
        this.fallbackTtl = cacheProps.getFallbackTtl();
    }

    @Override
    public boolean skipTenantScope() {
        Long userId = AuthUtil.getUserId();
        if (userId == null) {
            return false;
        }
        WhaleCache cache = cacheManager.getCache("dataScope");
        Boolean cached = cache.get(DataScopeCacheKey.skipTenantScope(userId), Boolean.class);
        if (cached != null) {
            return cached;
        }
        return fetchAndCacheSkipTenantScope(cache, userId);
    }

    @Override
    public boolean skipDataScope() {
        Long userId = AuthUtil.getUserId();
        if (userId == null) {
            return false;
        }
        WhaleCache cache = cacheManager.getCache("dataScope");
        Boolean cached = cache.get(DataScopeCacheKey.skipDataScope(userId), Boolean.class);
        if (cached != null) {
            return cached;
        }
        return fetchAndCacheSkipDataScope(cache, userId);
    }

    @Override
    public List<Object> resolveDeptIds(DataScopeType scopeType, String module) {
        Long userId = AuthUtil.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        WhaleCache cache = cacheManager.getCache("dataScope");
        List<Object> cached = cache.getList(DataScopeCacheKey.resolveDeptIds(userId, scopeType, module));
        if (cached != null) {
            return cached;
        }
        return fetchAndCacheDeptIds(cache, userId, scopeType, module);
    }

    private boolean fetchAndCacheSkipDataScope(WhaleCache cache, Long userId) {
        try {
            DataScopeSkipResponse response = remoteApi.skipDataScope(userId);
            boolean result = response != null && response.skip();
            cache.put(DataScopeCacheKey.skipDataScope(userId), result, skipTtl);
            cache.put(DataScopeCacheKey.fallbackSkipDataScope(userId), result, fallbackTtl);
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch skipDataScope from RBAC service for userId={}", userId, e);
            Boolean fallback = cache.get(DataScopeCacheKey.fallbackSkipDataScope(userId), Boolean.class);
            if (fallback != null) {
                log.warn("Using fallback cache for skipDataScope userId={}, value={}", userId, fallback);
                return fallback;
            }
            return false;
        }
    }

    private boolean fetchAndCacheSkipTenantScope(WhaleCache cache, Long userId) {
        try {
            DataScopeSkipResponse response = remoteApi.skipTenantScope(userId);
            boolean result = response != null && response.skip();
            cache.put(DataScopeCacheKey.skipTenantScope(userId), result, skipTtl);
            cache.put(DataScopeCacheKey.fallbackSkipTenantScope(userId), result, fallbackTtl);
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch skipTenantScope from RBAC service for userId={}", userId, e);
            Boolean fallback = cache.get(DataScopeCacheKey.fallbackSkipTenantScope(userId), Boolean.class);
            if (fallback != null) {
                log.warn("Using fallback cache for skipTenantScope userId={}, value={}", userId, fallback);
                return fallback;
            }
            return false;
        }
    }

    private List<Object> fetchAndCacheDeptIds(WhaleCache cache, Long userId,
                                              DataScopeType scopeType, String module) {
        try {
            DataScopeResolveResponse response = remoteApi.resolveDeptIds(userId, scopeType, module);
            List<Object> result = response != null ? response.deptIds() : Collections.emptyList();
            cache.put(DataScopeCacheKey.resolveDeptIds(userId, scopeType, module), result, deptTtl);
            cache.put(DataScopeCacheKey.fallbackResolveDeptIds(userId, scopeType, module), result, fallbackTtl);
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch resolveDeptIds from RBAC service for userId={}, scopeType={}, module={}",
                    userId, scopeType, module, e);
            List<Object> fallback = cache.getList(
                    DataScopeCacheKey.fallbackResolveDeptIds(userId, scopeType, module));
            if (fallback != null) {
                log.warn("Using fallback cache for resolveDeptIds userId={}, scopeType={}, module={}",
                        userId, scopeType, module);
                return fallback;
            }
            return Collections.emptyList();
        }
    }
}