package io.github.springwhale.database.flyway;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

@Slf4j
@RequiredArgsConstructor
public class ResilientFlywayMigrationStrategy implements FlywayMigrationStrategy {
    private static final String ERROR_INSERT_SQL = "INSERT INTO flyway_error_log (server_name, create_time, message) VALUES (?, now(), ?);";
    private final DataSource dataSource;
    private final String serverName;
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
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement ps = connection.prepareStatement(ERROR_INSERT_SQL)) {
                    ps.setString(1, serverName);
                    ps.setString(2, e.getMessage());
                    ps.execute();
                }
            }
        }
    }
}
