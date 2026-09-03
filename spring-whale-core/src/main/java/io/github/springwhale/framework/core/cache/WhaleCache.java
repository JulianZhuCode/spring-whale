package io.github.springwhale.framework.core.cache;

import java.time.Duration;
import java.util.function.Supplier;

public interface WhaleCache {

    String getName();

    <T> T get(String key, Class<T> type);

    <T> T get(String key, Class<T> type, Supplier<T> loader);

    <T> T get(String key, Class<T> type, Supplier<T> loader, Duration ttl);

    void put(String key, Object value);

    void put(String key, Object value, Duration ttl);

    void evict(String key);

    void clear();

    boolean exists(String key);
}