# spring-whale-webmvc

Spring Whale Web MVC インフラストラクチャモジュール。Spring Boot 自動構成に基づき、RESTful サービス向けの統一レスポンスラッピング、JWT 認証、グローバル例外処理、i18n などを提供します。

---

## モジュール構造

```
spring-whale-webmvc
├── advice/                             レスポンスボディ自動ラッピング
├── autoconfigure/                      自動構成
├── exception/                          グローバル例外処理
└── security/                           JWT 認証
```

---

## コア機能

| 機能 | クラス | 説明 |
|------|--------|------|
| **レスポンス自動ラッピング** | `SpringWhaleWebMvcResponseBodyAdvice` | Controller の戻り値を自動で `ApiResult` にラップ、`@AdviceIgnore` でスキップ可能 → [詳細](doc/response-body-advice.ja.md) |
| **JWT 認証** | `SecurityAutoConfiguration` / `JwtAuthenticationFilter` | ステートレス認証、Header/Cookie デュアルチャネルトークン抽出、SPI 拡張、Feign 伝播 → [詳細](doc/security.ja.md) |
| **グローバル例外処理** | `SpringWhaleWebMvcExceptionHandler` | 例外を統一 `ApiResult` にマッピング、i18n エラーメッセージ対応 → [詳細](doc/exception.ja.md) |
| **i18n** | `SpringWhaleI18nAutoConfiguration` | Cookie による言語設定保持、`?lang=ja_JP` で切替、デフォルト無効 |
| **MessageSource 集約** | `SpringWhaleMessageSourceAutoConfiguration` | クラスパス上の全 `messages-*.properties` を統合、ResourceBundle の単一読み込み問題を解決 |

---

## クイックスタート

### Maven 依存関係

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-webmvc</artifactId>
</dependency>
```

### i18n を有効にする（オプション）

```yaml
spring:
  whale:
    i18n:
      enabled: true
```

### JWT シークレットを設定（本番環境）

```yaml
spring:
  whale:
    web-mvc:
      security:
        jwt-secret: ${JWT_SECRET}
        permit-all-urls:
          - /public/**
          - /api/login
```

---

## 設定リファレンス

| 設定キー | デフォルト | 説明 |
|----------|-----------|------|
| `spring.whale.web-mvc.security.jwt-secret` | 組み込みデフォルト | 本番環境では必ず変更 |
| `spring.whale.web-mvc.security.jwt-expiration` | `86400000` | トークン有効期限（ms） |
| `spring.whale.web-mvc.security.permit-all-urls` | `[]` | 認証不要 URL |
| `spring.whale.web-mvc.exception.enable-i18n` | `false` | 例外メッセージの i18n |
| `spring.whale.i18n.enabled` | `false` | i18n 自動構成を有効化 |