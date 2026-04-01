# Spring Whale 全局异常处理

Spring Whale 框架提供了强大的全局异常处理功能，基于 Spring Boot 的 `@RestControllerAdvice` 实现，支持多种常见异常的自动处理和国际化。

## 目录

- [功能特性](#功能特性)
- [支持的异常类型](#支持的异常类型)
- [配置说明](#配置说明)
- [使用示例](#使用示例)
- [最佳实践](#最佳实践)

## 功能特性

### 核心特性

- ✅ **统一响应格式** - 所有异常都返回统一的 `ApiResult` 格式
- ✅ **多异常类型支持** - 支持业务异常、参数验证异常、HTTP 方法不支持等多种异常
- ✅ **国际化支持** - 错误消息支持多语言国际化
- ✅ **分级日志记录** - 根据异常类型自动记录不同级别的日志
- ✅ **错误码管理** - 支持模块化的错误码命名
- ✅ **扩展数据支持** - 业务异常可以携带扩展数据
- ✅ **自动注册** - 通过 Spring Boot 自动配置，无需手动注册

### HTTP 状态码映射

| 异常类型 | HTTP 状态码 | 说明 |
|---------|-----------|------|
| `Exception` | 500 | 服务器内部错误 |
| `BusinessException` | 动态 | 业务异常，错误码由业务决定 |
| `IllegalArgumentException` | 400 | 非法参数异常 |
| `ValidationException` | 400 | JSR-303 验证异常 |
| `MethodArgumentNotValidException` | 400 | @Validated 参数验证失败 |
| `BindException` | 400 | 参数绑定失败 |
| `HttpRequestMethodNotSupportedException` | 405 | HTTP 方法不支持 |
| `DuplicateKeyException` | 409 | 重复键（唯一约束冲突） |

## 支持的异常类型

### 1. 通用异常

捕获所有未被其他处理器处理的异常：

```java
@ExceptionHandler(value = Exception.class)
public ApiResult<Boolean> handleException(Exception e)
```

**响应示例：**
```json
{
  "code": "500",
  "message": "服务器异常，请稍后重试！",
  "data": false
}
```

**日志级别：** ERROR

### 2. 业务异常

处理自定义的业务异常：

```java
@ExceptionHandler(value = BusinessException.class)
public ApiResult<?> handleBusinessException(BusinessException e)
```

**响应示例：**
```json
{
  "code": "USER_MODULE_USER_NOT_FOUND",
  "message": "用户不存在",
  "data": {
    "userId": 123,
    "attemptedEmail": "test@example.com"
  }
}
```

**特点：**
- 支持模块化错误码（如 `user-module_USER_NOT_FOUND`）
- 支持携带扩展数据
- 支持国际化消息
- 日志级别为 WARN，避免过多警告信息

### 3. 参数验证异常

处理所有参数验证相关的异常：

```java
@ExceptionHandler(value = {
    IllegalArgumentException.class,
    ValidationException.class,
    MethodArgumentNotValidException.class,
    BindException.class
})
public ApiResult<Boolean> handleIllegalArgumentException(Throwable e)
```

**响应示例：**
```json
{
  "code": "400",
  "message": "无效请求参数！",
  "data": false
}
```

**日志级别：** WARN

### 4. HTTP 方法不支持

处理 HTTP 方法不支持的情况：

```java
@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
public ApiResult<Boolean> handleHttpRequestMethodNotSupportedException(
    Throwable e
)
```

**响应示例：**
```json
{
  "code": "405",
  "message": "方法不允许！",
  "data": false
}
```

**日志级别：** WARN

### 5. 重复键异常

处理数据库唯一约束冲突等情况：

```java
@ExceptionHandler(value = DuplicateKeyException.class)
public ApiResult<Boolean> handleDuplicateKeyException(DuplicateKeyException e)
```

**响应示例：**
```json
{
  "code": "409",
  "message": "重复记录！",
  "data": false
}
```

**日志级别：** WARN

## 配置说明

### 配置文件

在 `application.yml` 中添加以下配置：

```yaml
spring:
  whale:
    web-mvc:
      exception:
        # 是否启用国际化（默认：false）
        enable-i18n: true
        # 500 错误消息
        message-500: 服务器异常，请稍后重试！
        code-500: http.error.500
        # 400 错误消息
        message-400: 无效请求参数！
        code-400: http.error.400
        # 405 错误消息
        message-405: 方法不允许！
        code-405: http.error.405
        # 409 错误消息
        message-409: 重复记录！
        code-409: http.error.409
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `enable-i18n` | boolean | false | 是否启用国际化消息 |
| `message-500` | String | 服务器异常，请稍后重试！ | 500 错误的默认消息 |
| `code-500` | String | http.error.500 | 500 错误的国际化 key |
| `message-400` | String | 无效请求参数！ | 400 错误的默认消息 |
| `code-400` | String | http.error.400 | 400 错误的国际化 key |
| `message-405` | String | 方法不允许！ | 405 错误的默认消息 |
| `code-405` | String | http.error.405 | 405 错误的国际化 key |
| `message-409` | String | 重复记录！ | 409 错误的默认消息 |
| `code-409` | String | http.error.409 | 409 错误的国际化 key |

### 国际化配置

#### 1. 创建国际化资源文件

`resources/messages.properties` (默认):

```properties
http.error.500=Server abnormal, please try again later!
http.error.400=Invalid request parameters!
http.error.405=Method not allowed!
http.error.409=Duplicate records!
USER_NOT_FOUND=User not found
```

`resources/messages_zh_CN.properties` (中文):

```properties
http.error.500=服务器异常，请稍后重试！
http.error.400=无效请求参数！
http.error.405=方法不允许！
http.error.409=重复记录！
USER_NOT_FOUND=用户不存在
ORDER_NOT_FOUND=订单不存在
```

`resources/messages_ja_JP.properties` (日文):

```properties
http.error.500=サーバーエラーが発生しました
http.error.400=無効なリクエストパラメータ！
http.error.405=メソッドが許可されていません！
http.error.409=重複レコード！
USER_NOT_FOUND=ユーザーが存在しません
```

#### 2. 动态切换语言

```java
// 在请求中设置语言环境
LocaleContextHolder.setLocale(Locale.JAPAN);

// 或者通过拦截器根据请求头设置
@GetMapping(value = "/users/{id}", headers = "Accept-Language=ja-JP")
public ResponseEntity<UserDTO> getUserJapanese(@PathVariable Long id) {
    LocaleContextHolder.setLocale(Locale.JAPAN);
    return ResponseEntity.ok(userService.getUser(id));
}
```

## 使用示例

### 1. 抛出业务异常

```java
import io.github.springwhale.framework.core.exception.BusinessException;

@Service
public class UserService {
    
    public User getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw BusinessException.create(
                "USER_NOT_FOUND",
                "用户不存在"
            );
        }
        return user;
    }
    
    public void createUser(User user) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(user.getEmail())) {
            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            throw BusinessException.create(
                "EMAIL_EXISTS",
                "邮箱已存在",
                data
            );
        }
        userRepository.save(user);
    }
    
    public void updateUser(User user) {
        // 带模块名的业务异常
        throw BusinessException.createWithModule(
            "USER_NOT_FOUND",
            "user-module",
            "用户不存在"
        );
    }
}
```

### 2. Controller 中使用

```java
import io.github.springwhale.framework.core.model.ApiResult;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // 返回值会被自动包装成 ApiResult<User>
        return userService.getUserById(id);
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        // 返回值会被自动包装成 ApiResult<User>
        userService.createUser(user);
        return user;
    }
    
    @PutMapping("/{id}")
    public void updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        // void 类型会被自动包装成 ApiResult.success()
        userService.updateUser(user);
    }
    
    @GetMapping("/list")
    public List<User> listUsers() {
        // 集合类型也会被自动包装
        return userService.findAll();
    }
}
```

**自动转换说明：**
- `User` → `ApiResult<User>`
- `List<User>` → `ApiResult<List<User>>`
- `void` → `ApiResult<Boolean>` (值为 true)
- `ApiResult<T>` → 保持原样（不会被重复包装）

### 3. 参数验证异常

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
        // 如果参数验证失败，会自动返回 400 错误
        // 返回值会被自动包装成 ApiResult<User>
        return userService.createUser(request.toEntity());
    }
    
    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam @NotBlank String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page) {
        // 如果参数为空或格式不正确，会自动返回 400 错误
        return userService.searchUsers(keyword, page);
    }
}

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "密码不能为空")
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

**响应示例（成功）：**
```json
{
  "code": "200",
  "message": "success",
  "data": {
    "id": 1,
    "username": "张三",
    "email": "zhangsan@example.com"
  }
}
```

**响应示例（验证失败）：**
```json
{
  "code": "400",
  "message": "无效请求参数！",
  "data": false
}
```

### 4. 带国际化的业务异常

```java
@Service
public class OrderService {
    
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            // 使用 i18n key，实际消息从国际化资源文件获取
            throw BusinessException.createWithI18n(
                "ORDER_NOT_FOUND",
                "error.order.notfound",
                "订单不存在"
            );
        }
        return order;
    }
    
    public void createOrder(Order order) {
        // 带模块和数据的 i18n 异常
        Map<String, Object> data = new HashMap<>();
        data.put("productId", order.getProductId());
        throw BusinessException.createI18nWithModule(
            "PRODUCT_OUT_OF_STOCK",
            "error.product.outofstock",
            "order-module",
            "商品库存不足",
            data
        );
    }
}
```

### 5. 前端调用示例

```javascript
// 正常请求 - 成功响应
fetch('/api/users/1')
  .then(response => response.json())
  .then(data => {
    // data 结构：{ code: '200', message: 'success', data: {...} }
    if (data.code === '200') {
      console.log('用户信息:', data.data);
    } else {
      console.error('业务错误:', data.message);
    }
  });

