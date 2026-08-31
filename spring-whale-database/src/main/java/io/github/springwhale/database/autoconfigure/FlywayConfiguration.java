package io.github.springwhale.database.autoconfigure;

import io.github.springwhale.database.flyway.FlywayMigrationRetryListener;
import io.github.springwhale.database.flyway.ResilientFlywayMigrationStrategy;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Auto-configuration for resilient Flyway migration.
 *
 * <p>Replaces the default {@link FlywayMigrationStrategy} with
 * {@link ResilientFlywayMigrationStrategy}, which catches migration errors
 * without blocking application startup. When the spring-whale-event framework
 * is not on the classpath, also registers a {@link FlywayMigrationRetryListener}
 * for Spring-based event-driven retry.</p>
 *
 * <p>When the event framework is present, retry is handled by
 * {@link FlywayEventRetryConfiguration} instead.</p>
 */
@AutoConfiguration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
@AutoConfigureBefore({FlywayAutoConfiguration.class})
public class FlywayConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy(DataSource dataSource,
                                                           @Value("${spring.application.name}") String serverName,
                                                           ApplicationEventPublisher eventPublisher) {
        return new ResilientFlywayMigrationStrategy(dataSource, serverName, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingClass("io.github.springwhale.framework.event.EventPublisher")
    public FlywayMigrationRetryListener flywayMigrationRetryListener(ResilientFlywayMigrationStrategy resilientFlywayMigrationStrategy) {
        return new FlywayMigrationRetryListener(resilientFlywayMigrationStrategy);
    }

}