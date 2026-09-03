<div align="center">

# Spring Whale 🐋

**Spring Boot 向けの汎用エンタープライズ開発フレームワーク**。迅速で標準化された、拡張可能なエンタープライズアプリケーション開発を実現します。

![JDK](https://img.shields.io/badge/JDK-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.x-brightgreen)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

[English](README.md) · [中文](README.zn.md) · **日本語**

</div>

---

エンタープライズシステムを作ったことがある方なら実感されているはずです。本当に時間を取られるのは業務ロジックではなく、どのプロジェクトでも書き直すことになる「土台」の部分——API のレスポンス形式、例外処理、認証、データスコープのフィルタリング、イベントを発行したのに誰も消費しなかったらどうするか、といったものです。どれも書くのは難しくありませんが、1 箇所でも漏れれば本番事故になります。

Spring Whale は、こうした汎用的でミスしやすい課題をあらかじめ解決しておき、すべて **jar 依存**として提供します。依存を追加するだけで Spring Boot の自動構成により動作し、Web、データベース、キャッシュ、イベント、管理コンソールなど、エンタープライズ開発のよくある共通関心ごとをカバーします。詳細は下記のモジュールナビゲーションをご覧ください。

さらに platform モジュールとして、オートインで使えるシンプルな業務機能も同梱しています。中規模・小規模プロジェクトではそのまま利用できるほか、業務モジュールを作る際の実装リファレンスとしても活用できます。

## モジュールナビゲーション

**コアフレームワーク**（継続的に進化、API の安定性をコミット）：

| モジュール | 役割 | ドキュメント |
|------|------|------|
| spring-whale-core | キャッシュ抽象（Caffeine / Redis）、JSON シリアライズ（Enum i18n・日時・BigDecimal）、BusinessException、ApiResult | [ドキュメント](spring-whale-core/README.ja.md) |
| spring-whale-webmvc | JWT 認証（Header/Cookie 二系統 + Feign 伝播）、グローバル例外処理、レスポンス自動ラップ、i18n、Security SPI | [ドキュメント](spring-whale-webmvc/README.ja.md) |
| spring-whale-database | JpaQueryWrapper チェーンクエリ、6 段階のデータスコープ、SQL レベルのマルチテナント、Flyway 耐障害マイグレーション、BaseEntity | [ドキュメント](spring-whale-database/README.ja.md) |
| spring-whale-event | Local / Kafka / RabbitMQ 統合イベント API、失敗時の永続化リトライ、イベントバージョニング、Metrics SPI | [ドキュメント](spring-whale-event/README.ja.md) |
| spring-whale-thymeleaf | プラガブルな管理コンソール、メニュー SPI、権限連動 UI、宣言的 CRUD コンポーネント | [ドキュメント](spring-whale-thymeleaf/README.ja.md) |

**プラットフォーム参考実装**（そのまま使えるシンプルな業務機能。フレームワーク機能の組み合わせ方の実例でもあります）：

| モジュール | 役割 | ドキュメント |
|------|------|------|
| spring-whale-platform-rbac | ユーザー / ロール / メニュー / 部署の RBAC。データスコープと連動、Thymeleaf 管理画面付き | [ドキュメント](spring-whale-platform/rbac/README.ja.md) |
| spring-whale-platform-task | バッチタスクエンジン：進捗管理、中断点からの再開、失敗リトライ、仮想スレッド並列、管理画面付き | [ドキュメント](spring-whale-platform/task/README.ja.md) |

## クイックスタート

**方法 1：親プロジェクトを継承**（推奨。バージョンが一元管理され依存に version が不要。親には Spring Boot / Spring Cloud BOM が同梱されています）：

```xml
<parent>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale</artifactId>
    <version>1.0.0</version>
</parent>

<dependencies>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-database</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-event-core</artifactId>
    </dependency>
</dependencies>
```

**方法 2：依存を直接追加**（親を継承しない場合はバージョンを個別に管理）：

```xml
<properties>
    <spring-whale.version>1.0.0</spring-whale.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-webmvc</artifactId>
        <version>${spring-whale.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-database</artifactId>
        <version>${spring-whale.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-event-core</artifactId>
        <version>${spring-whale.version}</version>
    </dependency>
</dependencies>
```

すべてのモジュールは Spring Boot の自動構成で有効化され、デフォルトではゼロ設定で起動します。DDL スクリプトは各モジュールの `src/main/resources/db/migration/` にあり、Flyway 利用時は自動でマイグレーションされ、未使用の場合は順番に手動実行してください。詳細な設定は各モジュールのドキュメントを参照してください。

## 技術スタック

| 分類 | 技術 |
|------|------|
| 言語 / フレームワーク | Java 25 · Spring Boot 4.1.x · Spring Cloud 2025.1.x |
| 永続化 | Spring Data JPA (Hibernate) · Flyway · Druid · PostgreSQL |
| キャッシュ | Caffeine（ローカル）· Redis（分散） |
| メッセージング | Spring Events · Kafka · RabbitMQ |
| 認証 | Spring Security · JJWT |
| 管理画面 | Thymeleaf · Bootstrap 5 |

## Roadmap

| バージョン | 内容 |
|------|------|
| **1.0.0** ✅ | 5 つのコアモジュールの API が安定。RBAC / バッチタスクの参考実装 |
| 1.1 | WebSocket 対応 |
| 1.2 | ワークフローオーケストレーション（業務ステップ / タスク / イベントを組み合わせた連携） |

## 導入事例

- [jp-cn-dict](https://github.com/JulianZhuCode/jp-cn-dict) — Spring Whale で構築された日中辞書アプリケーション（語彙・文法・例文の管理、Edge TTS 発音）。

## License

[Apache License 2.0](LICENSE)

Spring Whale はコミュニティ駆動のオープンソースプロジェクトであり、Spring Framework または VMware の公式プロダクトではありません。
