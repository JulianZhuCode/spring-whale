# Spring Whale グローバル例外処理

Spring Whale フレームワークは、Spring Boot の `@RestControllerAdvice` に基づく強力なグローバル例外処理機能を提供し、さまざまな一般的な例外の自動処理と国際化をサポートします。

## 目次

- [機能](#機能)
- [サポートされている例外タイプ](#サポートされている例外タイプ)
- [設定](#設定)
- [使用例](#使用例)
- [ベストプラクティス](#ベストプラクティス)

## 機能

### コア機能

- ✅ **統一レスポンス形式** - すべての例外が統一された `ApiResult` 形式を返します
- ✅ **複数の例外タイプ** - ビジネス例外、パラメータ検証例外、HTTP メソッド未サポートなどをサポート
- ✅ **国際化サポート** - エラーメッセージの多言語 i18n をサポート
- ✅ **階層型ロギング** - 例外タイプに基づいて異なるレベルを自動的にログ記録
- ✅ **エラーコード管理** - モジュール式エラーコード命名をサポート
- ✅ **拡張データサポート** - ビジネス例外は拡張データを携带できます
- ✅ **自動登録** - Spring Boot 経由で自動設定され、手動登録は不要です

### HTTP ステータスコードマッピング

| 例外タイプ | HTTP ステータス | 説明 |
|------------|----------------|------|
| `Exception` | 500 | サーバー内部エラー |
| `BusinessException` | 動的 | ビジネス例外、エラーコードはビジネスによって決定 |
| `IllegalArgumentException` | 400 | 違法引数例外 |
| `ValidationException` | 400 | JSR-303 検証例外 |
| `MethodArgumentNotValidException` | 400 | @Validated パラメータ検証失敗 |
| `BindException` | 400 | パラメータバインド失敗 |
| `HttpRequestMethodNotSupportedException` | 405 | HTTP メソッド未サポート |
| `DuplicateKeyException` | 409 | 重複キー（一意制約競合） |

## サポートされている例外タイプ

### 1. 一般例外

他のハンドラーによって処理されていないすべての例外をキャッチします：

```java
@ExceptionHandler(value = Exception.class)
public ApiResult<Boolean> handleException(Exception e)
```

**レスポンス例：**
```json
{
  "code": "500",
  "message": "サーバーエラーが発生しました。後でもう一度お試しください。",
  "data": false
}
```

**ログレベル：** ERROR

### 2. ビジネス例外

カスタムビジネクス例外を処理します：

```java
@ExceptionHandler(value = BusinessException.class)
public ApiResult<?> handleBusinessException(BusinessException e)
```

**レスポンス例：**
```json
{
  "code": "USER_MODULE_USER_NOT_FOUND",
  "message": "ユーザーが見つかりません",
  "data": {
    "userId": 123,
    "attemptedEmail": "test@example.com"
  }
}
```

**特徴：**
- モジュール式エラーコードをサポート（例：`user-module_USER_NOT_FOUND`）
- 拡張データの携带をサポート
- 国際化メッセージをサポート
- ログレベルは WARN で、過度の警告情報を回避

### 3. パラメータ検証例外

すべてのパラメータ検証関連の例外を処理します：

```java
@ExceptionHandler(value = {
    IllegalArgumentException.class,
    ValidationException.class,
    MethodArgumentNotValidException.class,
    BindException.class
})
public ApiResult<Boolean> handleIllegalArgumentException(Throwable e)
```

**レスポンス例：**
```json
{
  "code": "400",
  "message": "無効なリクエストパラメータ！",
  "data": false
}
```

**ログレベル：** WARN

### 4. HTTP メソッド未サポート

HTTP メソッドがサポートされていない場合を処理します：

```java
@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
public ApiResult<Boolean> handleHttpRequestMethodNotSupportedException(
    Throwable e
)
```

**レスポンス例：**
```json
{
  "code": "405",
  "message": "メソッドが許可されていません！",
  "data": false
}
```

**ログレベル：** WARN

### 5. 重複キー例外

データベースの一意制約競合などを処理します：

```java
@ExceptionHandler(value = DuplicateKeyException.class)
public ApiResult<Boolean> handleDuplicateKeyException(DuplicateKeyException e)
```

**レスポンス例：**
```json
{
  "code": "409",
  "message": "重複レコード！",
  "data": false
}
```

**ログレベル：** WARN

## 設定

### 設定ファイル

`application.yml` に以下の設定を追加します：

```yaml
spring:
  whale:
    web-mvc:
      exception:
        # 国際化を有効にする（デフォルト：false）
        enable-i18n: true
        # 500 エラーメッセージ
        message-500: サーバーエラーが発生しました。後でもう一度お試しください。
        code-500: http.error.500
        # 400 エラーメッセージ
        message-400: 無効なリクエストパラメータ！
        code-400: http.error.400
        # 405 エラーメッセージ
        message-405: メソッドが許可されていません！
        code-405: http.error.405
        # 409 エラーメッセージ
        message-409: 重複レコード！
        code-409: http.error.409
```

### 設定項目

| 項目 | タイプ | デフォルト | 説明 |
|------|--------|-----------|------|
| `enable-i18n` | boolean | false | 国際化メッセージを有効にする |
| `message-500` | String | サーバーエラー... | 500 エラーのデフォルトメッセージ |
| `code-500` | String | http.error.500 | 500 エラーの国際化キー |
| `message-400` | String | 無効なリクエスト... | 400 エラーのデフォルトメッセージ |
| `code-400` | String | http.error.400 | 400 エラーの国際化キー |
| `message-405` | String | メソッドが許可... | 405 エラーのデフォルトメッセージ |
| `code-405` | String | http.error.405 | 405 エラーの国際化キー |
| `message-409` | String | 重複レコード！ | 409 エラーのデフォルトメッセージ |
| `code-409` | String | http.error.409 | 409 エラーの国際化キー |

### 国際化設定

#### 1. 国際化リソースファイルの作成

`resources/messages.properties` (デフォルト):

```properties
http.error.500=Server abnormal, please try again later!
http.error.400=Invalid request parameters!
http.error.405=Method not allowed!
http.error.409=Duplicate records!
USER_NOT_FOUND=User not found
```

`resources/messages_zh_CN.properties` (中国語):

```properties
http.error.500=服务器异常，请稍后重试！
http.error.400=无效请求参数！
http.error.405=方法不允许！
http.error.409=重复记录！
USER_NOT_FOUND=用户不存在
ORDER_NOT_FOUND=订单不存在
```

`resources/messages_ja_JP.properties` (日本語):

```properties
http.error.500=サーバーエラーが発生しました
http.error.400=無効なリクエストパラメータ！
http.error.405=メソッドが許可されていません！
http.error.409=重複レコード！
USER_NOT_FOUND=ユーザーが存在しません
```

#### 2. 動的言語切り替え

```java
// リクエスト内でロケールを設定
LocaleContextHolder.setLocale(Locale.JAPAN);

// またはインターセプター経由でリクエストヘッダーに基づき設定
@GetMapping(value = "/users/{id}", headers = "Accept-Language=ja-JP")
public ResponseEntity<UserDTO> getUserJapanese(@PathVariable Long id) {
    LocaleContextHolder.setLocale(Locale.JAPAN);
    return ResponseEntity.ok(userService.getUser(id));
}
```

## 使用例

### 1. ビジネス例外のスロー

```java
import io.github.springwhale.framework.core.exception.BusinessException;

@Service
public class UserService {
    
    public User getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw BusinessException.create(
                "USER_NOT_FOUND",
                "ユーザーが見つかりません"
            );
        }
        return user;
    }
    
    public void createUser(User user) {
        // メールアドレスが既に存在するかチェック
        if (userRepository.existsByEmail(user.getEmail())) {
            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            throw BusinessException.create(
                "EMAIL_EXISTS",
                "メールアドレスが既に存在します",
                data
            );
        }
        userRepository.save(user);
    }
    
    public void updateUser(User user) {
        // モジュール名付きビジネス例外
        throw BusinessException.createWithModule(
            "USER_NOT_FOUND",
            "user-module",
            "ユーザーが見つかりません"
        );
    }
}
```

### 2. Controller での使用

```java
import io.github.springwhale.framework.core.model.ApiResult;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // 戻り値は自動的に ApiResult<User> にラップされます
        return userService.getUserById(id);
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        // 戻り値は自動的に ApiResult<User> にラップされます
        userService.createUser(user);
        return user;
    }
    
    @PutMapping("/{id}")
    public void updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        // void タイプは自動的に ApiResult.success() にラップされます
        userService.updateUser(user);
    }
    
    @GetMapping("/list")
    public List<User> listUsers() {
        // コレクションタイプも自動的にラップされます
        return userService.findAll();
    }
}
```

**自動変換：**
- `User` → `ApiResult<User>`
- `List<User>` → `ApiResult<List<User>>`
- `void` → `ApiResult<Boolean>` (値は true)
- `ApiResult<T>` → 変更なし（二重ラップなし）

### 3. パラメータ検証例外

```java
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping("/validate")
    public User createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // パラメータ検証に失敗すると、自動的に 400 エラーが返されます
        // 戻り値は自動的に ApiResult<User> にラップされます
        return userService.createUser(request.toEntity());
    }
    
    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam @NotBlank String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page) {
        // パラメータが空または無効な形式の場合、自動的に 400 エラーが返されます
        return userService.searchUsers(keyword, page);
    }
}

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "ユーザー名は必須です")
    private String username;
    
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が無効です")
    private String email;
    
    @NotBlank(message = "パスワードは必須です")
    private String password;
    
    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPassword(this.password);
        return user;
    }
}
```

**成功レスポンス：**
```json
{
  "code": "200",
  "message": "success",
  "data": {
    "id": 1,
    "username": "山田太郎",
    "email": "yamada@example.com"
  }
}
```

**検証失敗レスポンス：**
```json
{
  "code": "400",
  "message": "無効なリクエストパラメータ！",
  "data": false
}
```

### 4. 国際化付きビジネス例外

```java
@Service
public class OrderService {
    
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            // i18n キーを使用、実際のメッセージは国際化リソースファイルから取得
            throw BusinessException.createWithI18n(
                "ORDER_NOT_FOUND",
                "error.order.notfound",
                "注文が見つかりません"
            );
        }
        return order;
    }
    
    public void createOrder(Order order) {
        // モジュールとデータ付き i18n 例外
        Map<String, Object> data = new HashMap<>();
        data.put("productId", order.getProductId());
        throw BusinessException.createI18nWithModule(
            "PRODUCT_OUT_OF_STOCK",
            "error.product.outofstock",
            "order-module",
            "商品の在庫がありません",
            data
        );
    }
}
```

### 5. フロントエンド呼び出し例

```javascript
// 通常のリクエスト - 成功レスポンス
fetch('/api/users/1')
  .then(response => response.json())
  .then(data => {
    // data 構造：{ code: '200', message: 'success', data: {...} }
    if (data.code === '200') {
      console.log('ユーザー情報:', data.data);
    } else {
      console.error('ビジネスエラー:', data.message);
    }
  });

// ビジネス例外をキャッチ
fetch('/api/users/999')
  .then(response => response.json())
  .then(data => {
    // data 構造：{ code: 'USER_NOT_FOUND', message: 'ユーザーが見つかりません', data: null/false }
    if (data.code !== '200') {
      alert(`エラー [${data.code}]: ${data.message}`);
    }
  });

// パラメータ検証失敗
fetch('/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ invalid: 'data' })
})
.then(response => response.json())
.then(data => {
  // data 構造：{ code: '400', message: '無効なリクエストパラメータ！', data: false }
  if (data.code === '400') {
    alert('パラメータ検証失敗：' + data.message);
  }
});

// 作成成功 - ラップされたデータを返す
fetch('/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ 
    username: 'new_user',
    email: 'new@example.com',
    password: 'password123'
  })
})
.then(response => response.json())
.then(data => {
  // data 構造：{ code: '200', message: 'success', data: { id: 1, username: 'new_user', ... } }
  if (data.code === '200') {
    console.log('作成成功、ユーザー ID:', data.data.id);
  }
});

// 重複レコード例外をキャッチ
fetch('/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ 
    username: 'existing_user',
    email: 'existing@example.com',
    password: 'password123'
  })
})
.then(response => response.json())
.then(data => {
  // data 構造：{ code: '409', message: '重複レコード！', data: false }
  if (data.code === '409') {
    alert(data.message);
  }
});
```

**統一レスポンス形式：**

すべての Controller の戻り値は自動的に `ApiResult` 形式にラップされます：

| シナリオ | HTTP ステータス | レスポンス本文形式 |
|----------|----------------|-------------------|
| 成功 | 200 | `{ code: '200', message: 'success', data: {...} }` |
| ビジネス例外 | 200 | `{ code: 'ERROR_CODE', message: 'エラーメッセージ', data: null/false }` |
| パラメータ検証失敗 | 200 | `{ code: '400', message: '無効なリクエストパラメータ！', data: false }` |
| サーバーエラー | 200 | `{ code: '500', message: 'サーバーエラー', data: false }` |

**注意：** すべての例外は HTTP 200 を返し、`code` フィールドで区別します。

## ベストプラクティス

### 1. ビジネス例外使用基準

**推奨：**
```java
// ✅ 意味のあるエラーコードを使用
throw BusinessException.create("USER_NOT_FOUND", "ユーザーが見つかりません");

// ✅ モジュール式命名を使用
throw BusinessException.createWithModule(
    "NOT_FOUND", 
    "user-module", 
    "ユーザーが見つかりません"
);

// ✅ 拡張データを携带
Map<String, Object> data = Map.of("userId", userId);
throw BusinessException.create(
    "USER_NOT_FOUND", 
    "ユーザーが見つかりません", 
    data
);

// ✅ 国際化を使用
throw BusinessException.createWithI18n(
    "USER_NOT_FOUND",
    "error.user.notfound"
);
```

**非推奨：**
```java
// ❌ 意味のないエラーコード
throw BusinessException.create("ERROR_1", "エラーが発生しました");

// ❌ ハードコードされたエラーメッセージ
throw BusinessException.create("NO_PERMISSION", "このリソースにアクセスする権限がありません");

// ❌ 機密情報の漏洩
throw BusinessException.create("DB_ERROR", e.getMessage());
```

### 2. ログレベルの選択

例外ハンドラーには、組み込みの適切なログレベルがあります：

- **ERROR**: 不明な例外（即時対応が必要）
- **WARN**: ビジネス例外、パラメータ検証例外（通常のビジネスフロー）
- **DEBUG**: スタックトレース（デバッグ用）

ビジネスコードでログを重複して記録する必要はありません。

### 3. 国際化ベストプラクティス

**本番環境：**
```yaml
spring:
  whale:
    web-mvc:
      exception:
        enable-i18n: true
```

**開発環境：**
```yaml
spring:
  whale:
    web-mvc:
      exception:
        enable-i18n: false
```

**国際化リソースファイルの構成：**
```
resources/
├── messages.properties          # デフォルト（英語）
├── messages_zh_CN.properties   # 簡体字中国語
├── messages_zh_TW.properties   # 繁体字中国語
├── messages_ja_JP.properties   # 日本語
└── messages_ko_KR.properties   # 韓国語
```

### 4. エラーコード命名規則

**推奨されるエラーコード命名規則：**

```
[MODULE_]RESOURCE_ACTION
```

**例：**
- `USER_NOT_FOUND` - ユーザーが見つかりません
- `ORDER_CREATE_FAILED` - 注文作成に失敗
- `PAYMENT_INSUFFICIENT_BALANCE` - 支払い残高不足
- `user-module_USER_LOCKED` - ユーザーモジュール - ユーザーロック済み

### 5. テスト推奨事項

```java
import io.github.springwhale.framework.core.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ExceptionHandlerTest {
    
    @Autowired
    private UserController userController;
    
    @Test
    public void testUserNotFound() {
        ApiResult<User> result = userController.getUser(999L);
        
        assertEquals("USER_NOT_FOUND", result.getCode());
        assertNotNull(result.getMessage());
        assertFalse(result.getData());
    }
    
    @Test
    public void testInvalidParameter() {
        CreateUserRequest request = new CreateUserRequest();
        // 無効なデータを設定
        
        ApiResult<User> result = userController.createUser(request);
        
        assertEquals("400", result.getCode());
    }
}
```

## 注意事項

### 1. パフォーマンスの考慮

- 例外処理にはパフォーマンスオーバーヘッドがあります。真の例外状況に使用する必要があります
- 通常のビジネスロジックは、フロー制御に例外に依存すべきではありません
- 予期されるビジネス検証については、例外をスローする代わりに Service レイヤーから結果を返す必要があります

### 2. セキュリティ

**機密情報を公開しないでください：**
```java
// ❌ セキュリティ上問題あり - データベースエラーの詳細を公開
throw new RuntimeException(e.getMessage());

// ✅ セキュア - 汎用的なエラーメッセージを返す
throw BusinessException.create("SYSTEM_ERROR", "システムビジー、後でもう一度お試しください");
```

### 3. トランザクション処理

例外は自動的にトランザクションのロールバックをトリガーします（RuntimeException）：

```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    // 残高不足の場合 BusinessException をスロー
    // 自動的にトランザクション全体をロールバックします
    accountService.deduct(fromId, amount);
    accountService.add(toId, amount);
}
```

### 4. 非同期処理

非同期メソッド内の例外は、グローバル例外ハンドラーによってキャッチされない場合があります：

```java
// ❌ 非同期メソッドの例外はグローバル例外ハンドラーによってキャッチされません
@Async
public void asyncProcess() {
    throw BusinessException.create("ERROR", "非同期エラー");
}

// ✅ 非同期メソッド内で例外をキャッチして処理する必要があります
@Async
public CompletableFuture<Void> asyncProcess() {
    try {
        // 処理ロジック
    } catch (Exception e) {
        log.error("非同期処理に失敗しました", e);
        throw e;
    }
}
```

### 5. 他の例外ハンドラーとの優先順位

プロジェクト内で複数の `@RestControllerAdvice` が定義されている場合、`@Order` アノテーションを使用して優先順位を制御できます：

```java
@Component
@Order(1) // 数字が小さいほど優先度が高い
@RestControllerAdvice
public class CustomExceptionHandler {
    // カスタム例外ハンドラー
}
```

## 関連リソース

- [Spring Boot 例外処理公式ドキュメント](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-web-applications.spring-hateoas)
- [Spring MVC 例外処理](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-exceptionhandlers)
- [Bean Validation 仕様](https://beanvalidation.org/)
