package io.github.springwhale.framework.event.server.kafka;

import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.server.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.server.dao.EventConsumeFailedRecordDao;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaEventServerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KafkaEventConsumeFailedListener kafkaEventConsumeFailedListener(
            EventConsumeFailedRecordDao failedRecordDao,
            EventProperties eventProperties, ObjectMapper jsonMapper,
            RetryStrategyRegistry retryStrategyRegistry,
            List<EventMetricsCollector> metricsCollectors,
            List<EventConsumeTerminalHandler> terminalHandlers) {
        return new KafkaEventConsumeFailedListener(failedRecordDao, eventProperties,
                jsonMapper, retryStrategyRegistry, metricsCollectors, terminalHandlers);
    }

}