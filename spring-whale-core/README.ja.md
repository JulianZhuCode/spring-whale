# spring-whale-core

spring-whale のコアフレームワークであり、すべての spring-whale モジュールの基盤です。

---

## モジュール構成

```
spring-whale-core
├── cache/               キャッシュ抽象化
├── context/              認証コンテキスト
├── enums/                基本列挙型インターフェース
├── exception/            ビジネス例外
├── json/                 JSON 直列化/逆直列化
├── model/                モデル
└── utils/                ユーティリティ
```

---

## コア機能

| 機能                   | クラス                                        | 説明                                                                          |
|----------------------|--------------------------------------------|-----------------------------------------------------------------------------|
| **キャッシュ抽象化** ⭐     | `cache/` パッケージ                             | ローカル（Caffeine）/ 分散（Redis）キャッシュ、キーごとの TTL、Spring Cache 互換 → [詳細](doc/cache.ja.md) |
| **JSON 直列化** ⭐      | `json/` パッケージ                              | マルチフォーマット時刻、列挙型 i18n、BigDecimal 精度、数値オーバーフロー保護 → [詳細](doc/json.ja.md) |
| **認証コンテキスト**         | `AuthenticationContextHolder` / `AuthUtil`  | ThreadLocal による現在のユーザー ID 情報（userId、username、tenantId）の保存                    |
| **統一 API レスポンス**     | `ApiResult<T>`                             | code + message + data をカプセル化し、`success()` / `error()` ファクトリメソッドを提供           |
| **ビジネス例外**           | `BusinessException`                        | errorCode、i18n（messageCode）、モジュール分類（module）、拡張データ（data）をサポート               |
| **基本列挙型**            | `BaseEnum`                                 | `getId()` / `getDesc()` 契約を定義し、JSON モジュールと連携して自動直列化と国際化を実現                  |
| **Spring コンテキスト**   | `SpringContextUtils`                       | Spring 管理外のクラスで Bean を取得                                                  |
| **Edge TTS**         | `EdgeTtsUtil`                              | edge-tts CLI を呼び出して音声合成、並行処理とタイムアウト設定をサポート                                |
| **日時フォーマット**         | `DateTimeFormats`                          | Date / LocalDate / LocalTime 向けの 40 種類以上の一般的なフォーマットパターン                   |
| **例外ユーティリティ**        | `ExceptionUtil`                            | 例外スタックトレースを文字列に変換                                                          |

---

## クイックスタート

### Maven 依存関係

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-core</artifactId>
</dependency>
```