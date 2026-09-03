package io.github.springwhale.framework.core.cache;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

public interface WhaleCache {

    String getName();

    <T> T get(String key, Class<T> type);

    <T> T get(String key, Class<T> type, Supplier<T> loader);

    <T> T get(String key, Class<T> type, Supplier<T> loader, Duration ttl);

    /**
     * Type-safe convenience method for retrieving a list from cache.
     *
     * <p>Java generics erase {@code List<Object>.class} to {@code List.class},
     * so a raw cast is unavoidable. This method encapsulates the cast in a single
     * place so callers don't need to suppress warnings.</p>
     */
    @SuppressWarnings("unchecked")
    default <T> List<T> getList(String key) {
        return (List<T>) get(key, List.class);
    }

    /**
     * Type-safe convenience method for retrieving a list from cache with a loader and TTL.
     *
     * @see #getList(String)
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default <T> List<T> getList(String key, Supplier<List<T>> loader, Duration ttl) {
        return (List<T>) get(key, List.class, (Supplier) loader, ttl);
    }

    void put(String key, Object value);

    void put(String key, Object value, Duration ttl);

    void evict(String key);

    void clear();

    boolean exists(String key);
}