# Spring Whale Global Exception Handling

Spring Whale framework provides powerful global exception handling capabilities based on Spring Boot's `@RestControllerAdvice`, supporting automatic handling and internationalization of various common exceptions.

## Table of Contents

- [Features](#features)
- [Supported Exception Types](#supported-exception-types)
- [Configuration](#configuration)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)

## Features

### Core Features

- ✅ **Unified Response Format** - All exceptions return a unified `ApiResult` format
- ✅ **Multiple Exception Types** - Supports business exceptions, parameter validation exceptions, HTTP method not supported, etc.
- ✅ **Internationalization Support** - Error messages support multi-language i18n
- ✅ **Hierarchical Logging** - Automatically logs different levels based on exception type
- ✅ **Error Code Management** - Supports modular error code naming
- ✅ **Extended Data Support** - Business exceptions can carry extended data
- ✅ **Auto Registration** - Automatically configured via Spring Boot, no manual registration needed

### HTTP Status Code Mapping

| Exception Type | HTTP Status | Description |
|----------------|-------------|-------------|
| `Exception` | 500 | Server internal error |
| `BusinessException` | Dynamic | Business exception, error code determined by business |
| `IllegalArgumentException` | 400 | Illegal argument exception |
| `ValidationException` | 400 | JSR-303 validation exception |
| `MethodArgumentNotValidException` | 400 | @Validated parameter validation failed |
| `BindException` | 400 | Parameter binding failed |
| `HttpRequestMethodNotSupportedException` | 405 | HTTP method not supported |
| `DuplicateKeyException` | 409 | Duplicate key (unique constraint conflict) |

## Supported Exception Types

### 1. Generic Exception

Catches all exceptions not handled by other handlers:

```java
@ExceptionHandler(value = Exception.class)
public ApiResult<Boolean> handleException(Exception e)
```

**Response Example:**
```json
{
  "code": "500",
  "message": "Server abnormal, please try again later!",
  "data": false
}
```

**Log Level:** ERROR

### 2. Business Exception

Handles custom business exceptions:

```java
@ExceptionHandler(value = BusinessException.class)
public ApiResult<?> handleBusinessException(BusinessException e)
```

**Response Example:**
```json
{
  "code": "USER_MODULE_USER_NOT_FOUND",
  "message": "User not found",
  "data": {
    "userId": 123,
    "attemptedEmail": "test@example.com"
  }
}
```

**Features:**
- Supports modular error codes (e.g., `user-module_USER_NOT_FOUND`)
- Supports carrying extended data
- Supports internationalized messages
- Log level is WARN to avoid excessive warning information

### 3. Parameter Validation Exception

Handles all parameter validation related exceptions:

```java
@ExceptionHandler(value = {
    IllegalArgumentException.class,
    ValidationException.class,
    MethodArgumentNotValidException.class,
    BindException.class
})
public ApiResult<Boolean> handleIllegalArgumentException(Throwable e)
```

**Response Example:**
```json
{
  "code": "400",
  "message": "Invalid request parameters!",
  "data": false
}
```

**Log Level:** WARN

### 4. HTTP Method Not Supported

Handles cases where HTTP method is not supported:

```java
@ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
public ApiResult<Boolean> handleHttpRequestMethodNotSupportedException(
    Throwable e
)
```

**Response Example:**
```json
{
  "code": "405",
  "message": "Method not allowed!",
  "data": false
}
```

**Log Level:** WARN

### 5. Duplicate Key Exception

Handles database unique constraint conflicts, etc.:

```java
@ExceptionHandler(value = DuplicateKeyException.class)
public ApiResult<Boolean> handleDuplicateKeyException(DuplicateKeyException e)
```

**Response Example:**
```json
{
  "code": "409",
  "message": "Duplicate records!",
  "data": false
}
```

**Log Level:** WARN

## Configuration

### Configuration File

Add the following configuration to `application.yml`:

```yaml
spring:
  whale:
    web-mvc:
      exception:
        # Enable internationalization (default: false)
        enable-i18n: true
        # 500 error message
        message-500: Server abnormal, please try again later!
        code-500: http.error.500
        # 400 error message
        message-400: Invalid request parameters!
        code-400: http.error.400
        # 405 error message
        message-405: Method not allowed!
        code-405: http.error.405
        # 409 error message
        message-409: Duplicate records!
        code-409: http.error.409
```

### Configuration Items

| Item | Type | Default | Description |
|------|------|---------|-------------|
| `enable-i18n` | boolean | false | Enable internationalized messages |
| `message-500` | String | Server abnormal... | Default message for 500 errors |
| `code-500` | String | http.error.500 | I18n key for 500 errors |
| `message-400` | String | Invalid request... | Default message for 400 errors |
| `code-400` | String | http.error.400 | I18n key for 400 errors |
| `message-405` | String | Method not allowed! | Default message for 405 errors |
| `code-405` | String | http.error.405 | I18n key for 405 errors |
| `message-409` | String | Duplicate records! | Default message for 409 errors |
| `code-409` | String | http.error.409 | I18n key for 409 errors |

### Internationalization Configuration

#### 1. Create I18n Resource Files

`resources/messages.properties` (Default):

```properties
http.error.500=Server abnormal, please try again later!
http.error.400=Invalid request parameters!
http.error.405=Method not allowed!
http.error.409=Duplicate records!
USER_NOT_FOUND=User not found
```

`resources/messages_zh_CN.properties` (Chinese):

```properties
http.error.500=服务器异常，请稍后重试！
http.error.400=无效请求参数！
http.error.405=方法不允许！
http.error.409=重复记录！
USER_NOT_FOUND=用户不存在
ORDER_NOT_FOUND=Order not found
```

`resources/messages_ja_JP.properties` (Japanese):

```properties
http.error.500=サーバーエラーが発生しました
http.error.400=無効なリクエストパラメータ！
http.error.405=メソッドが許可されていません！
http.error.409=重複レコード！
USER_NOT_FOUND=ユーザーが存在しません
```

#### 2. Dynamic Language Switching

```java
// Set locale in request
LocaleContextHolder.setLocale(Locale.JAPAN);

// Or set via interceptor based on request header
@GetMapping(value = "/users/{id}", headers = "Accept-Language=ja-JP")
public ResponseEntity<UserDTO> getUserJapanese(@PathVariable Long id) {
    LocaleContextHolder.setLocale(Locale.JAPAN);
    return ResponseEntity.ok(userService.getUser(id));
}
```

## Usage Examples

### 1. Throw Business Exception

```java
import io.github.springwhale.framework.core.exception.BusinessException;

@Service
public class UserService {
    
    public User getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw BusinessException.create(
                "USER_NOT_FOUND",
                "User not found"
            );
        }
        return user;
    }
    
    public void createUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            throw BusinessException.create(
                "EMAIL_EXISTS",
                "Email already exists",
                data
            );
        }
        userRepository.save(user);
    }
    
    public void updateUser(User user) {
        // Business exception with module name
        throw BusinessException.createWithModule(
            "USER_NOT_FOUND",
            "user-module",
            "User not found"
        );
    }
}
```

### 2. Using in Controller

```java
import io.github.springwhale.framework.core.model.ApiResult;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // Return value will be automatically wrapped as ApiResult<User>
        return userService.getUserById(id);
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        // Return value will be automatically wrapped as ApiResult<User>
        userService.createUser(user);
        return user;
    }
    
    @PutMapping("/{id}")
    public void updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        // void type will be automatically wrapped as ApiResult.success()
        userService.updateUser(user);
    }
    
    @GetMapping("/list")
    public List<User> listUsers() {
        // Collection types will also be automatically wrapped
        return userService.findAll();
    }
}
```

**Automatic Conversion:**
- `User` → `ApiResult<User>`
- `List<User>` → `ApiResult<List<User>>`
- `void` → `ApiResult<Boolean>` (value is true)
- `ApiResult<T>` → Remains unchanged (won't be double-wrapped)

### 3. Parameter Validation Exception

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
        // If parameter validation fails, automatically returns 400 error
        // Return value will be automatically wrapped as ApiResult<User>
        return userService.createUser(request.toEntity());
    }
    
    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam @NotBlank String keyword,
            @RequestParam(required = false, defaultValue = "1") Integer page) {
        // If parameter is empty or invalid format, automatically returns 400 error
        return userService.searchUsers(keyword, page);
    }
}

@Data
public class CreateUserRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
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

**Success Response:**
```json
{
  "code": "200",
  "message": "success",
  "data": {
    "id": 1,
    "username": "John Doe",
    "email": "john@example.com"
  }
}
```

**Validation Failed Response:**
```json
{
  "code": "400",
  "message": "Invalid request parameters!",
  "data": false
}
```

### 4. Business Exception with I18n

```java
@Service
public class OrderService {
    
    public Order getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            // Use i18n key, actual message retrieved from i18n resource file
            throw BusinessException.createWithI18n(
                "ORDER_NOT_FOUND",
                "error.order.notfound",
                "Order not found"
            );
        }
        return order;
    }
    
    public void createOrder(Order order) {
        // i18n exception with module and data
        Map<String, Object> data = new HashMap<>();
        data.put("productId", order.getProductId());
        throw BusinessException.createI18nWithModule(
            "PRODUCT_OUT_OF_STOCK",
            "error.product.outofstock",
            "order-module",
            "Product out of stock",
            data
        );
    }
}
```

### 5. Frontend Call Examples

```javascript
// Normal request - Success response
fetch('/api/users/1')
  .then(response => response.json())
  .then(data => {
    // data structure: { code: '200', message: 'success', data: {...} }
    if (data.code === '200') {
      console.log('User info:', data.data);
    } else {
      console.error('Business error:', data.message);
    }
  });

// Catch business exception
fetch('/api/users/999')
  .then(response => response.json())
  .then(data => {
    // data structure: { code: 'USER_NOT_FOUND', message: 'User not found', data: null/false }
    if (data.code !== '200') {
      alert(`Error [${data.code}]: ${data.message}`);
    }
  });

// Parameter validation failed
fetch('/api/users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ invalid: 'data' })
})
.then(response => response.json())
.then(data => {
  // data structure: { code: '400', message: 'Invalid request parameters!', data: false }
  if (data.code === '400') {
    alert('Parameter validation failed: ' + data.message);
  }
});

// Create success - Returns wrapped data
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
  // data structure: { code: '200', message: 'success', data: { id: 1, username: 'new_user', ... } }
  if (data.code === '200') {
    console.log('Create successful, user ID:', data.data.id);
  }
});

// Catch duplicate record exception
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
  // data structure: { code: '409', message: 'Duplicate records!', data: false }
  if (data.code === '409') {
    alert(data.message);
  }
});
```

**Unified Response Format:**

All Controller return values are automatically wrapped as `ApiResult` format:

| Scenario | HTTP Status | Response Body Format |
|----------|-------------|---------------------|
| Success | 200 | `{ code: '200', message: 'success', data: {...} }` |
| Business Exception | 200 | `{ code: 'ERROR_CODE', message: 'Error message', data: null/false }` |
| Parameter Validation Failed | 200 | `{ code: '400', message: 'Invalid request parameters!', data: false }` |
| Server Error | 200 | `{ code: '500', message: 'Server abnormal', data: false }` |

**Note:** All exceptions return HTTP 200, distinguished by the `code` field.

## Best Practices

### 1. Business Exception Usage Standards

**Recommended:**
```java
// ✅ Use meaningful error codes
throw BusinessException.create("USER_NOT_FOUND", "User not found");

// ✅ Use modular naming
throw BusinessException.createWithModule(
    "NOT_FOUND", 
    "user-module", 
    "User not found"
);

// ✅ Carry extended data
Map<String, Object> data = Map.of("userId", userId);
throw BusinessException.create(
    "USER_NOT_FOUND", 
    "User not found", 
    data
);

// ✅ Use internationalization
throw BusinessException.createWithI18n(
    "USER_NOT_FOUND",
    "error.user.notfound"
);
```

**Not Recommended:**
```java
// ❌ Error code without meaning
throw BusinessException.create("ERROR_1", "Something went wrong");

// ❌ Hardcoded error messages
throw BusinessException.create("NO_PERMISSION", "You don't have permission to access this resource");

// ❌ Expose sensitive information
throw BusinessException.create("DB_ERROR", e.getMessage());
```

### 2. Log Level Selection

The exception handler has built-in reasonable log levels:

- **ERROR**: Unknown exceptions (require immediate attention)
- **WARN**: Business exceptions, parameter validation exceptions (normal business flow)
- **DEBUG**: Stack traces (for debugging)

No need to duplicate logging in business code.

### 3. Internationalization Best Practices

**Production Environment:**
```yaml
spring:
  whale:
    web-mvc:
      exception:
        enable-i18n: true
```

**Development Environment:**
```yaml
spring:
  whale:
    web-mvc:
      exception:
        enable-i18n: false
```

**I18n Resource File Organization:**
```
resources/
├── messages.properties          # Default (English)
├── messages_zh_CN.properties   # Simplified Chinese
├── messages_zh_TW.properties   # Traditional Chinese
├── messages_ja_JP.properties   # Japanese
└── messages_ko_KR.properties   # Korean
```

### 4. Error Code Naming Convention

**Recommended error code naming convention:**

```
[MODULE_]RESOURCE_ACTION
```

**Examples:**
- `USER_NOT_FOUND` - User not found
- `ORDER_CREATE_FAILED` - Order creation failed
- `PAYMENT_INSUFFICIENT_BALANCE` - Payment insufficient balance
- `user-module_USER_LOCKED` - User module - User locked

### 5. Testing Recommendations

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
        // Set invalid data
        
        ApiResult<User> result = userController.createUser(request);
        
        assertEquals("400", result.getCode());
    }
}
```

## Considerations

### 1. Performance Considerations

- Exception handling has performance overhead, should be used for true exceptional cases
- Normal business logic should not rely on exceptions for flow control
- For expected business validations, return results from Service layer instead of throwing exceptions

### 2. Security

**Don't expose sensitive information:**
```java
// ❌ Insecure - Exposes database error details
throw new RuntimeException(e.getMessage());

