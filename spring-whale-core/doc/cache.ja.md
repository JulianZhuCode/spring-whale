# Spring Whale キャッシュ

Spring Whale フレームワークは抽象キャッシュ層を提供し、モノリシック構成では Caffeine（ローカルキャッシュ）、
マイクロサービス構成では Redis（分散キャッシュ）にコード変更なしでシームレスに切り替えられます。

## 目次

- [機能概要](#機能概要)
- [アーキテクチャ](#アーキテクチャ)
- [設定](#設定)
- [プログラマティック API](#プログラマティック-api)
- [宣言的 API](#宣言的-api)
- [キャッシュペネトレーション保護](#キャッシュペネトレーション保護)
- [ベストプラクティス](#ベストプラクティス)
- [注意事項](#注意事項)

## 機能概要

### コア機能

- ✅ **ローカル/分散切替** - デフォルト Caffeine ローカルキャッシュ、設定一行で Redis に切替
- ✅ **キーごとの TTL** - 各キャッシュキーに独立した有効期限を設定可能
- ✅ **キャッシュペネトレーション保護** - null 値をプレースホルダーとして自動キャッシュし、ペネトレーションを防止
- ✅ **Spring Cache 互換** - `@Cacheable` / `@CacheEvict` / `@CachePut` アノテーションと完全互換
- ✅ **プログラマティック API** - `WhaleCacheManager` が詳細制御のための豊富な API を提供
- ✅ **自動設定** - クラスパスと設定に基づいて適切なキャッシュバックエンドを自動選択
- ✅ **スレッドセーフ** - すべてのキャッシュ操作はスレッドセーフに設計
- ✅ **モノリス零設定** - 単一サービス構成ではキャッシュ設定不要

### サポートされるキャッシュバックエンド

| バックエンド     | シナリオ            | デフォルト | 説明                                          |
|-------------|-----------------|-------|---------------------------------------------|
| **Caffeine** | モノリス / ローカル     | ✅    | 高性能 JVM ローカルキャッシュ、外部依存なし                     |
| **Redis**    | マイクロサービス        |       | 分散共有キャッシュ、マルチインスタンスのデータ整合性                  |

## アーキテクチャ

```
┌──────────────────────────────────────────────────────────────┐
│                       アプリケーションコード                      │
├───────────────────────────┬──────────────────────────────────┤
│   @Cacheable / @CachePut  │   WhaleCacheManager API          │
│   @CacheEvict             │   get() / put() / evict()        │
└───────────────────────────┴──────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │  WhaleCacheManager │
                    │     Adapter        │
                    └─────────┬─────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
   ┌──────────┴──────────┐     ┌──────────────┴──────────┐
   │ CaffeineWhaleCache  │     │  RedisWhaleCache        │
   │ Manager             │     │  Manager                │
   │ (モノリス - デフォルト) │     │  (マイクロサービス)         │
   └─────────────────────┘     └─────────────────────────┘
```

## 設定

### 設定ファイル

`application.yml` に以下の設定を追加します：

```yaml
spring:
  whale:
    cache:
      # キャッシュタイプ：LOCAL（デフォルト）または REDIS
      type: LOCAL
      # デフォルト有効期限（デフォルト：30m）
      default-ttl: 30m
      # Redis キープレフィックス（REDIS モードのみ有効）
      key-prefix: "whale:cache:"
      # null 値をキャッシュしてペネトレーションを防止するか（デフォルト：true）
      cache-null-values: true
      # null 値プレースホルダーの有効期限（デフォルト：1m）
      null-value-ttl: 1m
```

### モノリシックアーキテクチャ（デフォルト）

設定不要。Caffeine ローカルキャッシュが自動的に使用されます：

```yaml
# 設定不要 - Caffeine ローカルキャッシュが自動的に使用されます
# 以下と同等：
spring:
  whale:
    cache:
      type: LOCAL
```

### マイクロサービスアーキテクチャ

`spring-boot-starter-data-redis` 依存関係を追加し、Redis 接続を設定します：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  whale:
    cache:
      type: REDIS
      default-ttl: 10m          # マイクロサービスでは短めの TTL を推奨
      key-prefix: "whale:cache:"
      cache-null-values: true
      null-value-ttl: 1m

  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD}
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
```

### 設定項目

| 項目                   | 型       | デフォルト         | 説明                                    |
|----------------------|---------|---------------|---------------------------------------|
| `type`               | Enum    | LOCAL         | キャッシュタイプ：LOCAL=Caffeine、REDIS=Redis    |
| `default-ttl`        | Duration | 30m           | 全キャッシュキーのデフォルト有効期限                   |
| `key-prefix`         | String  | whale:cache:  | Redis キープレフィックス（キー衝突防止）               |
| `cache-null-values`  | boolean | true          | null 値をプレースホルダーとしてキャッシュするか            |
| `null-value-ttl`     | Duration | 1m            | null 値プレースホルダーの有効期限                  |

## プログラマティック API

`WhaleCacheManager` を注入してプログラマティック API を使用します：

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final WhaleCacheManager cacheManager;

    // ===== シンプルな読み書き =====
    
    public UserDto getUser(Long userId) {
        WhaleCache cache = cacheManager.getCache("user");

        // キャッシュから読み取り
        UserDto cached = cache.get("user:" + userId, UserDto.class);
        if (cached != null) {
            return cached;
        }

        UserDto user = userRepository.findById(userId);
        cache.put("user:" + userId, user);
        return user;
    }

    // ===== 読み取りまたはロード（ペネトレーション保護付き） =====
    
    public UserDto getUserV2(Long userId) {
        WhaleCache cache = cacheManager.getCache("user");
        return cache.get("user:" + userId, UserDto.class, () -> {
            return userRepository.findById(userId);
        });
    }

    // ===== キーごとの TTL =====

    public OrderDto getOrder(Long orderId) {
        WhaleCache cache = cacheManager.getCache("order");

        // ホットデータ：10 秒間有効
        return cache.get("order:" + orderId, OrderDto.class, () -> {
            return orderRepository.findById(orderId);
        }, Duration.ofSeconds(10));
    }

    // ===== 書き込み操作 =====

    public void updateUser(UserDto user) {
        WhaleCache cache = cacheManager.getCache("user");
        
        // デフォルト TTL で書き込み
        cache.put("user:" + user.getId(), user);
        
        // カスタム TTL で書き込み
        cache.put("user:" + user.getId(), user, Duration.ofMinutes(5));
    }

    public void deleteUser(Long userId) {
        WhaleCache cache = cacheManager.getCache("user");
        cache.evict("user:" + userId);
    }

    public void clearAllUsers() {
        cacheManager.getCache("user").clear();
    }
}
```

### API リファレンス

| メソッド | 説明 |
|------|------|
| `getCache(name)` | キャッシュ名前空間を取得または作成 |
| `get(key, type)` | キーでキャッシュ値を読み取り |
| `get(key, type, loader)` | デフォルト TTL で読み取りまたはロード |
| `get(key, type, loader, ttl)` | カスタム TTL で読み取りまたはロード |
| `put(key, value)` | デフォルト TTL で書き込み |
| `put(key, value, ttl)` | カスタム TTL で書き込み |
| `evict(key)` | 単一キャッシュエントリを削除 |
| `clear()` | 名前空間内の全エントリをクリア |
| `exists(key)` | キーの存在確認 |

## 宣言的 API

Spring の `@Cacheable` / `@CacheEvict` / `@CachePut` アノテーションと完全互換：

```java
@Service
public class OrderService {

    @Cacheable(value = "order", key = "#orderId")
    public OrderDto getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @CacheEvict(value = "order", key = "#orderId")
    public void deleteOrder(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @CachePut(value = "order", key = "#result.id")
    public OrderDto saveOrder(OrderDto dto) {
        return orderRepository.save(dto);
    }
}
```

> **注意**：`@Cacheable` はキーごとの TTL をサポートしていません。同じキャッシュ名前空間内の全キーが `defaultTtl` を共有します。
> キーごとの TTL が必要な場合はプログラマティック API を使用してください。

## キャッシュペネトレーション保護

`cache-null-values` が有効な場合（デフォルト有効）、loader が null を返すと自動的に null プレースホルダーがキャッシュされます：

```
リクエスト → キャッシュミス → Loader が null を返す → null プレースホルダーをキャッシュ（短い TTL） → null を返す
次のリクエスト → キャッシュヒット（null プレースホルダー） → 直接 null を返す（DB アクセスなし）
```

これにより、存在しないデータへの大量リクエストがキャッシュをバイパスしてデータベースに到達するのを防ぎます。

### 設定

```yaml
spring:
  whale:
    cache:
      cache-null-values: true   # null 値キャッシュを有効化（デフォルト有効）
      null-value-ttl: 1m        # null プレースホルダーの短い TTL
```

## ベストプラクティス

### 1. キャッシュキー設計

常にエンティティタイプと ID、およびバリエーションパラメータを含めます：

```java
// 推奨：一意で意味が明確
cache.put("user:" + userId, user);
cache.put("order:" + orderId + ":status:" + status, orders);

// 非推奨：衝突しやすい
cache.put(userId.toString(), user);
```

### 2. TTL 戦略

| データタイプ | 推奨 TTL | 理由 |
|-----------|---------|------|
| ユーザープロフィール | 30m ~ 1h | 変更頻度が低い |
| ユーザー権限 | 5m ~ 10m | 権限変更を迅速に反映する必要がある |
| 設定データ | 10m ~ 30m | 管理画面から変更される可能性がある |
| ホットデータ | 10s ~ 1m | 高同時実行、短い TTL で stale データを防止 |
| 参照データ | 1h ~ 24h | ほとんど変更されない |

### 3. モノリス vs マイクロサービス

| アーキテクチャ | キャッシュタイプ | TTL 推奨 |
|------------|----------|--------|
| モノリス | LOCAL（Caffeine） | 長めの TTL、一貫性の問題なし |
| マイクロサービス | REDIS | 短めの TTL（5m~10m）、マルチインスタンスのデータ整合性を確保 |

### 4. Spring Cache アノテーションの使用

読み取り操作には `@Cacheable`、削除操作には `@CacheEvict`、更新操作には `@CachePut` を使用します：

```java
@Cacheable(value = "product", key = "#productId")
public ProductDto getProduct(Long productId) { ... }

@CacheEvict(value = "product", key = "#productId")
public void deleteProduct(Long productId) { ... }

@CachePut(value = "product", key = "#result.id")
public ProductDto updateProduct(ProductDto dto) { ... }
```

## 注意事項

1. **Redis 依存関係**：`spring-whale-core` の `spring-boot-starter-data-redis` は `provided` スコープで宣言されています。
   モノリスプロジェクトでは追加不要、マイクロサービスプロジェクトでは明示的に追加してください。

2. **キー直列化**：Redis モードでは、値は Jackson で JSON に直列化されて保存されます。キャッシュオブジェクトが Jackson 直列化可能であることを確認してください。

3. **キャッシュ名前空間**：`getCache(name)` の各呼び出しは独立した名前空間を作成します。異なる名前空間のキーは衝突しません。

4. **スレッドセーフ**：すべてのキャッシュ操作はスレッドセーフです。Caffeine は内部的に ConcurrentHashMap を使用し、Redis は Lettuce コネクションプーリングを使用します。

5. **Spring Boot 4 互換性**：このモジュールは Spring Boot 4.x 向けに設計・テストされています。Spring Boot 4 では `CacheAutoConfiguration` が削除されたため、`WhaleCacheAutoConfiguration` が独自のアダプターを提供します。