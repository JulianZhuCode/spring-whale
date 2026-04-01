# Spring Whale レスポンスボディ自動ラッピング

Spring Whale フレームワークは、Spring MVC の `ResponseBodyAdvice` インターフェースをベースにしたインテリジェントなレスポンスボディ自動ラッピング機能を提供し、Controller メソッドの戻り値を統一された `ApiResult` 形式に自動的にラップします。

## 目次

- [機能特徴](#機能特徴)
- [仕組み](#仕組み)
- [サポートされている戻り値の型](#サポートされている戻り値の型)
- [@AdviceIgnore アノテーション](#adviceignore-アノテーション)
- [使用例](#使用例)
- [ベストプラクティス](#ベストプラクティス)

## 機能特徴

### コア機能

- ✅ **自動ラッピング** - 通常のオブジェクトは自動的に `ApiResult.success(data)` としてラップされます
- ✅ **スマート認識** - ラップ不要の型（`ApiResult`、`ResponseEntity` など）を自動認識します
- ✅ **統一フォーマット** - すべての成功レスポンスは統一された `ApiResult` 形式を返します
- ✅ **柔軟な制御** - アノテーションを通じて特定メソッドのラップをスキップできます
- ✅ **サブクラス対応** - `ResponseEntity` や `HttpEntity` のサブクラスも自動認識します
- ✅ **void サポート** - void メソッドは空の成功レスポンスを返します
- ✅ **自動登録** - Spring Boot による自動設定で、手動登録は不要です

### ラップルール

| 戻り値の型 | ラップされるか | 説明 |
|-----------|--------------|------|
| 通常のオブジェクト (POJO) | ✅ はい | `ApiResult.success(data)` としてラップ |
| String | ✅ はい | `ApiResult.success(data)` としてラップ |
| List/Set | ✅ はい | `ApiResult.success(data)` としてラップ |
| Map | ✅ はい | `ApiResult.success(data)` としてラップ |
| プリミティブ型 | ✅ はい | `ApiResult.success(data)` としてラップ |
| void | ✅ はい | `ApiResult.success()` を返す |
| ApiResult | ❌ いいえ | 直接返される、二重ラップされない |
| ResponseEntity | ❌ いいえ | 直接返される、元の動作を維持 |
| HttpEntity | ❌ いいえ | 直接返される、元の動作を維持 |
| ModelAndView | ❌ いいえ | 直接返される、ビューレンダリング用 |
| @AdviceIgnore が付与されたメソッド | ❌ いいえ | 直接返される、ラップをスキップ |

## 仕組み

`SpringWhaleWebMvcResponseBodyAdvice` は Spring MVC の `ResponseBodyAdvice<Object>` インターフェースを実装し、2 つのコアメソッドを通じてレスポンスボディの処理を制御します：

### 1. supports() メソッド

戻り値をラップするかどうかを判定します：

```java
@Override
public boolean supports(MethodParameter returnType, 
                       Class<? extends HttpMessageConverter<?>> converterType) {
    // 無視リストに含まれているか確認
    if (ignoreList.contains(returnClass)) {
        return false;
    }
    
    // @AdviceIgnore アノテーションがあるか確認
    if (returnType.hasMethodAnnotation(AdviceIgnore.class)) {
        return false;
    }
    
    // ResponseEntity または HttpEntity のサブクラスか確認
    if (ResponseEntity.class.isAssignableFrom(returnClass) ||
        HttpEntity.class.isAssignableFrom(returnClass)) {
        return false;
    }
    
    return true;
}
```

### 2. beforeBodyWrite() メソッド

レスポンスボディを書き込む前にラップします：

```java
@Override
public Object beforeBodyWrite(Object body, MethodParameter returnType, 
                             MediaType selectedContentType, 
                             Class<? extends HttpMessageConverter<?>> selectedConverterType, 
                             ServerHttpRequest request, 
                             ServerHttpResponse response) {
    // void 型の場合は空の成功を返す
    if (returnTypeClass.equals(Void.TYPE)) {
        return ApiResult.success();
    }
    
    // その他の型は success としてラップ
    return ApiResult.success(body);
}
```

## サポートされている戻り値の型

### 1. 自動ラップされる型

#### 通常のオブジェクト

```java
@GetMapping("/user")
public User getUser() {
    return new User(1L, "John");
}
```

**実際のレスポンス：**
```json
{
  "code": "200",
  "message": "success",
  "data": {
    "id": 1,
    "name": "John"
  }
}
```

#### String 型

```java
@GetMapping("/hello")
public String hello() {
    return "Hello World";
}
```

**実際のレスポンス：**
```json
{
  "code": "200",
  "message": "success",
  "data": "Hello World"
}
```

#### List 型

```java
@GetMapping("/users")
public List<User> listUsers() {
    return List.of(new User(1L, "John"), new User(2L, "Jane"));
}
```

**実際のレスポンス：**
```json
{
  "code": "200",
  "message": "success",
  "data": [
    {"id": 1, "name": "John"},
    {"id": 2, "name": "Jane"}
  ]
}
```

#### void 型

```java
@GetMapping("/success")
public void doSomething() {
    // 何らかの処理を実行
}
```

**実際のレスポンス：**
```json
{
  "code": "200",
  "message": "success",
  "data": true
}
```

### 2. ラップされない型

#### ApiResult 型

```java
@GetMapping("/api-result")
public ApiResult<User> getUser() {
    return ApiResult.success(new User(1L, "John"));
}
```

**実際のレスポンス：**
```json
{
  "code": "200",
  "message": "success",
  "data": {
    "id": 1,
    "name": "John"
  }
}
```

**注意：** 二重ラップされず、元の `ApiResult` が直接返されます。

#### ResponseEntity 型

```java
@GetMapping("/response-entity")
public ResponseEntity<User> getUser() {
    return ResponseEntity.ok(new User(1L, "John"));
}
```

**実際のレスポンス：**
```json
{
  "id": 1,
  "name": "John"
}
```

**注意：** `ResponseEntity` はラップされず、User オブジェクトが直接返されます。HTTP ステータスコードをカスタマイズする必要がある場合に適しています。

#### HttpEntity 型

```java
@GetMapping("/http-entity")
public HttpEntity<User> getUser() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Custom-Header", "value");
    return new HttpEntity<>(new User(1L, "John"), headers);
}
```

**実際のレスポンス：**
```json
{
  "id": 1,
  "name": "John"
}
```

#### ResponseEntity サブクラス

```java
public class CustomResponseEntity<T> extends ResponseEntity<T> {
    public CustomResponseEntity(T body) {
        super(body, HttpStatus.OK);
    }
}

@GetMapping("/custom")
public CustomResponseEntity<User> getUser() {
    return new CustomResponseEntity<>(new User(1L, "John"));
}
```

**実際のレスポンス：**
```json
{
  "id": 1,
  "name": "John"
}
```

**注意：** `ResponseEntity` のサブクラスも自動的に認識され、除外されます。

## @AdviceIgnore アノテーション

`@AdviceIgnore` は、ラップする必要がないメソッドに付与するマーカーアノテーションです。

### アノテーション定義

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdviceIgnore {
}
```

### 使用ユースケース

特定の Controller メソッドが生データを返すようにしたい場合（自動ラップをスキップする場合）に、このアノテーションを使用します。

### 使用例

```java
@RestController
public class UserController {
    
    @GetMapping("/user")
    public User getUser() {
        return new User(1L, "John");
    }
    
    @GetMapping("/raw-user")
    @AdviceIgnore
    public User getRawUser() {
        return new User(1L, "John");
    }
}
```

**比較：**

- `/user` のレスポンス：`{"code":"200","message":"success","data":{"id":1,"name":"John"}}`
- `/raw-user` のレスポンス：`{"id":1,"name":"John"}`

### 注意点

- アノテーションは必ず**メソッド**に付与してください
- アノテーションの保持ポリシーは `RUNTIME` で、実行時にリフレクションを通じて検出可能です
- このアノテーションが付与されると、メソッドの戻り値は**直接返され**、`ApiResult` としてラップされません

## 使用例

### 基本的な例

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    
    // 自動ラップ
    @GetMapping("/object")
    public User getObject() {
        return new User(1L, "John");
    }
    
    // 自動ラップ
    @GetMapping("/string")
    public String getString() {
        return "Hello";
    }
    
    // 自動ラップ
    @GetMapping("/list")
    public List<User> getList() {
        return List.of(new User(1L, "John"));
    }
    
    // ラップなし（すでに ApiResult）
    @GetMapping("/api-result")
    public ApiResult<User> getApiResult() {
        return ApiResult.success(new User(1L, "John"));
    }
    
    // ラップなし（ResponseEntity）
    @GetMapping("/response-entity")
    public ResponseEntity<User> getResponseEntity() {
        return ResponseEntity.ok(new User(1L, "John"));
    }
    
    // ラップなし（アノテーション使用）
    @GetMapping("/raw")
    @AdviceIgnore
    public User getRaw() {
        return new User(1L, "John");
    }
}
```

### 高度な例

```java
@RestController
@RequestMapping("/api/users")
public class AdvancedUserController {
    
    @Autowired
    private UserService userService;
    
    // 標準的な成功レスポンス
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // 特定の HTTP ステータスコードを返す必要がある
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // カスタムレスポンス形式が必要
    @GetMapping("/special")
    @AdviceIgnore
    public Map<String, Object> getSpecialData() {
        Map<String, Object> result = new HashMap<>();
        result.put("custom", "format");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
```

## ベストプラクティス

### 1. 自動ラップを優先使用する

ほとんどの場合、単純なオブジェクトを返すだけで、自動ラップの利便性を享受できます：

```java
// ✅ 推奨
@GetMapping("/user")
public User getUser() {
    return userService.getUser();
}

// ❌ 非推奨（冗長）
@GetMapping("/user")
public ApiResult<User> getUser() {
    return ApiResult.success(userService.getUser());
}
```

### 2. ResponseEntity は必要な場合のみ使用する

HTTP ステータスコードやレスポンスヘッダーをカスタマイズする必要がある場合に `ResponseEntity` を使用します：

```java
// 201 Created を返す必要がある
@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) {
    User created = userService.create(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// カスタムレスポンスヘッダーを追加する必要がある
@GetMapping("/cached")
public ResponseEntity<User> getCachedUser() {
    User user = userService.getUser();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cache-Control", "max-age=3600");
    return ResponseEntity.ok().headers(headers).body(user);
}
```

### 3. @AdviceIgnore は慎重に使用する

本当に生データを返す必要がある場合のみ `@AdviceIgnore` を使用します：

```java
// ✅ 妥当なユースケース：フロントエンドが必要な特殊形式を返す
@GetMapping("/chart-data")
@AdviceIgnore
public Map<String, Object> getChartData() {
    Map<String, Object> chartData = new HashMap<>();
    chartData.put("labels", List.of("Jan", "Feb", "Mar"));
    chartData.put("values", List.of(10, 20, 30));
    return chartData;
}

// ❌ 非推奨：特別な理由がない
@GetMapping("/user")
@AdviceIgnore  // なぜ自動ラップを使わないのか？
public User getUser() {
    return userService.getUser();
}
```

### 4. 一貫性を保つ

同じプロジェクト内では、レスポンス形式の一貫性を保つようにします：

- ほとんどのエンドポイントでは自動ラップを使用
- 本当に必要なエンドポイントのみ `ResponseEntity` または `@AdviceIgnore` を使用
- 複数のレスポンス形式を混在させない（フロントエンドの複雑さが増すため）

### 5. API ドキュメントを記載する

`@AdviceIgnore` または特殊なレスポンス形式を使用するエンドポイントについては、API ドキュメントに明確に記載します：

```java
/**
 * チャートデータを取得
 * 
 * @return 特殊なチャートデータ形式（ラップされていない）
 * 
 * レスポンス例:
 * {
 *   "labels": ["Jan", "Feb", "Mar"],
 *   "values": [10, 20, 30]
 * }
 */
@GetMapping("/chart-data")
@AdviceIgnore
public Map<String, Object> getChartData() {
    // ...
}
```

## 技術詳細

### 自動設定

`SpringWhaleWebMvcResponseBodyAdvice` は Spring Boot の自動設定メカニズムを通じて自動的に登録されます：

```java
@RestControllerAdvice
public class SpringWhaleWebMvcResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    // ...
}
```

手動設定は一切不要で、`spring-whale-webmvc` 依存関係を導入するだけで使用できます。

### 無視リスト

`addIgnore()` メソッドを通じて、無視する型を動的に追加できます：

```java
@Autowired
private SpringWhaleWebMvcResponseBodyAdvice advice;

// カスタム無視型を追加
advice.addIgnore(MyCustomType.class);
```

### グローバル例外処理との連携

`ResponseBodyAdvice` はグローバル例外ハンドラー（`SpringWhaleWebMvcExceptionHandler`）と連携して動作します：

1. 正常レスポンス → `ResponseBodyAdvice` がラップ → `ApiResult.success()` を返す
2. 例外発生 → `ExceptionHandler` がキャッチ → `ApiResult.error()` を返す

この 2 つが協力して、レスポンス形式の統一性を実現しています。
