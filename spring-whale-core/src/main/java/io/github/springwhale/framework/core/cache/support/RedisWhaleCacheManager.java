package io.github.springwhale.framework.core.cache.support;

import io.github.springwhale.framework.core.cache.WhaleCache;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.exception.CacheException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Slf4j
public class RedisWhaleCacheManager implements WhaleCacheManager {

    private static final String NULL_VALUE_MARKER = "\0__NULL__\0";

    private final WhaleCacheProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, RedisWhaleCache> caches = new ConcurrentHashMap<>();

    public RedisWhaleCacheManager(WhaleCacheProperties properties,
                                  StringRedisTemplate redisTemplate,
                                  ObjectMapper objectMapper) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public WhaleCache getCache(String name) {
        return caches.computeIfAbsent(name, n -> new RedisWhaleCache(n, properties, redisTemplate, objectMapper));
    }

    @Slf4j
    static class RedisWhaleCache implements WhaleCache, org.springframework.cache.Cache {

        private final String name;
        private final String keyPrefix;
        private final Duration defaultTtl;
        private final boolean cacheNullValues;
        private final Duration nullValueTtl;
        private final StringRedisTemplate redisTemplate;
        private final ObjectMapper objectMapper;

        RedisWhaleCache(String name, WhaleCacheProperties properties,
                        StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
            this.name = name;
            this.keyPrefix = properties.getKeyPrefix() + name + ":";
            this.defaultTtl = properties.getDefaultTtl();
            this.cacheNullValues = properties.isCacheNullValues();
            this.nullValueTtl = properties.getNullValueTtl();
            this.redisTemplate = redisTemplate;
            this.objectMapper = objectMapper;
        }

        private String buildKey(String key) {
            return keyPrefix + key;
        }

        private String serialize(Object value) {
            if (value == null) {
                return NULL_VALUE_MARKER;
            }
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JacksonException e) {
                throw new CacheException("Failed to serialize cache value for key prefix: " + keyPrefix, e);
            }
        }

        private <T> T deserialize(String json, Class<T> type) {
            if (json == null || NULL_VALUE_MARKER.equals(json)) {
                return null;
            }
            try {
                return objectMapper.readValue(json, type);
            } catch (JacksonException e) {
                throw new CacheException("Failed to deserialize cache value for key prefix: " + keyPrefix, e);
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public <T> T get(String key, Class<T> type) {
            String redisKey = buildKey(key);
            String json = redisTemplate.opsForValue().get(redisKey);
            return deserialize(json, type);
        }

        @Override
        public <T> T get(String key, Class<T> type, Supplier<T> loader) {
            return get(key, type, loader, null);
        }

        @Override
        public <T> T get(String key, Class<T> type, Supplier<T> loader, Duration ttl) {
            String redisKey = buildKey(key);
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json != null) {
                return deserialize(json, type);
            }

            T value = loader.get();
            Duration effectiveTtl = ttl != null ? ttl : defaultTtl;

            if (value != null) {
                redisTemplate.opsForValue().set(redisKey, serialize(value), effectiveTtl);
            } else if (cacheNullValues) {
                redisTemplate.opsForValue().set(redisKey, NULL_VALUE_MARKER, nullValueTtl);
            }
            return value;
        }

        @Override
        public void put(String key, Object value) {
            put(key, value, null);
        }

        @Override
        public void put(String key, Object value, Duration ttl) {
            String redisKey = buildKey(key);
            Duration effectiveTtl = ttl != null ? ttl : defaultTtl;
            redisTemplate.opsForValue().set(redisKey, serialize(value), effectiveTtl);
        }

        @Override
        public void evict(String key) {
            redisTemplate.delete(buildKey(key));
        }

        @Override
        public void clear() {
            redisTemplate.delete(redisTemplate.keys(keyPrefix + "*"));
        }

        @Override
        public boolean exists(String key) {
            return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(key)));
        }

        @Override
        public Object getNativeCache() {
            return redisTemplate;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object key, Class<T> type) {
            return get(key.toString(), type);
        }

        @Override
        public ValueWrapper get(Object key) {
            String redisKey = buildKey(key.toString());
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json == null) {
                return null;
            }
            Object value = NULL_VALUE_MARKER.equals(json) ? null : json;
            return () -> value;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(Object key, Callable<T> valueLoader) {
            String redisKey = buildKey(key.toString());
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json != null) {
                if (NULL_VALUE_MARKER.equals(json)) {
                    return null;
                }
                return (T) json;
            }

            try {
                T value = valueLoader.call();
                if (value != null) {
                    redisTemplate.opsForValue().set(redisKey, serialize(value), defaultTtl);
                } else if (cacheNullValues) {
                    redisTemplate.opsForValue().set(redisKey, NULL_VALUE_MARKER, nullValueTtl);
                }
                return value;
            } catch (Exception e) {
                throw new CacheException("Failed to load cache value for key: " + key, e);
            }
        }

        @Override
        public void put(Object key, Object value) {
            put(key.toString(), value);
        }

        @Override
        public void evict(Object key) {
            evict(key.toString());
        }
    }
}