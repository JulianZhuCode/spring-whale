package io.github.springwhale.framework.event.recovery.autoconfigure;

import io.github.springwhale.framework.event.EventMetricsCollector;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.event.recovery.EventRecoveryManager;
import io.github.springwhale.framework.event.recovery.EventRetryTask;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@AutoConfiguration
@EnableScheduling
public class SpringWhaleEventServerConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventConsumeFailedRecordDao eventConsumeFailedRecordDao(JdbcTemplate jdbcTemplate) {
        return new EventConsumeFailedRecordDao(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventRetryTask eventRetryTask(EventConsumeFailedRecordDao recordDao,
                                         EventProperties eventProperties,
                                         EventPublisher eventPublisher,
                                         ObjectMapper jsonMapper,
                                         List<EventMetricsCollector> metricsCollectors) {
        return new EventRetryTask(recordDao, eventProperties, eventPublisher, jsonMapper, metricsCollectors);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventRecoveryManager eventRecoveryManager(EventConsumeFailedRecordDao recordDao) {
        return new EventRecoveryManager(recordDao);
    }
}