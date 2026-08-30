package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.LogMetricsCollector;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

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
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    public LogMetricsCollector logMetricsCollector() {
        return new LogMetricsCollector();
    }
}