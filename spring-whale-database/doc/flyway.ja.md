# Flyway 耐障害性マイグレーション

Spring Whale は、マイグレーション失敗がアプリケーション起動をブロックしない Flyway 耐障害性マイグレーション戦略を提供します。

## 目次

- [機能](#機能)
- [動作の仕組み](#動作の仕組み)
- [エラーログテーブル](#エラーログテーブル)
- [イベントリスニング](#イベントリスニング)
- [spring-whale-event との統合](#spring-whale-event-との統合)

## 機能

- ✅ **非ブロッキング起動** — マイグレーション失敗はログに記録されるがアプリケーション起動をブロックしない
- ✅ **エラーログ** — 失敗は自動的に `flyway_error_log` テーブルに記録
- ✅ **イベント駆動リトライ** — 失敗時に `FlywayMigrationEvent` を発行し、カスタムアラート/リトライロジックを可能に
- ✅ **オプションのイベントフレームワーク** — `spring-whale-event` とシームレスに統合し、永続化、リトライ、分散シナリオに対応

## 動作の仕組み

モジュールを含めるだけで自動的に有効になり、追加設定は不要です。マイグレーション失敗時、フレームワークは自動的に：

1. エラーログを `flyway_error_log` テーブルに書き込み
2. `FlywayMigrationEvent` を発行（リスナーでアラート可能）
3. アプリケーションの正常起動を許可し、マイグレーション失敗でブロックしない

## エラーログテーブル

`flyway_error_log` テーブルはマイグレーション失敗ログを記録します。最初のマイグレーションスクリプトで作成することを推奨します。

```sql
CREATE TABLE flyway_error_log
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_name VARCHAR(128),
    create_time TIMESTAMP,
    message     TEXT
);
```

## イベントリスニング

### Spring ネイティブイベント

デフォルトでは Spring のネイティブイベントメカニズムが使用されます。`FlywayMigrationEvent` をリスンするだけです：

```java
@Component
public class FlywayAlertListener implements ApplicationListener<FlywayMigrationEvent> {
    @Override
    public void onApplicationEvent(FlywayMigrationEvent event) {
        if (event.getType() == FlywayEventType.MIGRATION_FAILED) {
            // アラート通知を送信
        }
    }
}
```

### イベントタイプ

| イベントタイプ       | 説明               |
|---------------------|--------------------|
| `MIGRATION_STARTED` | マイグレーション開始  |
| `MIGRATION_SUCCESS` | マイグレーション成功  |
| `MIGRATION_FAILED`  | マイグレーション失敗  |

## spring-whale-event との統合

プロジェクトに `spring-whale-event-core` も含まれている場合、フレームワークは自動的に Flyway イベントをイベントフレームワークにブリッジします。追加設定は不要です：

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-event-core</artifactId>
</dependency>
```

含めると、`ApplicationListener` の代わりに `AbstractEventListener` を使用してイベントを消費できます：

```java
@Component
public class FlywayAlertListener extends AbstractEventListener<FlywayMigrationEvent> {
    @Override
    protected void onMessage(FlywayMigrationEvent event) {
        if (event.getType() == FlywayEventType.MIGRATION_FAILED) {
            // アラート通知を送信
        }
    }
}
```

> **注意：** イベントフレームワーク導入後は、`ApplicationListener` 実装は無効になります。`AbstractEventListener` を統一して使用してください。