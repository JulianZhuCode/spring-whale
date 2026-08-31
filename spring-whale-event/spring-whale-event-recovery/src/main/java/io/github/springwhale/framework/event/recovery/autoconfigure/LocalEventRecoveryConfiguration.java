package io.github.springwhale.framework.event.recovery.autoconfigure;

import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.recovery.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import io.github.springwhale.framework.event.recovery.local.LocalEventConsumeFailedListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(name = "spring.whale.event.mode", havingValue = "local", matchIfMissing = true)
public class LocalEventRecoveryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LocalEventConsumeFailedListener localEventConsumeFailedListener(
            EventConsumeFailedRecordDao failedRecordDao,
            EventProperties eventProperties, ObjectMapper jsonMapper,
            RetryStrategyRegistry retryStrategyRegistry,
            List<EventMetricsCollector> metricsCollectors,
            List<EventConsumeTerminalHandler> terminalHandlers) {
        return new LocalEventConsumeFailedListener(failedRecordDao, eventProperties,
                jsonMapper, retryStrategyRegistry, metricsCollectors, terminalHandlers);
    }

}