# Flyway 容错迁移

Spring Whale 提供 Flyway 容错迁移策略，防止迁移失败阻塞应用启动。

## 目录

- [特性](#特性)
- [工作原理](#工作原理)
- [错误日志表](#错误日志表)
- [事件监听](#事件监听)
- [集成 spring-whale-event](#集成-spring-whale-event)

## 特性

- ✅ **非阻塞启动** — 迁移失败记录日志但不阻塞应用启动
- ✅ **错误日志** — 失败自动记录到 `flyway_error_log` 表
- ✅ **事件驱动重试** — 失败时发布 `FlywayMigrationEvent`，支持自定义告警/重试逻辑
- ✅ **可选事件框架** — 无缝集成 `spring-whale-event`，支持持久化、重试和分布式场景

## 工作原理

引入模块后自动生效，无需额外配置。迁移失败时框架自动：

1. 将错误日志写入 `flyway_error_log` 表
2. 发布 `FlywayMigrationEvent` 事件（可监听该事件实现告警）
3. 允许应用正常启动，不因迁移失败而阻塞

## 错误日志表

`flyway_error_log` 表用于记录迁移失败日志，建议在首次迁移脚本中创建。

```sql
CREATE TABLE flyway_error_log
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_name VARCHAR(128),
    create_time TIMESTAMP,
    message     TEXT
);
```

## 事件监听

### Spring 原生事件

默认使用 Spring 原生事件机制，监听 `FlywayMigrationEvent` 即可：

```java
@Component
public class FlywayAlertListener implements ApplicationListener<FlywayMigrationEvent> {
    @Override
    public void onApplicationEvent(FlywayMigrationEvent event) {
        if (event.getType() == FlywayEventType.MIGRATION_FAILED) {
            // 发送告警通知
        }
    }
}
```

### 事件类型

| 事件类型             | 说明           |
|---------------------|---------------|
| `MIGRATION_STARTED` | 迁移已开始      |
| `MIGRATION_SUCCESS` | 迁移成功完成    |
| `MIGRATION_FAILED`  | 迁移失败        |

## 集成 spring-whale-event

当项目中同时引入 `spring-whale-event-core` 时，框架自动将 Flyway 事件桥接到事件框架，无需额外配置：

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-event-core</artifactId>
</dependency>
```

引入后可使用 `AbstractEventListener` 替代 `ApplicationListener` 消费事件：

```java
@Component
public class FlywayAlertListener extends AbstractEventListener<FlywayMigrationEvent> {
    @Override
    protected void onMessage(FlywayMigrationEvent event) {
        if (event.getType() == FlywayEventType.MIGRATION_FAILED) {
            // 发送告警通知
        }
    }
}
```

> **注意：** 引入事件框架后，`ApplicationListener` 实现将不再生效，请统一使用 `AbstractEventListener`。