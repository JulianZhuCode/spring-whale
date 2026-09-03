package io.github.springwhale.test.cache;

import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.cache.support.RedisWhaleCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("RedisWhaleCacheManager")
@ExtendWith(MockitoExtension.class)
class RedisWhaleCacheManagerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private ObjectMapper objectMapper;

    private RedisWhaleCacheManager manager;
    private WhaleCache cache;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);

        WhaleCacheProperties properties = new WhaleCacheProperties();
        properties.setDefaultTtl(Duration.ofSeconds(10));
        properties.setNullValueTtl(Duration.ofSeconds(1));
        properties.setCacheNullValues(true);
        properties.setKeyPrefix("whale:cache:");

        manager = new RedisWhaleCacheManager(properties, redisTemplate, objectMapper);
        cache = manager.getCache("test");
    }

    @Nested
    @DisplayName("put and get")
    class PutAndGet {

        @Test
        @DisplayName("Should return cached value from Redis")
        void testGetHit() {
            when(valueOps.get("whale:cache:test:key1")).thenReturn("\"value1\"");

            String result = cache.get("key1", String.class);
            assertEquals("value1", result);
        }

        @Test
        @DisplayName("Should return null when key not in Redis")
        void testGetMiss() {
            when(valueOps.get("whale:cache:test:key1")).thenReturn(null);

            String result = cache.get("key1", String.class);
            assertNull(result);
        }

        @Test
        @DisplayName("Should put value to Redis with default TTL")
        void testPut() {
            cache.put("key1", "value1");

            verify(valueOps).set(eq("whale:cache:test:key1"), eq("\"value1\""), eq(Duration.ofSeconds(10)));
        }

        @Test
        @DisplayName("Should put value to Redis with custom TTL")
        void testPutWithTtl() {
            cache.put("key1", "value1", Duration.ofSeconds(5));

            verify(valueOps).set(eq("whale:cache:test:key1"), eq("\"value1\""), eq(Duration.ofSeconds(5)));
        }
    }

    @Nested
    @DisplayName("get with loader")
    class GetWithLoader {

        @Test
        @DisplayName("Should return cached value and skip loader")
        void testCacheHit() {
            when(valueOps.get("whale:cache:test:loader1")).thenReturn("\"cached\"");

            AtomicInteger callCount = new AtomicInteger(0);
            String result = cache.get("loader1", String.class, () -> {
                callCount.incrementAndGet();
                return "loaded";
            });

            assertEquals("cached", result);
            assertEquals(0, callCount.get());
        }

        @Test
        @DisplayName("Should load value and cache when miss")
        void testCacheMiss() {
            when(valueOps.get("whale:cache:test:loader1")).thenReturn(null);

            String result = cache.get("loader1", String.class, () -> "loaded");

            assertEquals("loaded", result);
            verify(valueOps).set(eq("whale:cache:test:loader1"), eq("\"loaded\""), eq(Duration.ofSeconds(10)));
        }

        @Test
        @DisplayName("Should cache null marker when loader returns null")
        void testNullValue() {
            when(valueOps.get("whale:cache:test:nullKey")).thenReturn(null);

            String result = cache.get("nullKey", String.class, () -> null);

            assertNull(result);
            verify(valueOps).set(eq("whale:cache:test:nullKey"), eq("\0__NULL__\0"), eq(Duration.ofSeconds(1)));
        }
    }

    @Nested
    @DisplayName("evict and exists")
    class EvictAndExists {

        @Test
        @DisplayName("Should delete key from Redis")
        void testEvict() {
            cache.evict("key1");

            verify(redisTemplate).delete("whale:cache:test:key1");
        }

        @Test
        @DisplayName("Should check key existence in Redis")
        void testExists() {
            when(redisTemplate.hasKey("whale:cache:test:key1")).thenReturn(true);
            when(redisTemplate.hasKey("whale:cache:test:key2")).thenReturn(false);

            assertTrue(cache.exists("key1"));
            assertFalse(cache.exists("key2"));
        }
    }

    @Nested
    @DisplayName("Spring Cache interface")
    class SpringCacheInterface {

        @Test
        @DisplayName("Should support get with Callable valueLoader")
        void testGetWithCallable() {
            Cache springCache = (Cache) cache;
            when(valueOps.get("whale:cache:test:callableKey")).thenReturn(null);

            String result = springCache.get("callableKey", (Callable<String>) () -> "from-callable");
            assertEquals("from-callable", result);
            verify(valueOps).set(eq("whale:cache:test:callableKey"), eq("\"from-callable\""), eq(Duration.ofSeconds(10)));
        }

        @Test
        @DisplayName("Should return cached value via Spring Cache get with ValueWrapper")
        void testGetValueWrapper() {
            Cache springCache = (Cache) cache;
            when(valueOps.get("whale:cache:test:vwKey")).thenReturn("\"vwValue\"");

            Cache.ValueWrapper wrapper = springCache.get("vwKey");
            assertNotNull(wrapper);
            assertEquals("\"vwValue\"", wrapper.get());
        }

        @Test
        @DisplayName("Should return null when key not in Redis via Spring Cache get")
        void testGetValueWrapperMiss() {
            Cache springCache = (Cache) cache;
            when(valueOps.get("whale:cache:test:missing")).thenReturn(null);

            Cache.ValueWrapper wrapper = springCache.get("missing");
            assertNull(wrapper);
        }

        @Test
        @DisplayName("Should support put via Spring Cache interface")
        void testPutViaSpringCache() {
            Cache springCache = (Cache) cache;
            springCache.put("springKey", "springValue");

            verify(valueOps).set(eq("whale:cache:test:springKey"), eq("\"springValue\""), eq(Duration.ofSeconds(10)));
        }

        @Test
        @DisplayName("Should support evict via Spring Cache interface")
        void testEvictViaSpringCache() {
            Cache springCache = (Cache) cache;
            springCache.evict("springEvictKey");

            verify(redisTemplate).delete("whale:cache:test:springEvictKey");
        }
    }
}