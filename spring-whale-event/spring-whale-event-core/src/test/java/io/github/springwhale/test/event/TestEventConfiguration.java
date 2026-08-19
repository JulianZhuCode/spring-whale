package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.LogMetricsCollector;
import io.github.springwhale.framework.event.RetryStrategy;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.kafka.KafkaEventProperties;
import io.github.springwhale.framework.event.kafka.KafkaEventPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestEventConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> mock = mock(KafkaTemplate.class);
        return mock;
    }

    @Bean
    public EventProperties eventProperties() {
        return new EventProperties();
    }

    @Bean
    public KafkaEventProperties kafkaEventProperties() {
        return new KafkaEventProperties();
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public LogMetricsCollector logMetricsCollector() {
        return new LogMetricsCollector();
    }

    @Bean
    public RetryStrategyRegistry retryStrategyRegistry(Map<String, RetryStrategy> customStrategies) {
        return new RetryStrategyRegistry(customStrategies);
    }

    @Bean
    public KafkaEventPublisher kafkaEventPublisher(EventProperties properties, ObjectMapper jsonMapper,
                                                   List<EventMetricsCollector> metricsCollectors,
                                                   KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaEventPublisher(properties, jsonMapper, metricsCollectors, kafkaTemplate);
    }
}