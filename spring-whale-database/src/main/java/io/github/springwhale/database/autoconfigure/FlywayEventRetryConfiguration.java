package io.github.springwhale.database.autoconfigure;

import io.github.springwhale.database.flyway.FlywayEventRetryListener;
import io.github.springwhale.database.flyway.FlywayMigrationEventBridge;
import io.github.springwhale.database.flyway.ResilientFlywayMigrationStrategy;
import io.github.springwhale.framework.event.EventPublisher;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({Flyway.class, EventPublisher.class})
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
@AutoConfigureAfter({FlywayConfiguration.class})
public class FlywayEventRetryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlywayMigrationEventBridge flywayMigrationEventBridge(EventPublisher eventPublisher) {
        return new FlywayMigrationEventBridge(eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlywayEventRetryListener flywayMigrationEventRetryListener(ResilientFlywayMigrationStrategy resilientFlywayMigrationStrategy) {
        return new FlywayEventRetryListener(resilientFlywayMigrationStrategy);
    }

}