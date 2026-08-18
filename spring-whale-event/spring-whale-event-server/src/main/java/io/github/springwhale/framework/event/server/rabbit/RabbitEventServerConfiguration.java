package io.github.springwhale.framework.event.server.rabbit;

import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.server.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.server.repository.EventConsumeFailedRecordRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitEventServerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RabbitEventConsumeFailedListener rabbitEventConsumeFailedListener(
            EventConsumeFailedRecordRepository failedRecordRepository,
            EventProperties eventProperties, ObjectMapper jsonMapper,
            RetryStrategyRegistry retryStrategyRegistry,
            List<EventMetricsCollector> metricsCollectors,
            List<EventConsumeTerminalHandler> terminalHandlers) {
        return new RabbitEventConsumeFailedListener(failedRecordRepository, eventProperties,
                jsonMapper, retryStrategyRegistry, metricsCollectors, terminalHandlers);
    }

}