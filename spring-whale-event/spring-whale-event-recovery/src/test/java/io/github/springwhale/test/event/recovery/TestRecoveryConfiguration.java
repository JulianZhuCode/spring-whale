package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.event.RetryStrategy;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.util.Map;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestRecoveryConfiguration {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public EventProperties eventProperties() {
        return new EventProperties();
    }

    @Bean
    public EventPublisher eventPublisher() {
        return mock(EventPublisher.class);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> mock = mock(KafkaTemplate.class);
        return mock;
    }

    @Bean
    public RetryStrategyRegistry retryStrategyRegistry(Map<String, RetryStrategy> customStrategies) {
        return new RetryStrategyRegistry(customStrategies);
    }

    @Bean
    public EventConsumeFailedRecordDao eventConsumeFailedRecordDao(DataSource dataSource) {
        return new EventConsumeFailedRecordDao(dataSource);
    }
}