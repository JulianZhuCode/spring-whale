package io.github.springwhale.framework.core.cache;

public interface WhaleCacheManager {

    WhaleCache getCache(String name);
}