# Spring Whale JWT 认证

Spring Whale 基于 Spring Security 和 JWT 提供开箱即用的无状态认证方案，支持 Header/Cookie 双通道提取 Token、SPI 扩展机制以及 Feign 服务间 Token 透传。

---

## 目录

- [架构概览](#架构概览)
- [配置说明](#配置说明)
- [认证流程](#认证流程)
- [Token 管理](#token-管理)
- [SPI 扩展](#spi-扩展)
- [Feign 透传](#feign-透传)
- [使用示例](#使用示例)
- [最佳实践](#最佳实践)

---

## 架构概览

```
请求 → JwtAuthenticationFilter → SecurityContextHolder（Spring Security）
                              → AuthenticationContextHolder（业务上下文）
                              → SecurityFilterChain（权限校验）
```

**核心组件：**

| 组件 | 职责 |
|------|------|
| `SecurityAutoConfiguration` | 组装 SecurityFilterChain，配置无状态会话、BCrypt 密码编码、CORS |
| `JwtAuthenticationFilter` | OncePerRequestFilter，从请求中提取 JWT 并校验，设置双认证上下文 |
| `JwtUtil` | HMAC-SHA 签名，生成/解析/校验 Token |
| `SecurityProperties` | 所有安全配置项，前缀 `spring.whale.web-mvc.security` |
| `SecurityConfigProvider` | SPI 接口，下游模块可声明免认证 URL 和自定义 HttpSecurity 配置 |
| `SecurityFeignInterceptor` | Feign 调用时自动从当前请求提取 JWT 并透传 |

**双认证上下文：**

- **Spring Security Context**：`SecurityContextHolder`，用于 `@PreAuthorize`、角色校验等标准 Spring Security 能力
- **业务上下文**：`AuthenticationContextHolder`（ThreadLocal），提供 `userId`、`username`、`tenantId`，通过 `AuthUtil` 便捷获取

---

## 配置说明

### 配置文件

```yaml
spring:
  whale:
    web-mvc:
      security:
        # JWT 签名密钥（默认：SpringWhaleSecretKey2024ForJWTTokenGeneration）
        jwt-secret: ${JWT_SECRET:your-secret-key-at-least-256-bits}
        # Token 过期时间，单位毫秒（默认：86400000，即 24 小时）
        jwt-expiration: 86400000
        # Authorization 请求头名称（默认：Authorization）
        token-header: Authorization
        # Token 前缀（默认：Bearer ）
        token-prefix: "Bearer "
        # Cookie 中 Token 的名称（默认：sw_token）
        token-cookie-name: sw_token
        # 是否启用 CSRF（默认：false）
        csrf-enabled: false
        # 免认证 URL 列表
        permit-all-urls:
          - /public/**
          - /api/login
        
        
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `jwt-secret` | String | `SpringWhaleSecretKey2024ForJWTTokenGeneration` | HMAC-SHA 签名密钥，生产环境务必修改 |
| `jwt-expiration` | long | `86400000`（24h） | Token 过期时间（毫秒） |
| `token-header` | String | `Authorization` | 存放 Token 的 HTTP 请求头名称 |
| `token-prefix` | String | `Bearer ` | Token 值前缀，提取时会自动去除 |
| `token-cookie-name` | String | `sw_token` | 存放 Token 的 Cookie 名称 |
| `csrf-enabled` | boolean | `false` | 是否启用 CSRF 防护 |
| `permit-all-urls` | List\<String\> | `[]` | 免认证 URL 列表，支持 Ant 风格通配符 |



---

## 认证流程

### 1. Token 提取

`JwtUtil.extractJwtFromRequest()` 按以下优先级提取 Token：

1. **请求头**：从 `Authorization` 头中提取，去除 `Bearer ` 前缀
2. **Cookie**：从指定名称的 Cookie 中提取

```java
// 请求头方式（REST API）
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

// Cookie 方式（Admin 控制台）
Cookie: sw_token=eyJhbGciOiJIUzI1NiJ9...
```

### 2. Token 校验

`JwtAuthenticationFilter` 在 `OncePerRequestFilter.doFilterInternal()` 中执行：

1. **提取 Token**：从请求头或 Cookie 中获取 JWT
2. **校验签名**：使用 HMAC-SHA 验证 Token 完整性
3. **校验过期**：检查 `exp` 声明是否已过期
4. **加载用户**：从 Token 中提取 `username`，通过 `UserDetailsService.loadUserByUsername()` 加载用户详情
5. **设置上下文**：
   - `SecurityContextHolder`：设置 `UsernamePasswordAuthenticationToken`
   - `AuthenticationContextHolder`：设置 `userId`、`username`、`tenantId`

### 3. Token 缺失或无效

**Filter 不会阻断请求**，缺失或无效的 Token 只记录日志，继续执行 Filter Chain。最终由 Spring Security 默认的 `AuthenticationEntryPoint` 返回 401 Unauthorized。

### 4. 上下文清理

请求结束后，`finally` 块中清理 `AuthenticationContextHolder` 和 `SecurityContextHolder`，防止 ThreadLocal 泄漏。

---

## Token 管理

### JWT Claims 结构

| Claim | 类型 | 说明 |
|-------|------|------|
| `sub` | String | 用户名 |
| `userId` | Integer | 用户 ID |
| `username` | String | 用户名 |
| `tenantId` | Object | 租户 ID（可选） |
| `iat` | Date | 签发时间 |
| `exp` | Date | 过期时间 |

### 生成 Token

```java
@Autowired
private JwtUtil jwtUtil;

String token = jwtUtil.generateToken("admin", 1, null);
```

### 解析 Token

```java
String username = jwtUtil.getUsernameFromToken(token);
Integer userId = jwtUtil.getUserIdFromToken(token);
Object tenantId = jwtUtil.getTenantIdFromToken(token);
```

### 校验 Token

```java
boolean valid = jwtUtil.validateToken(token);
```

---

## SPI 扩展

下游模块实现 `SecurityConfigProvider` 接口即可扩展安全配置，无需修改框架代码。

### 接口定义

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

### 使用示例

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

所有 `SecurityConfigProvider` 实现会被自动发现并按 `getOrder()` 升序执行。

---

## Feign 透传

`SecurityFeignInterceptor` 在 Feign 调用时自动从当前请求上下文提取 JWT 并添加到请求头，实现服务间认证透传。

**启用条件：**

- Classpath 中存在 `feign.RequestInterceptor`
- 当前请求上下文中有 `ServletRequestAttributes`

**行为：**

- 从当前请求中提取 JWT
- 以 `{token-prefix}{token}` 格式设置到请求头
- 无 JWT 或无请求上下文时跳过，不报错

---

## 使用示例

### 1. 实现 UserDetailsService

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().toArray(new String[0]))
                .build();
    }
}
```

### 2. 登录接口

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

### 3. 获取当前用户

```java
@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResult<UserInfo> getCurrentUser() {
        // 通过 AuthUtil 获取业务上下文
        String username = AuthUtil.getCurrentUsername();
        return ApiResult.success(userService.getByUsername(username));
    }
}
```

### 4. 前端存储 Token

```javascript
// REST API：存储在 localStorage
fetch('/api/login', {
    method: 'POST',
    body: JSON.stringify({ username: 'admin', password: '123456' })
})
.then(res => res.json())
.then(data => {
    localStorage.setItem('token', data.data);
});

// 后续请求携带 Token
fetch('/api/users', {
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
});
```

---

## 最佳实践

### 1. 生产环境修改密钥

```yaml
spring:
  whale:
    web-mvc:
      security:
        jwt-secret: ${JWT_SECRET}  # 通过环境变量注入，不要硬编码
```

### 2. Token 过期时间

- 短期 Token（15-30 分钟）+ Refresh Token 机制，安全性更高
- 长期 Token（24 小时）适合内部管理系统

### 3. 免认证 URL

- 登录接口、公开 API、健康检查端点应加入 `permit-all-urls`
- 也可通过 `SecurityConfigProvider` SPI 声明

### 4. 用户密码

- 自动使用 BCrypt 编码，无需手动处理
- 数据库中存储的密码应为 BCrypt 哈希值

### 5. 静态资源

`/admin/css/**` 和 `/admin/js/**` 不会被 JWT 缺失的警告日志干扰，但其他 `/admin/**` 下的静态资源建议也加入免认证列表。