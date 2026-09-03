# Flyway Fault-Tolerant Migration

Spring Whale provides a fault-tolerant Flyway migration strategy that prevents migration failures from blocking
application startup.

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Error Log Table](#error-log-table)
- [Event Listening](#event-listening)
- [Integration with spring-whale-event](#integration-with-spring-whale-event)

## Features

- ✅ **Non-Blocking Startup** — Migration failures are logged but do not block application startup
- ✅ **Error Logging** — Failures are automatically recorded in the `flyway_error_log` table
- ✅ **Event-Driven Retry** — `FlywayMigrationEvent` is published on failure, enabling custom alert/retry logic
- ✅ **Optional Event Framework** — Seamlessly integrates with `spring-whale-event` for persistence, retry, and
  distributed scenarios

## How It Works

Automatically enabled upon module inclusion, no extra configuration required. On migration failure, the framework
automatically:

1. Writes error logs to the `flyway_error_log` table
2. Publishes a `FlywayMigrationEvent` (listeners can be attached for alerting)
3. Allows the application to start normally without blocking on migration failures

## Error Log Table

The `flyway_error_log` table records migration failure logs. It is recommended to create it in the first migration
script.

```sql
CREATE TABLE flyway_error_log
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_name VARCHAR(128),
    create_time TIMESTAMP,
    message     TEXT
);
```

## Event Listening

### Spring Native Event

By default, Spring's native event mechanism is used. Simply listen for `FlywayMigrationEvent`:

```java
@Component
public class FlywayAlertListener implements ApplicationListener<FlywayMigrationEvent> {
    @Override
    public void onApplicationEvent(FlywayMigrationEvent event) {
        if (event.getType() == FlywayEventType.MIGRATION_FAILED) {
            // Send alert notification
        }
    }
}
```

### Event Types

| Event Type          | Description                  |
|---------------------|------------------------------|
| `MIGRATION_STARTED` | Migration has started        |
| `MIGRATION_SUCCESS` | Migration completed successfully |
| `MIGRATION_FAILED`  | Migration failed             |

## Integration with spring-whale-event

When `spring-whale-event-core` is also present in the project, the framework automatically bridges Flyway events to the
event framework, no extra configuration required:

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-event-core</artifactId>
</dependency>
```

Once included, use `AbstractEventListener` instead of `ApplicationListener` to consume events:

```java
@Component
public class FlywayAlertListener extends AbstractEventListener<FlywayMigrationEvent> {
    @Override
    protected void onMessage(FlywayMigrationEvent event) {
        if (event.getType() == FlywayEventType.MIGRATION_FAILED) {
            // Send alert notification
        }
    }
}
```

> **Note:** After introducing the event framework, `ApplicationListener` implementations will no longer take effect.
> Use `AbstractEventListener` uniformly.