package io.github.springwhale.framework.core.cache.autoconfigure;

import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.cache.support.RedisWhaleCacheManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@ConditionalOnClass({StringRedisTemplate.class})
@ConditionalOnProperty(name = "spring.whale.cache.type", havingValue = "REDIS")
@EnableConfigurationProperties(WhaleCacheProperties.class)
public class WhaleCacheRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WhaleCacheManager.class)
    public RedisWhaleCacheManager redisWhaleCacheManager(WhaleCacheProperties properties,
                                                         StringRedisTemplate stringRedisTemplate,
                                                         ObjectMapper objectMapper) {
        return new RedisWhaleCacheManager(properties, stringRedisTemplate, objectMapper);
    }
}