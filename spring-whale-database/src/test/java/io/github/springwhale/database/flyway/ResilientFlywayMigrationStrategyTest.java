package io.github.springwhale.database.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResilientFlywayMigrationStrategyTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Flyway flyway;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    private ResilientFlywayMigrationStrategy strategy;

    private static final String SERVER_NAME = "test-server";

    @BeforeEach
    void setUp() {
        strategy = new ResilientFlywayMigrationStrategy(dataSource, SERVER_NAME, eventPublisher);
    }

    @Test
    @DisplayName("Should init flyway reference and call doMigrate on migrate")
    void shouldInitFlywayAndDoMigrate() {
        strategy.migrate(flyway);

        verify(flyway).migrate();
    }

    @Test
    @DisplayName("Should skip retry when flyway is not yet initialized")
    void shouldSkipRetryWhenFlywayNotInitialized() {
        strategy.retry();

        verifyNoInteractions(flyway);
    }

    @Test
    @DisplayName("Should call doMigrate on retry when flyway is initialized")
    void shouldRetryWhenFlywayInitialized() {
        strategy.migrate(flyway);
        reset(flyway);

        strategy.retry();

        verify(flyway).migrate();
    }

    @Test
    @DisplayName("Should migrate successfully when no exception occurs")
    void shouldMigrateSuccessfully() {
        strategy.migrate(flyway);

        verify(flyway).migrate();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should log error and publish event on FlywayException")
    void shouldLogErrorAndPublishEventOnFlywayException() throws Exception {
        FlywayException exception = new FlywayException("Migration failed");
        doThrow(exception).when(flyway).migrate();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        strategy.migrate(flyway);

        verify(preparedStatement).setString(1, SERVER_NAME);
        verify(preparedStatement).setTimestamp(eq(2), any());
        verify(preparedStatement).setString(3, "Migration failed");
        verify(preparedStatement).execute();
        verify(connection).close();

        ArgumentCaptor<FlywayMigrationEvent> eventCaptor =
                ArgumentCaptor.forClass(FlywayMigrationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getType()).isEqualTo(FlywayEventType.MIGRATION_FAILED);
        assertThat(eventCaptor.getValue().getSource()).isSameAs(strategy);
    }

    @Test
    @DisplayName("Should still publish event when DB error log insert fails")
    void shouldPublishEventWhenDbInsertFails() throws Exception {
        FlywayException exception = new FlywayException("Migration failed");
        doThrow(exception).when(flyway).migrate();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB connection lost"));

        strategy.migrate(flyway);

        ArgumentCaptor<FlywayMigrationEvent> eventCaptor =
                ArgumentCaptor.forClass(FlywayMigrationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getType()).isEqualTo(FlywayEventType.MIGRATION_FAILED);
    }

    @Test
    @DisplayName("Should close connection even when prepared statement execution fails")
    void shouldCloseConnectionOnStatementFailure() throws Exception {
        FlywayException exception = new FlywayException("Migration failed");
        doThrow(exception).when(flyway).migrate();
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        doThrow(new SQLException("Insert failed")).when(preparedStatement).execute();

        strategy.migrate(flyway);

        verify(connection).close();
        verify(eventPublisher).publishEvent(any(FlywayMigrationEvent.class));
    }
}