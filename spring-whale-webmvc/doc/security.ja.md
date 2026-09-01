# Spring Whale JWT 認証

Spring Whale は、Spring Security と JWT に基づくステートレス認証をすぐに利用できる形で提供し、Header/Cookie のデュアルチャネルトークン抽出、SPI 拡張メカニズム、および Feign サービス間トークン伝播をサポートします。

---

## 目次

- [アーキテクチャ概要](#アーキテクチャ概要)
- [設定](#設定)
- [認証フロー](#認証フロー)
- [トークン管理](#トークン管理)
- [SPI 拡張](#spi-拡張)
- [Feign 伝播](#feign-伝播)
- [使用例](#使用例)
- [ベストプラクティス](#ベストプラクティス)

---

## アーキテクチャ概要

```
リクエスト → JwtAuthenticationFilter → SecurityContextHolder (Spring Security)
                                     → AuthenticationContextHolder (ビジネスコンテキスト)
                                     → SecurityFilterChain (認可)
```

**コアコンポーネント：**

| コンポーネント | 役割 |
|--------------|------|
| `SecurityAutoConfiguration` | SecurityFilterChain の組み立て、ステートレスセッション、BCrypt エンコーディング、CORS の設定 |
| `JwtAuthenticationFilter` | OncePerRequestFilter、JWT を抽出・検証し、デュアル認証コンテキストを設定 |
| `JwtUtil` | HMAC-SHA 署名、トークンの生成/解析/検証 |
| `SecurityProperties` | すべてのセキュリティ設定、プレフィックス `spring.whale.web-mvc.security` |
| `SecurityConfigProvider` | 下流モジュールが認証不要 URL とカスタム HttpSecurity 設定を宣言するための SPI インターフェース |
| `SecurityFeignInterceptor` | Feign 呼び出し時に現在のリクエストから JWT を自動抽出して伝播 |

**デュアル認証コンテキスト：**

- **Spring Security コンテキスト**：`SecurityContextHolder`、`@PreAuthorize` やロールチェックなどの標準 Spring Security 機能に使用
- **ビジネスコンテキスト**：`AuthenticationContextHolder`（ThreadLocal）、`AuthUtil` 経由で `userId`、`username`、`tenantId` を提供

---

## 設定

### 設定ファイル

```yaml
spring:
  whale:
    web-mvc:
      security:
        # JWT 署名キー（デフォルト：SpringWhaleSecretKey2024ForJWTTokenGeneration）
        jwt-secret: ${JWT_SECRET:your-secret-key-at-least-256-bits}
        # トークン有効期限（ミリ秒、デフォルト：86400000、24時間）
        jwt-expiration: 86400000
        # Authorization ヘッダー名（デフォルト：Authorization）
        token-header: Authorization
        # トークンプレフィックス（デフォルト：Bearer ）
        token-prefix: "Bearer "
        # トークン用 Cookie 名（デフォルト：sw_token）
        token-cookie-name: sw_token
        # CSRF を有効にするか（デフォルト：false）
        csrf-enabled: false
        # 認証不要 URL リスト
        permit-all-urls:
          - /public/**
          - /api/login
        
        
```

### 設定項目

| 項目 | タイプ | デフォルト | 説明 |
|------|--------|-----------|------|
| `jwt-secret` | String | `SpringWhaleSecretKey2024ForJWTTokenGeneration` | HMAC-SHA 署名キー、本番環境では必ず変更 |
| `jwt-expiration` | long | `86400000`（24h） | トークン有効期限（ミリ秒） |
| `token-header` | String | `Authorization` | トークンを格納する HTTP ヘッダー名 |
| `token-prefix` | String | `Bearer ` | トークン値のプレフィックス、抽出時に自動除去 |
| `token-cookie-name` | String | `sw_token` | トークンを格納する Cookie 名 |
| `csrf-enabled` | boolean | `false` | CSRF 保護を有効にするか |
| `permit-all-urls` | List\<String\> | `[]` | 認証不要 URL リスト、Ant スタイルパターンをサポート |



---

## 認証フロー

### 1. トークン抽出

`JwtUtil.extractJwtFromRequest()` は以下の優先順位でトークンを抽出します：

1. **ヘッダー**：`Authorization` ヘッダーから抽出し、`Bearer ` プレフィックスを除去
2. **Cookie**：設定された Cookie から抽出

```java
// ヘッダー方式（REST API）
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

// Cookie 方式（Admin コンソール）
Cookie: sw_token=eyJhbGciOiJIUzI1NiJ9...
```

### 2. トークン検証

`JwtAuthenticationFilter` は `OncePerRequestFilter.doFilterInternal()` で以下を実行します：

1. **トークン抽出**：ヘッダーまたは Cookie から JWT を取得
2. **署名検証**：HMAC-SHA を使用してトークンの整合性を検証
3. **有効期限チェック**：`exp` クレームが期限切れかどうかを確認
4. **ユーザー読み込み**：トークンから `username` を抽出し、`UserDetailsService.loadUserByUsername()` を呼び出し
5. **コンテキスト設定**：
   - `SecurityContextHolder`：`UsernamePasswordAuthenticationToken` を設定
   - `AuthenticationContextHolder`：`userId`、`username`、`tenantId` を設定

### 3. トークン欠落または無効

**フィルターはリクエストをブロックしません**。欠落または無効なトークンはログに記録されるだけで、フィルターチェーンは続行されます。Spring Security のデフォルト `AuthenticationEntryPoint` が 401 Unauthorized を返します。

### 4. コンテキストクリーンアップ

リクエスト完了後、`finally` ブロックで `AuthenticationContextHolder` と `SecurityContextHolder` をクリーンアップし、ThreadLocal リークを防止します。

---

## トークン管理

### JWT クレーム構造

| クレーム | タイプ | 説明 |
|---------|--------|------|
| `sub` | String | ユーザー名 |
| `userId` | Integer | ユーザー ID |
| `username` | String | ユーザー名 |
| `tenantId` | Object | テナント ID（オプション） |
| `iat` | Date | 発行時刻 |
| `exp` | Date | 有効期限 |

### トークン生成

```java
@Autowired
private JwtUtil jwtUtil;

String token = jwtUtil.generateToken("admin", 1, null);
```

### トークン解析

```java
String username = jwtUtil.getUsernameFromToken(token);
Integer userId = jwtUtil.getUserIdFromToken(token);
Object tenantId = jwtUtil.getTenantIdFromToken(token);
```

### トークン検証

```java
boolean valid = jwtUtil.validateToken(token);
```

---

## SPI 拡張

下流モジュールは `SecurityConfigProvider` インターフェースを実装することで、フレームワークコードを変更せずにセキュリティ設定を拡張できます。

### インターフェース定義

```java
public interface SecurityConfigProvider {

    default List<String> getPermitAllUrls() {
        return List.of();
    }

    default void configure(HttpSecurity http) throws Exception {
    }

    default int getOrder() {
        return 0;
    }
}
```

### 使用例

```java
@Component
public class MySecurityConfig implements SecurityConfigProvider {

    @Override
    public List<String> getPermitAllUrls() {
        return List.of("/api/public/**", "/actuator/health");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
```

すべての `SecurityConfigProvider` 実装は自動検出され、`getOrder()` の昇順で実行されます。

---

## Feign 伝播

`SecurityFeignInterceptor` は、Feign 呼び出し時に現在のリクエストコンテキストから JWT を自動抽出し、リクエストヘッダーに追加することで、サービス間認証伝播を実現します。

**有効化条件：**

- クラスパスに `feign.RequestInterceptor` が存在する
- 現在のリクエストコンテキストに `ServletRequestAttributes` が存在する

**動作：**

- 現在のリクエストから JWT を抽出
- `{token-prefix}{token}` 形式でリクエストヘッダーに設定
- JWT またはリクエストコンテキストがない場合はスキップ

---

## 使用例

### 1. UserDetailsService の実装

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().toArray(new String[0]))
                .build();
    }
}
```

### 2. ログイン API

```java
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResult<String> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Integer userId = getUserId(userDetails.getUsername());
        String token = jwtUtil.generateToken(userDetails.getUsername(), userId, null);

        return ApiResult.success(token);
    }
}
```

### 3. 現在のユーザー取得

```java
@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<UserInfo> getCurrentUser() {
        String username = AuthUtil.getCurrentUsername();
        return ApiResult.success(userService.getByUsername(username));
    }
}
```

### 4. フロントエンドでのトークン保存

```javascript
// REST API：localStorage に保存
fetch('/api/login', {
    method: 'POST',
    body: JSON.stringify({ username: 'admin', password: '123456' })
})
.then(res => res.json())
.then(data => {
    localStorage.setItem('token', data.data);
});

// 後続のリクエストでトークンを送信
fetch('/api/users', {
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
});
```

---

## ベストプラクティス

### 1. 本番環境でキーを変更

```yaml
spring:
  whale:
    web-mvc:
      security:
        jwt-secret: ${JWT_SECRET}  # 環境変数で注入、ハードコードしない
```

### 2. トークン有効期限

- 短期トークン（15〜30分）+ Refresh Token メカニズムでセキュリティを向上
- 長期トークン（24時間）は内部管理システムに適している

### 3. 認証不要 URL

- ログイン API、公開 API、ヘルスチェックエンドポイントは `permit-all-urls` に追加
- `SecurityConfigProvider` SPI 経由でも宣言可能

### 4. ユーザーパスワード

- BCrypt エンコーディングが自動的に適用されるため、手動処理は不要
- データベースに保存するパスワードは BCrypt ハッシュ値であること

### 5. 静的リソース

`/admin/css/**` と `/admin/js/**` は JWT 欠落の警告ログに妨げられませんが、`/admin/**` 下のその他の静的リソースも認証不要リストに追加することを推奨します。