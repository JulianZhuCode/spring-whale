package io.github.springwhale.framework.core.cache.support;

import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;

public class WhaleCacheManagerAdapter implements CacheManager {

    private final WhaleCacheManager delegate;

    public WhaleCacheManagerAdapter(WhaleCacheManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public Cache getCache(String name) {
        return (Cache) delegate.getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.emptyList();
    }
}