// 捕获业务异常
fetch('/api/users/999')
  .then(response => response.json())
  .then(data => {
    // data 结构：{ code: 'USER_NOT_FOUND', message: '用户不存在', data: null/false }
    if (data.code !== '200') {
      alert(`错误 [${data.code}]: ${data.message}`);
    }
  });

// 参数验证失败
fetch('/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ invalid: 'data' })
})
.then(response => response.json())
.then(data => {
  // data 结构：{ code: '400', message: '无效请求参数！', data: false }
  if (data.code === '400') {
    alert('参数验证失败：' + data.message);
  }
});

// 创建成功 - 返回包装后的数据
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
  // data 结构：{ code: '200', message: 'success', data: { id: 1, username: 'new_user', ... } }
  if (data.code === '200') {
    console.log('创建成功，用户 ID:', data.data.id);
  }
});

// 捕获重复记录异常
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
  // data 结构：{ code: '409', message: '重复记录！', data: false }
  if (data.code === '409') {
    alert(data.message);
  }
});
```

**响应格式统一说明：**

所有 Controller 的返回值都会被自动包装为 `ApiResult` 格式：

| 场景 | HTTP 状态码 | 响应体格式 |
|------|-----------|----------|
| 成功 | 200 | `{ code: '200', message: 'success', data: {...} }` |
| 业务异常 | 200 | `{ code: 'ERROR_CODE', message: '错误消息', data: null/false }` |
| 参数验证失败 | 200 | `{ code: '400', message: '无效请求参数！', data: false }` |
| 服务器错误 | 200 | `{ code: '500', message: '服务器异常', data: false }` |

**注意：** 所有异常都会返回 HTTP 200，通过 `code` 字段区分错误类型。

## 最佳实践

### 1. 业务异常使用规范

**推荐做法：**
```java
// ✅ 使用有意义的错误码
throw BusinessException.create("USER_NOT_FOUND", "用户不存在");

