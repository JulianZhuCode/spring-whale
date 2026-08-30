package io.github.springwhale.framework.event.autoconfigure;

import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.RetryStrategy;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Map;

@AutoConfiguration
public class SpringWhaleEventConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventProperties eventProperties() {
        return new EventProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryStrategyRegistry retryStrategyRegistry(Map<String, RetryStrategy> customStrategies) {
        return new RetryStrategyRegistry(customStrategies);
    }
}