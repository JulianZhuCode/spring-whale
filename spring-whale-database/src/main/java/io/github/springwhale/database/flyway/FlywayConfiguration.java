package io.github.springwhale.database.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
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
    public FlywayMigrationRetryListener flywayMigrationRetryListener(ResilientFlywayMigrationStrategy resilientFlywayMigrationStrategy) {
        return new FlywayMigrationRetryListener(resilientFlywayMigrationStrategy);
    }

}