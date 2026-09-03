package io.github.springwhale.test.cache;

import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import io.github.springwhale.framework.core.cache.WhaleCacheProperties;
import io.github.springwhale.framework.core.cache.autoconfigure.WhaleCacheRedisAutoConfiguration;
import io.github.springwhale.framework.core.cache.support.RedisWhaleCacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("WhaleCacheRedisAutoConfiguration")
class WhaleCacheRedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
            .withBean(tools.jackson.databind.ObjectMapper.class, tools.jackson.databind.ObjectMapper::new)
            .withUserConfiguration(WhaleCacheRedisAutoConfiguration.class);

    @Test
    @DisplayName("Should not register RedisWhaleCacheManager when type is LOCAL")
    void testShouldNotRegisterWhenTypeLocal() {
        contextRunner
                .withPropertyValues("spring.whale.cache.type=LOCAL")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RedisWhaleCacheManager.class);
                });
    }

    @Test
    @DisplayName("Should register RedisWhaleCacheManager when type is REDIS")
    void testShouldRegisterWhenTypeRedis() {
        contextRunner
                .withPropertyValues(
                        "spring.whale.cache.type=REDIS",
                        "spring.whale.cache.key-prefix=whale:test:",
                        "spring.whale.cache.default-ttl=5m",
                        "spring.whale.cache.null-value-ttl=30s")
                .run(context -> {
                    assertThat(context).hasSingleBean(RedisWhaleCacheManager.class);
                    assertThat(context).hasSingleBean(WhaleCacheManager.class);

                    WhaleCacheProperties props = context.getBean(WhaleCacheProperties.class);
                    assertThat(props.getType()).isEqualTo(WhaleCacheProperties.CacheType.REDIS);
                    assertThat(props.getKeyPrefix()).isEqualTo("whale:test:");
                });
    }
}