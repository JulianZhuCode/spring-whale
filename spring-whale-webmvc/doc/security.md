# Spring Whale JWT Authentication

Spring Whale provides out-of-the-box stateless authentication based on Spring Security and JWT, supporting dual-channel token extraction (Header/Cookie), SPI extension mechanism, and Feign service-to-service token propagation.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Configuration](#configuration)
- [Authentication Flow](#authentication-flow)
- [Token Management](#token-management)
- [SPI Extension](#spi-extension)
- [Feign Propagation](#feign-propagation)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)

---

## Architecture Overview

```
Request → JwtAuthenticationFilter → SecurityContextHolder (Spring Security)
                                  → AuthenticationContextHolder (Business Context)
                                  → SecurityFilterChain (Authorization)
```

**Core Components:**

| Component | Role |
|-----------|------|
| `SecurityAutoConfiguration` | Assembles SecurityFilterChain, configures stateless sessions, BCrypt encoding, CORS |
| `JwtAuthenticationFilter` | OncePerRequestFilter, extracts and validates JWT, sets dual authentication contexts |
| `JwtUtil` | HMAC-SHA signing, token generation/parsing/validation |
| `SecurityProperties` | All security settings, prefix `spring.whale.web-mvc.security` |
| `SecurityConfigProvider` | SPI interface for downstream modules to declare permit-all URLs and custom HttpSecurity config |
| `SecurityFeignInterceptor` | Automatically extracts and propagates JWT from current request during Feign calls |

**Dual Authentication Context:**

- **Spring Security Context**: `SecurityContextHolder`, used for `@PreAuthorize`, role checks, and other standard Spring Security capabilities
- **Business Context**: `AuthenticationContextHolder` (ThreadLocal), provides `userId`, `username`, `tenantId` via `AuthUtil`

---

## Configuration

### Configuration File

```yaml
spring:
  whale:
    web-mvc:
      security:
        # JWT signing secret (default: SpringWhaleSecretKey2024ForJWTTokenGeneration)
        jwt-secret: ${JWT_SECRET:your-secret-key-at-least-256-bits}
        # Token expiration in milliseconds (default: 86400000, i.e., 24 hours)
        jwt-expiration: 86400000
        # Authorization header name (default: Authorization)
        token-header: Authorization
        # Token prefix (default: Bearer )
        token-prefix: "Bearer "
        # Cookie name for token (default: sw_token)
        token-cookie-name: sw_token
        # Enable CSRF (default: false)
        csrf-enabled: false
        # URLs that bypass authentication
        permit-all-urls:
          - /public/**
          - /api/login
        
        
```

### Configuration Items

| Item | Type | Default | Description |
|------|------|---------|-------------|
| `jwt-secret` | String | `SpringWhaleSecretKey2024ForJWTTokenGeneration` | HMAC-SHA signing key, must change in production |
| `jwt-expiration` | long | `86400000` (24h) | Token expiration time (ms) |
| `token-header` | String | `Authorization` | HTTP header name for token |
| `token-prefix` | String | `Bearer ` | Token prefix, stripped during extraction |
| `token-cookie-name` | String | `sw_token` | Cookie name for token |
| `csrf-enabled` | boolean | `false` | Whether to enable CSRF protection |
| `permit-all-urls` | List\<String\> | `[]` | URLs bypassing authentication, supports Ant-style patterns |



---

## Authentication Flow

### 1. Token Extraction

`JwtUtil.extractJwtFromRequest()` extracts tokens in the following priority:

1. **Header**: Extract from `Authorization` header, removing `Bearer ` prefix
2. **Cookie**: Extract from the configured cookie

```java
// Header approach (REST API)
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

// Cookie approach (Admin Console)
Cookie: sw_token=eyJhbGciOiJIUzI1NiJ9...
```

### 2. Token Validation

`JwtAuthenticationFilter` executes in `OncePerRequestFilter.doFilterInternal()`:

1. **Extract Token**: Get JWT from header or cookie
2. **Verify Signature**: Use HMAC-SHA to verify token integrity
3. **Check Expiration**: Check if `exp` claim has expired
4. **Load User**: Extract `username` from token, call `UserDetailsService.loadUserByUsername()`
5. **Set Contexts**:
   - `SecurityContextHolder`: Set `UsernamePasswordAuthenticationToken`
   - `AuthenticationContextHolder`: Set `userId`, `username`, `tenantId`

### 3. Missing or Invalid Token

**The filter does not block the request**. Missing or invalid tokens are only logged, and the filter chain continues. Spring Security's default `AuthenticationEntryPoint` returns 401 Unauthorized.

### 4. Context Cleanup

After the request completes, `finally` blocks clean up `AuthenticationContextHolder` and `SecurityContextHolder` to prevent ThreadLocal leaks.

---

## Token Management

### JWT Claims Structure

| Claim | Type | Description |
|-------|------|-------------|
| `sub` | String | Username |
| `userId` | Integer | User ID |
| `username` | String | Username |
| `tenantId` | Object | Tenant ID (optional) |
| `iat` | Date | Issued at |
| `exp` | Date | Expiration time |

### Generate Token

```java
@Autowired
private JwtUtil jwtUtil;

String token = jwtUtil.generateToken("admin", 1, null);
```

### Parse Token

```java
String username = jwtUtil.getUsernameFromToken(token);
Integer userId = jwtUtil.getUserIdFromToken(token);
Object tenantId = jwtUtil.getTenantIdFromToken(token);
```

### Validate Token

```java
boolean valid = jwtUtil.validateToken(token);
```

---

## SPI Extension

Downstream modules can implement `SecurityConfigProvider` to extend security configuration without modifying framework code.

### Interface Definition

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

### Usage Example

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

All `SecurityConfigProvider` implementations are auto-detected and executed in ascending `getOrder()` order.

---

## Feign Propagation

`SecurityFeignInterceptor` automatically extracts JWT from the current request context and adds it to the request header during Feign calls, enabling service-to-service authentication propagation.

**Activation Conditions:**

- `feign.RequestInterceptor` is on the classpath
- `ServletRequestAttributes` exist in the current request context

**Behavior:**

- Extracts JWT from the current request
- Sets it in `{token-prefix}{token}` format to the request header
- Skips silently when no JWT or request context is available

---

## Usage Examples

### 1. Implement UserDetailsService

```java
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().toArray(new String[0]))
                .build();
    }
}
```

### 2. Login API

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

### 3. Get Current User

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

### 4. Frontend Token Storage

```javascript
// REST API: store in localStorage
fetch('/api/login', {
    method: 'POST',
    body: JSON.stringify({ username: 'admin', password: '123456' })
})
.then(res => res.json())
.then(data => {
    localStorage.setItem('token', data.data);
});

// Subsequent requests carry the token
fetch('/api/users', {
    headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
});
```

---

## Best Practices

### 1. Change Secret in Production

```yaml
spring:
  whale:
    web-mvc:
      security:
        jwt-secret: ${JWT_SECRET}  # Inject via environment variable, never hardcode
```

### 2. Token Expiration

- Short-lived tokens (15-30 min) + Refresh Token mechanism for higher security
- Long-lived tokens (24 hours) suitable for internal management systems

### 3. Permit-All URLs

- Login API, public APIs, and health check endpoints should be added to `permit-all-urls`
- Can also be declared via `SecurityConfigProvider` SPI

### 4. User Passwords

- BCrypt encoding is applied automatically, no manual handling needed
- Passwords stored in the database should be BCrypt hashes

### 5. Static Resources

`/admin/css/**` and `/admin/js/**` are not disturbed by missing JWT warning logs, but other static resources under `/admin/**` should also be added to the permit-all list.