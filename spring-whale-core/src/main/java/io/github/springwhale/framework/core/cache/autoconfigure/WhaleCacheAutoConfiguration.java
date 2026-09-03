package io.github.springwhale.framework.core.cache.autoconfigure;

import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.cache.support.CaffeineWhaleCacheManager;
import io.github.springwhale.framework.core.cache.support.WhaleCacheManagerAdapter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@EnableConfigurationProperties(WhaleCacheProperties.class)
public class WhaleCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WhaleCacheManager.class)
    @ConditionalOnProperty(name = "spring.whale.cache.type", havingValue = "LOCAL", matchIfMissing = true)
    public CaffeineWhaleCacheManager caffeineWhaleCacheManager(WhaleCacheProperties properties) {
        return new CaffeineWhaleCacheManager(properties);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public WhaleCacheManagerAdapter whaleCacheManagerAdapter(WhaleCacheManager whaleCacheManager) {
        return new WhaleCacheManagerAdapter(whaleCacheManager);
    }
}