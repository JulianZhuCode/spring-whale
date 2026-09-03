package io.github.springwhale.test.cache;

import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.cache.support.CaffeineWhaleCacheManager;
import io.github.springwhale.framework.core.cache.support.WhaleCacheManagerAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("WhaleCacheAutoConfiguration")
class WhaleCacheAutoConfigurationTest {

    @Autowired
    private WhaleCacheManager whaleCacheManager;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Should auto-configure CaffeineWhaleCacheManager as default")
    void testDefaultCacheManagerType() {
        assertNotNull(whaleCacheManager);
        assertInstanceOf(CaffeineWhaleCacheManager.class, whaleCacheManager);
    }

    @Test
    @DisplayName("Should register WhaleCacheManagerAdapter as Spring CacheManager")
    void testSpringCacheManagerAdapter() {
        assertNotNull(cacheManager);
        assertInstanceOf(WhaleCacheManagerAdapter.class, cacheManager);
    }

    @Test
    @DisplayName("Should bridge Spring CacheManager to WhaleCacheManager")
    void testCacheManagerBridge() {
        Cache springCache = cacheManager.getCache("test");
        assertNotNull(springCache);
        assertEquals("test", springCache.getName());

        springCache.put("key", "value");
        Cache.ValueWrapper wrapper = springCache.get("key");
        assertNotNull(wrapper);
        assertEquals("value", wrapper.get());
    }

    @Test
    @DisplayName("Should return empty collection for getCacheNames")
    void testGetCacheNames() {
        Collection<String> names = cacheManager.getCacheNames();
        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    @Test
    @DisplayName("Should use default TTL from properties")
    void testDefaultTtl(@Autowired WhaleCacheProperties properties) {
        assertEquals(WhaleCacheProperties.CacheType.LOCAL, properties.getType());
        assertNotNull(properties.getDefaultTtl());
    }
}