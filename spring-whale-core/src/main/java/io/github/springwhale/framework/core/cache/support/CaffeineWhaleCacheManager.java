package io.github.springwhale.framework.core.cache.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.exception.CacheException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Slf4j
public class CaffeineWhaleCacheManager implements WhaleCacheManager {

    private static final Object NULL_VALUE = new Object();

    private final WhaleCacheProperties properties;
    private final ConcurrentMap<String, CaffeineWhaleCache> caches = new ConcurrentHashMap<>();

    public CaffeineWhaleCacheManager(WhaleCacheProperties properties) {
        this.properties = properties;
    }

    @Override
    public WhaleCache getCache(String name) {
        return caches.computeIfAbsent(name, n -> new CaffeineWhaleCache(n, properties));
    }

    @Slf4j
    static class CaffeineWhaleCache implements WhaleCache, org.springframework.cache.Cache {

        private final String name;
        private final Duration defaultTtl;
        private final boolean cacheNullValues;
        private final Duration nullValueTtl;
        private final ConcurrentMap<String, Duration> ttlOverrides = new ConcurrentHashMap<>();
        private final Cache<String, Object> cache;

        CaffeineWhaleCache(String name, WhaleCacheProperties properties) {
            this.name = name;
            this.defaultTtl = properties.getDefaultTtl();
            this.cacheNullValues = properties.isCacheNullValues();
            this.nullValueTtl = properties.getNullValueTtl();

            this.cache = Caffeine.newBuilder()
                    .expireAfter(new Expiry<String, Object>() {
                        @Override
                        public long expireAfterCreate(String key, Object value, long currentTime) {
                            return resolveTtlNanos(key, value);
                        }

                        @Override
                        public long expireAfterUpdate(String key, Object value,
                                                      long currentTime, long currentDuration) {
                            return resolveTtlNanos(key, value);
                        }

                        @Override
                        public long expireAfterRead(String key, Object value,
                                                    long currentTime, long currentDuration) {
                            return currentDuration;
                        }
                    })
                    .build();
        }

        private static Object wrapNull(Object value) {
            return value != null ? value : NULL_VALUE;
        }

        @SuppressWarnings("unchecked")
        private static <T> T unwrapNull(Object value) {
            return value == NULL_VALUE ? null : (T) value;
        }

        private long resolveTtlNanos(String key, Object value) {
            if (value == NULL_VALUE) {
                return nullValueTtl.toNanos();
            }
            Duration ttl = ttlOverrides.getOrDefault(key, defaultTtl);
            return ttl.toNanos();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public <T> T get(String key, Class<T> type) {
            Object value = cache.getIfPresent(key);
            return unwrapNull(value);
        }

        @Override
        public <T> T get(String key, Class<T> type, Supplier<T> loader) {
            return get(key, type, loader, null);
        }

        @Override
        public <T> T get(String key, Class<T> type, Supplier<T> loader, Duration ttl) {
            if (ttl != null) {
                ttlOverrides.put(key, ttl);
            }
            Object value = cache.get(key, k -> {
                T loaded = loader.get();
                if (loaded == null && cacheNullValues) {
                    return NULL_VALUE;
                }
                return loaded;
            });
            return unwrapNull(value);
        }

        @Override
        public void put(String key, Object value) {
            put(key, value, null);
        }

        @Override
        public void put(String key, Object value, Duration ttl) {
            if (ttl != null) {
                ttlOverrides.put(key, ttl);
            }
            cache.put(key, wrapNull(value));
        }

        @Override
        public void evict(String key) {
            cache.invalidate(key);
            ttlOverrides.remove(key);
        }

        @Override
        public void clear() {
            cache.invalidateAll();
            ttlOverrides.clear();
        }

        @Override
        public boolean exists(String key) {
            return cache.getIfPresent(key) != null;
        }

        @Override
        public Object getNativeCache() {
            return cache;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object key, Class<T> type) {
            Object value = cache.getIfPresent(key.toString());
            return unwrapNull(value);
        }

        @Override
        public ValueWrapper get(Object key) {
            Object value = cache.getIfPresent(key.toString());
            if (value == null) {
                return null;
            }
            Object unwrapped = unwrapNull(value);
            return () -> unwrapped;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object key, Callable<T> valueLoader) {
            Object value = cache.get(key.toString(), k -> {
                try {
                    T loaded = valueLoader.call();
                    if (loaded == null && cacheNullValues) {
                        return NULL_VALUE;
                    }
                    return loaded;
                } catch (Exception e) {
                    throw new CacheException("Failed to load cache value for key: " + key, e);
                }
            });
            return unwrapNull(value);
        }

        @Override
        public void put(Object key, Object value) {
            cache.put(key.toString(), wrapNull(value));
        }

        @Override
        public void evict(Object key) {
            cache.invalidate(key.toString());
            ttlOverrides.remove(key.toString());
        }
    }
}