# Spring Whale Response Body Auto-Wrapping

Spring Whale framework provides intelligent response body auto-wrapping functionality based on Spring MVC's `ResponseBodyAdvice` interface, which can automatically wrap Controller method return values into a unified `ApiResult` format.

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
- [Supported Return Types](#supported-return-types)
- [@AdviceIgnore Annotation](#adviceignore-annotation)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)

## Features

### Core Features

- ✅ **Auto-wrapping** - Plain objects are automatically wrapped as `ApiResult.success(data)`
- ✅ **Smart Recognition** - Automatically recognizes types that don't need wrapping (such as `ApiResult`, `ResponseEntity`)
- ✅ **Unified Format** - All successful responses return a unified `ApiResult` format
- ✅ **Flexible Control** - Supports skipping wrapping for specific methods via annotations
- ✅ **Subclass Support** - Automatically recognizes subclasses of `ResponseEntity` and `HttpEntity`
- ✅ **void Support** - void methods return empty success response
- ✅ **Auto-registration** - Automatically configured through Spring Boot, no manual registration required

### Wrapping Rules

| Return Type | Wrapped? | Description |
|------------|----------|-------------|
| Plain Object (POJO) | ✅ Yes | Wrapped as `ApiResult.success(data)` |
| String | ✅ Yes | Wrapped as `ApiResult.success(data)` |
| List/Set | ✅ Yes | Wrapped as `ApiResult.success(data)` |
| Map | ✅ Yes | Wrapped as `ApiResult.success(data)` |
| Primitive Types | ✅ Yes | Wrapped as `ApiResult.success(data)` |
| void | ✅ Yes | Returns `ApiResult.success()` |
| ApiResult | ❌ No | Returned directly, no double-wrapping |
| ResponseEntity | ❌ No | Returned directly, preserves original behavior |
| HttpEntity | ❌ No | Returned directly, preserves original behavior |
| ModelAndView | ❌ No | Returned directly, used for view rendering |
| Methods marked with @AdviceIgnore | ❌ No | Returned directly, skips wrapping |

## How It Works

`SpringWhaleWebMvcResponseBodyAdvice` implements Spring MVC's `ResponseBodyAdvice<Object>` interface and controls response body processing through two core methods:

### 1. supports() Method

Determines whether a return value should be wrapped:

```java
@Override
public boolean supports(MethodParameter returnType, 
                       Class<? extends HttpMessageConverter<?>> converterType) {
    // Check if in ignore list
    if (ignoreList.contains(returnClass)) {
        return false;
    }
    
    // Check if has @AdviceIgnore annotation
    if (returnType.hasMethodAnnotation(AdviceIgnore.class)) {
        return false;
    }
    
    // Check if is subclass of ResponseEntity or HttpEntity
    if (ResponseEntity.class.isAssignableFrom(returnClass) ||
        HttpEntity.class.isAssignableFrom(returnClass)) {
        return false;
    }
    
    return true;
}
```

### 2. beforeBodyWrite() Method

Wraps the response body before writing:

```java
@Override
public Object beforeBodyWrite(Object body, MethodParameter returnType, 
                             MediaType selectedContentType, 
                             Class<? extends HttpMessageConverter<?>> selectedConverterType, 
                             ServerHttpRequest request, 
                             ServerHttpResponse response) {
    // Return empty success for void type
    if (returnTypeClass.equals(Void.TYPE)) {
        return ApiResult.success();
    }
    
    // Wrap other types as success
    return ApiResult.success(body);
}
```

## Supported Return Types

### 1. Types That Are Auto-Wrapped

#### Plain Object

```java
@GetMapping("/user")
public User getUser() {
    return new User(1L, "John");
}
```

**Actual Response:**
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

#### String Type

```java
@GetMapping("/hello")
public String hello() {
    return "Hello World";
}
```

**Actual Response:**
```json
{
  "code": "200",
  "message": "success",
  "data": "Hello World"
}
```

#### List Type

```java
@GetMapping("/users")
public List<User> listUsers() {
    return List.of(new User(1L, "John"), new User(2L, "Jane"));
}
```

**Actual Response:**
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

#### void Type

```java
@GetMapping("/success")
public void doSomething() {
    // Perform some operation
}
```

**Actual Response:**
```json
{
  "code": "200",
  "message": "success",
  "data": true
}
```

### 2. Types That Are NOT Wrapped

#### ApiResult Type

```java
@GetMapping("/api-result")
public ApiResult<User> getUser() {
    return ApiResult.success(new User(1L, "John"));
}
```

**Actual Response:**
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

**Note:** Not double-wrapped, returns the original `ApiResult` directly.

#### ResponseEntity Type

```java
@GetMapping("/response-entity")
public ResponseEntity<User> getUser() {
    return ResponseEntity.ok(new User(1L, "John"));
}
```

**Actual Response:**
```json
{
  "id": 1,
  "name": "John"
}
```

**Note:** `ResponseEntity` is not wrapped and returns the User object directly. Suitable for scenarios requiring custom HTTP status codes.

#### HttpEntity Type

```java
@GetMapping("/http-entity")
public HttpEntity<User> getUser() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Custom-Header", "value");
    return new HttpEntity<>(new User(1L, "John"), headers);
}
```

**Actual Response:**
```json
{
  "id": 1,
  "name": "John"
}
```

#### ResponseEntity Subclass

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

**Actual Response:**
```json
{
  "id": 1,
  "name": "John"
}
```

**Note:** Subclasses of `ResponseEntity` are also automatically recognized and excluded.

## @AdviceIgnore Annotation

`@AdviceIgnore` is a marker annotation used to mark methods that should not be wrapped.

### Annotation Definition

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdviceIgnore {
}
```

### Use Cases

Use this annotation when you want a specific Controller method to return raw data without automatic wrapping.

### Example Usage

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

**Comparison:**

- `/user` returns: `{"code":"200","message":"success","data":{"id":1,"name":"John"}}`
- `/raw-user` returns: `{"id":1,"name":"John"}`

### Important Notes

- The annotation must be placed on a **method**
- The annotation retention policy is `RUNTIME`, detectable via reflection at runtime
- When this annotation is present, the method return value is returned **directly** without wrapping as `ApiResult`

## Usage Examples

### Basic Examples

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    
    // Auto-wrapped
    @GetMapping("/object")
    public User getObject() {
        return new User(1L, "John");
    }
    
    // Auto-wrapped
    @GetMapping("/string")
    public String getString() {
        return "Hello";
    }
    
    // Auto-wrapped
    @GetMapping("/list")
    public List<User> getList() {
        return List.of(new User(1L, "John"));
    }
    
    // Not wrapped (already ApiResult)
    @GetMapping("/api-result")
    public ApiResult<User> getApiResult() {
        return ApiResult.success(new User(1L, "John"));
    }
    
    // Not wrapped (ResponseEntity)
    @GetMapping("/response-entity")
    public ResponseEntity<User> getResponseEntity() {
        return ResponseEntity.ok(new User(1L, "John"));
    }
    
    // Not wrapped (using annotation)
    @GetMapping("/raw")
    @AdviceIgnore
    public User getRaw() {
        return new User(1L, "John");
    }
}
```

### Advanced Examples

```java
@RestController
@RequestMapping("/api/users")
public class AdvancedUserController {
    
    @Autowired
    private UserService userService;
    
    // Standard success response
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // Need to return specific HTTP status code
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // Need custom response format
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

## Best Practices

### 1. Prefer Auto-Wrapping

In most cases, simply return plain objects and enjoy the convenience of auto-wrapping:

```java
// ✅ Recommended
@GetMapping("/user")
public User getUser() {
    return userService.getUser();
}

// ❌ Not recommended (redundant)
@GetMapping("/user")
public ApiResult<User> getUser() {
    return ApiResult.success(userService.getUser());
}
```

### 2. Use ResponseEntity Only When Necessary

Use `ResponseEntity` when you need to customize HTTP status codes or response headers:

```java
// Need to return 201 Created
@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) {
    User created = userService.create(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// Need to add custom response headers
@GetMapping("/cached")
public ResponseEntity<User> getCachedUser() {
    User user = userService.getUser();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cache-Control", "max-age=3600");
    return ResponseEntity.ok().headers(headers).body(user);
}
```

### 3. Use @AdviceIgnore Judiciously

Only use `@AdviceIgnore` when you truly need to return raw data:

```java
// ✅ Reasonable use case: Return special format needed by frontend
@GetMapping("/chart-data")
@AdviceIgnore
public Map<String, Object> getChartData() {
    Map<String, Object> chartData = new HashMap<>();
    chartData.put("labels", List.of("Jan", "Feb", "Mar"));
    chartData.put("values", List.of(10, 20, 30));
    return chartData;
}

// ❌ Not recommended: No special reason
@GetMapping("/user")
@AdviceIgnore  // Why not use auto-wrapping?
public User getUser() {
    return userService.getUser();
}
```

### 4. Maintain Consistency

Within the same project, try to maintain consistency in response format:

- Use auto-wrapping for most endpoints
- Use `ResponseEntity` or `@AdviceIgnore` only for endpoints that truly require it
- Avoid mixing multiple response formats, which increases frontend complexity

### 5. Document Your APIs

For endpoints using `@AdviceIgnore` or special response formats, clearly document them in API documentation:

```java
/**
 * Get chart data
 * 
 * @return Special chart data format (not wrapped)
 * 
 * Response example:
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

## Technical Details

### Auto-Configuration

`SpringWhaleWebMvcResponseBodyAdvice` is automatically registered through Spring Boot's auto-configuration mechanism:

```java
@RestControllerAdvice
public class SpringWhaleWebMvcResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    // ...
}
```

No manual configuration is required; just include the `spring-whale-webmvc` dependency.

### Ignore List

You can dynamically add types to ignore via the `addIgnore()` method:

```java
@Autowired
private SpringWhaleWebMvcResponseBodyAdvice advice;

// Add custom ignore type
advice.addIgnore(MyCustomType.class);
```

### Integration with Global Exception Handling

`ResponseBodyAdvice` works in conjunction with the global exception handler (`SpringWhaleWebMvcExceptionHandler`):

1. Normal response → `ResponseBodyAdvice` wraps → Returns `ApiResult.success()`
2. Exception → Caught by `ExceptionHandler` → Returns `ApiResult.error()`

Together, they ensure consistency in response format.