// ✅ 使用模块化命名
throw BusinessException.createWithModule(
    "NOT_FOUND", 
    "user-module", 
    "用户不存在"
);

// ✅ 携带扩展数据
Map<String, Object> data = Map.of("userId", userId);
throw BusinessException.create(
    "USER_NOT_FOUND", 
    "用户不存在", 
    data
);

// ✅ 使用国际化
throw BusinessException.createWithI18n(
    "USER_NOT_FOUND",
    "error.user.notfound"
);
```

**不推荐做法：**
```java
// ❌ 错误码没有意义
throw BusinessException.create("ERROR_1", "出错了");

// ❌ 硬编码错误消息
throw BusinessException.create("NO_PERMISSION", "你没有权限访问这个资源");

// ❌ 暴露敏感信息
throw BusinessException.create("DB_ERROR", e.getMessage());
```

### 2. 日志级别选择

异常处理器已经内置了合理的日志级别：

- **ERROR**: 未知异常（需要立即关注）
- **WARN**: 业务异常、参数验证异常（正常业务流程）
- **DEBUG**: 堆栈跟踪（调试时使用）

不需要在业务代码中重复记录日志。

### 3. 国际化最佳实践

**生产环境配置：**
```yaml
spring:
  whale:
    web-mvc:
      exception:
        enable-i18n: true
