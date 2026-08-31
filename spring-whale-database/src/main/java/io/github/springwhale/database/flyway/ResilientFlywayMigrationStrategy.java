package io.github.springwhale.database.flyway;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.ApplicationEventPublisher;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Flyway migration strategy that gracefully handles migration failures.
 *
 * <p>On migration failure, logs the error to the {@code flyway_error_log} table
 * (if the table exists) and publishes a {@link FlywayMigrationEvent} so the
 * application can continue starting. Failed migrations can be retried via
 * the {@link FlywayMigrationRetryListener}.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class ResilientFlywayMigrationStrategy implements FlywayMigrationStrategy {
    private static final String ERROR_INSERT_SQL = "INSERT INTO flyway_error_log (server_name, create_time, message) VALUES (?, ?, ?)";
    private final DataSource dataSource;
    private final String serverName;
    private final ApplicationEventPublisher eventPublisher;
    private Flyway flyway;

    @SneakyThrows
    @Override
    public void migrate(@NonNull Flyway flyway) {
        this.flyway = flyway;
        this.doMigrate();
    }

    public void retry() {
        if (flyway == null) {
            log.warn("Flyway not yet initialized, skipping retry");
            return;
        }
        this.doMigrate();
    }

    @SneakyThrows
    private void doMigrate() {
        try {
            flyway.migrate();
        } catch (FlywayException e) {
            log.error("Database migration script execution failed!", e);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement ps = connection.prepareStatement(ERROR_INSERT_SQL)) {
                ps.setString(1, serverName);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(3, e.getMessage());
                ps.execute();
            } catch (Exception ex) {
                log.error("Failed to persist flyway error log", ex);
            }
            eventPublisher.publishEvent(new FlywayMigrationEvent(this, FlywayEventType.MIGRATION_FAILED));
        }
    }
}