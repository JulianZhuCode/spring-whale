package io.github.springwhale.test.cache;

import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.cache.support.CaffeineWhaleCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CaffeineWhaleCacheManager")
class CaffeineWhaleCacheManagerTest {

    private CaffeineWhaleCacheManager manager;
    private WhaleCache cache;

    @BeforeEach
    void setUp() {
        WhaleCacheProperties properties = new WhaleCacheProperties();
        properties.setDefaultTtl(Duration.ofSeconds(10));
        properties.setNullValueTtl(Duration.ofSeconds(1));
        properties.setCacheNullValues(true);
        manager = new CaffeineWhaleCacheManager(properties);
        cache = manager.getCache("test");
    }

    @Nested
    @DisplayName("put and get")
    class PutAndGet {

        @Test
        @DisplayName("Should return cached value after put")
        void testPutAndGet() {
            cache.put("key1", "value1");
            assertEquals("value1", cache.get("key1", String.class));
        }

        @Test
        @DisplayName("Should return null for non-existent key")
        void testGetNonExistent() {
            assertNull(cache.get("nonexistent", String.class));
        }

        @Test
        @DisplayName("Should return cached object after put")
        void testPutAndGetObject() {
            TestDto dto = new TestDto("test", 42);
            cache.put("dto", dto);
            assertEquals(dto, cache.get("dto", TestDto.class));
        }
    }

    @Nested
    @DisplayName("get with loader")
    class GetWithLoader {

        @Test
        @DisplayName("Should load value and cache it when key not present")
        void testLoadAndCache() {
            AtomicInteger callCount = new AtomicInteger(0);

            String result1 = cache.get("loader1", String.class, () -> {
                callCount.incrementAndGet();
                return "loaded";
            });
            assertEquals("loaded", result1);
            assertEquals(1, callCount.get());

            String result2 = cache.get("loader1", String.class, () -> {
                callCount.incrementAndGet();
                return "loaded-again";
            });
            assertEquals("loaded", result2);
            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Should cache null value when loader returns null and cacheNullValues is true")
        void testCacheNullValue() {
            AtomicInteger callCount = new AtomicInteger(0);

            String result1 = cache.get("nullKey", String.class, () -> {
                callCount.incrementAndGet();
                return null;
            });
            assertNull(result1);
            assertEquals(1, callCount.get());

            String result2 = cache.get("nullKey", String.class, () -> {
                callCount.incrementAndGet();
                return "should-not-call";
            });
            assertNull(result2);
            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Should use custom TTL for per-key expiration")
        void testCustomTtl() {
            cache.get("shortTtl", String.class, () -> "short-lived", Duration.ofMillis(50));
            assertEquals("short-lived", cache.get("shortTtl", String.class));

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertNull(cache.get("shortTtl", String.class));
        }
    }

    @Nested
    @DisplayName("evict and clear")
    class EvictAndClear {

        @Test
        @DisplayName("Should remove cached value after evict")
        void testEvict() {
            cache.put("key1", "value1");
            assertTrue(cache.exists("key1"));

            cache.evict("key1");
            assertFalse(cache.exists("key1"));
            assertNull(cache.get("key1", String.class));
        }

        @Test
        @DisplayName("Should clear all cached values")
        void testClear() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            assertTrue(cache.exists("key1"));
            assertTrue(cache.exists("key2"));

            cache.clear();
            assertFalse(cache.exists("key1"));
            assertFalse(cache.exists("key2"));
        }
    }

    @Nested
    @DisplayName("cache namespaces")
    class Namespaces {

        @Test
        @DisplayName("Should isolate different cache namespaces")
        void testIsolation() {
            WhaleCache cacheA = manager.getCache("namespaceA");
            WhaleCache cacheB = manager.getCache("namespaceB");

            cacheA.put("key", "valueA");
            cacheB.put("key", "valueB");

            assertEquals("valueA", cacheA.get("key", String.class));
            assertEquals("valueB", cacheB.get("key", String.class));
        }
    }

    @Nested
    @DisplayName("Spring Cache interface")
    class SpringCacheInterface {

        @Test
        @DisplayName("Should support get with Callable valueLoader")
        void testGetWithCallable() {
            Cache springCache = (Cache) cache;
            AtomicInteger callCount = new AtomicInteger(0);

            String result = springCache.get("callableKey", (Callable<String>) () -> {
                callCount.incrementAndGet();
                return "from-callable";
            });
            assertEquals("from-callable", result);
            assertEquals(1, callCount.get());

            String cached = springCache.get("callableKey", (Callable<String>) () -> {
                callCount.incrementAndGet();
                return "should-not-call";
            });
            assertEquals("from-callable", cached);
            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Should support put and get via Spring Cache interface")
        void testPutAndGetViaSpringCache() {
            Cache springCache = (Cache) cache;
            springCache.put("springKey", "springValue");

            Cache.ValueWrapper wrapper = springCache.get("springKey");
            assertNotNull(wrapper);
            assertEquals("springValue", wrapper.get());
        }

        @Test
        @DisplayName("Should support evict via Spring Cache interface and clean TTL")
        void testEvictViaSpringCache() {
            cache.put("springEvictKey", "value", Duration.ofMinutes(5));
            assertTrue(cache.exists("springEvictKey"));

            Cache springCache = (Cache) cache;
            springCache.evict("springEvictKey");

            assertFalse(cache.exists("springEvictKey"));
        }
    }

    private record TestDto(String name, int age) {
    }
}