```

**开发环境配置：**
```yaml
spring:
  whale:
    web-mvc:
      exception:
        enable-i18n: false
```

**国际化资源文件组织：**
```
resources/
├── messages.properties          # 默认（英文）
├── messages_zh_CN.properties   # 简体中文
├── messages_zh_TW.properties   # 繁体中文
├── messages_ja_JP.properties   # 日文
└── messages_ko_KR.properties   # 韩文
```

### 4. 错误码设计规范

**推荐的错误码命名规范：**

```
[MODULE_]RESOURCE_ACTION
```

**示例：**
- `USER_NOT_FOUND` - 用户不存在
- `ORDER_CREATE_FAILED` - 创建订单失败
- `PAYMENT_INSUFFICIENT_BALANCE` - 支付余额不足
- `user-module_USER_LOCKED` - 用户模块 - 用户已锁定

### 5. 测试建议

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
        // 设置无效数据
        
        ApiResult<User> result = userController.createUser(request);
        
        assertEquals("400", result.getCode());
    }
}
```

## 注意事项

### 1. 性能考虑

- 异常处理会有性能开销，应该用于真正的异常情况
- 正常的业务逻辑不应该依赖异常来处理流程
- 对于可预期的业务校验，应该在 Service 层返回结果而不是抛异常

### 2. 安全性

**不要暴露敏感信息：**
```java
// ❌ 不安全 - 暴露数据库错误详情
throw new RuntimeException(e.getMessage());

// ✅ 安全 - 返回通用错误消息
throw BusinessException.create("SYSTEM_ERROR", "系统繁忙，请稍后重试");
```

### 3. 事务处理

异常会自动触发事务回滚（RuntimeException）：

```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    // 如果余额不足抛出 BusinessException
    // 会自动回滚整个事务
    accountService.deduct(fromId, amount);
    accountService.add(toId, amount);
}
```

### 4. 异步处理

异步方法中的异常可能无法被全局异常处理器捕获：

```java
// ❌ 异步方法的异常不会被全局异常处理器捕获
@Async
public void asyncProcess() {
    throw BusinessException.create("ERROR", "异步错误");
}

// ✅ 应该在异步方法内部捕获并处理异常
@Async
public CompletableFuture<Void> asyncProcess() {
    try {
        // 处理逻辑
    } catch (Exception e) {
        log.error("异步处理失败", e);
        throw e;
    }
}
```

### 5. 与其他异常处理器的优先级

如果项目中定义了多个 `@RestControllerAdvice`，可以通过 `@Order` 注解控制优先级：

```java
@Component
@Order(1) // 数字越小优先级越高
@RestControllerAdvice
public class CustomExceptionHandler {
    // 自定义异常处理器
}
```

## 相关资源

- [Spring Boot 异常处理官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-web-applications.spring-hateoas)
- [Spring MVC 异常处理](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-exceptionhandlers)
- [Bean Validation 规范](https://beanvalidation.org/)