// ✅ Secure - Returns generic error message
throw BusinessException.create("SYSTEM_ERROR", "System busy, please try again later");
```

### 3. Transaction Handling

Exceptions automatically trigger transaction rollback (RuntimeException):

```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    // If insufficient balance throws BusinessException
    // Will automatically rollback the entire transaction
    accountService.deduct(fromId, amount);
    accountService.add(toId, amount);
}
```

### 4. Async Processing

Exceptions in async methods may not be caught by the global exception handler:

```java
// ❌ Async method exceptions won't be caught by global exception handler
@Async
public void asyncProcess() {
    throw BusinessException.create("ERROR", "Async error");
}

// ✅ Should catch and handle exceptions inside async method
@Async
public CompletableFuture<Void> asyncProcess() {
    try {
        // Processing logic
    } catch (Exception e) {
        log.error("Async processing failed", e);
        throw e;
    }
}
```

### 5. Priority with Other Exception Handlers

If multiple `@RestControllerAdvice` are defined in the project, use `@Order` annotation to control priority:

```java
@Component
@Order(1) // Lower number = higher priority
@RestControllerAdvice
public class CustomExceptionHandler {
    // Custom exception handler
}
```

## Related Resources

- [Spring Boot Exception Handling Official Docs](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-web-applications.spring-hateoas)
- [Spring MVC Exception Handling](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-exceptionhandlers)
- [Bean Validation Specification](https://beanvalidation.org/)
