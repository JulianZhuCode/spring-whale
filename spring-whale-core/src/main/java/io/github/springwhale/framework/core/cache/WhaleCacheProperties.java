package io.github.springwhale.framework.core.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "spring.whale.cache")
public class WhaleCacheProperties {

    private CacheType type = CacheType.LOCAL;

    private Duration defaultTtl = Duration.ofMinutes(30);

    private String keyPrefix = "whale:cache:";

    private boolean cacheNullValues = true;

    private Duration nullValueTtl = Duration.ofMinutes(1);

    public enum CacheType {
        LOCAL,
        REDIS
    }
}