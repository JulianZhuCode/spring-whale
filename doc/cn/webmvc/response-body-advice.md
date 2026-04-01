# Spring Whale 响应体自动包装

Spring Whale 框架提供了智能的响应体自动包装功能，基于 Spring MVC 的 `ResponseBodyAdvice` 接口实现，可以自动将 Controller 方法的返回值包装为统一的 `ApiResult` 格式。

## 目录

- [功能特性](#功能特性)
- [工作原理](#工作原理)
- [支持的返回类型](#支持的返回类型)
- [@AdviceIgnore 注解](#adviceignore-注解)
- [使用示例](#使用示例)
- [最佳实践](#最佳实践)

## 功能特性

### 核心特性

- ✅ **自动包装** - 普通对象自动包装为 `ApiResult.success(data)`
- ✅ **智能识别** - 自动识别不需要包装的类型（如 `ApiResult`、`ResponseEntity`）
- ✅ **统一格式** - 所有成功响应都返回统一的 `ApiResult` 格式
- ✅ **灵活控制** - 支持通过注解跳过特定方法的包装
- ✅ **子类支持** - 自动识别 `ResponseEntity` 和 `HttpEntity` 的子类
- ✅ **void 支持** - void 方法返回空成功响应
- ✅ **自动注册** - 通过 Spring Boot 自动配置，无需手动注册

### 包装规则

| 返回类型 | 是否包装 | 说明 |
|---------|---------|------|
| 普通对象（POJO） | ✅ 是 | 包装为 `ApiResult.success(data)` |
| String | ✅ 是 | 包装为 `ApiResult.success(data)` |
| List/Set | ✅ 是 | 包装为 `ApiResult.success(data)` |
| Map | ✅ 是 | 包装为 `ApiResult.success(data)` |
| 基本数据类型 | ✅ 是 | 包装为 `ApiResult.success(data)` |
| void | ✅ 是 | 返回 `ApiResult.success()` |
| ApiResult | ❌ 否 | 直接返回，不重复包装 |
| ResponseEntity | ❌ 否 | 直接返回，保持原有行为 |
| HttpEntity | ❌ 否 | 直接返回，保持原有行为 |
| ModelAndView | ❌ 否 | 直接返回，用于视图渲染 |
| @AdviceIgnore 标记的方法 | ❌ 否 | 直接返回，跳过包装 |

## 工作原理

`SpringWhaleWebMvcResponseBodyAdvice` 实现了 Spring MVC 的 `ResponseBodyAdvice<Object>` 接口，通过两个核心方法控制响应体的处理：

### 1. supports() 方法

判断某个返回值是否需要被包装：

```java
@Override
public boolean supports(MethodParameter returnType, 
                       Class<? extends HttpMessageConverter<?>> converterType) {
    // 检查是否在忽略列表中
    if (ignoreList.contains(returnClass)) {
        return false;
    }
    
    // 检查是否有@AdviceIgnore 注解
    if (returnType.hasMethodAnnotation(AdviceIgnore.class)) {
        return false;
    }
    
    // 检查是否是 ResponseEntity 或 HttpEntity 的子类
    if (ResponseEntity.class.isAssignableFrom(returnClass) ||
        HttpEntity.class.isAssignableFrom(returnClass)) {
        return false;
    }
    
    return true;
}
```

### 2. beforeBodyWrite() 方法

在写入响应体之前进行包装：

```java
@Override
public Object beforeBodyWrite(Object body, MethodParameter returnType, 
                             MediaType selectedContentType, 
                             Class<? extends HttpMessageConverter<?>> selectedConverterType, 
                             ServerHttpRequest request, 
                             ServerHttpResponse response) {
    // void 类型返回空成功
    if (returnTypeClass.equals(Void.TYPE)) {
        return ApiResult.success();
    }
    
    // 其他类型包装为 success
    return ApiResult.success(body);
}
```

## 支持的返回类型

### 1. 自动包装的类型

#### 普通对象

```java
@GetMapping("/user")
public User getUser() {
    return new User(1L, "John");
}
```

**实际响应：**
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

#### String 类型

```java
@GetMapping("/hello")
public String hello() {
    return "Hello World";
}
```

**实际响应：**
```json
{
  "code": "200",
  "message": "success",
  "data": "Hello World"
}
```

#### List 类型

```java
@GetMapping("/users")
public List<User> listUsers() {
    return List.of(new User(1L, "John"), new User(2L, "Jane"));
}
```

**实际响应：**
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

#### void 类型

```java
@GetMapping("/success")
public void doSomething() {
    // 执行某些操作
}
```

**实际响应：**
```json
{
  "code": "200",
  "message": "success",
  "data": true
}
```

### 2. 不包装的类型

#### ApiResult 类型

```java
@GetMapping("/api-result")
public ApiResult<User> getUser() {
    return ApiResult.success(new User(1L, "John"));
}
```

**实际响应：**
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

**注意：** 不会重复包装，直接返回原始的 `ApiResult`。

#### ResponseEntity 类型

```java
@GetMapping("/response-entity")
public ResponseEntity<User> getUser() {
    return ResponseEntity.ok(new User(1L, "John"));
}
```

**实际响应：**
```json
{
  "id": 1,
  "name": "John"
}
```

**注意：** `ResponseEntity` 不会被包装，直接返回 User 对象。适用于需要自定义 HTTP 状态码的场景。

#### HttpEntity 类型

```java
@GetMapping("/http-entity")
public HttpEntity<User> getUser() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Custom-Header", "value");
    return new HttpEntity<>(new User(1L, "John"), headers);
}
```

**实际响应：**
```json
{
  "id": 1,
  "name": "John"
}
```

#### ResponseEntity 子类

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

**实际响应：**
```json
{
  "id": 1,
  "name": "John"
}
```

**注意：** `ResponseEntity` 的子类也会被自动识别并排除。

## @AdviceIgnore 注解

`@AdviceIgnore` 是一个标记注解，用于标注不需要被包装的方法。

### 注解定义

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdviceIgnore {
}
```

### 使用场景

当你希望某个特定的 Controller 方法返回原始数据，而不被自动包装时，可以使用此注解。

### 使用示例

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

**对比：**

- `/user` 返回：`{"code":"200","message":"success","data":{"id":1,"name":"John"}}`
- `/raw-user` 返回：`{"id":1,"name":"John"}`

### 注意事项

- 注解必须标注在**方法**上
- 注解保留策略为 `RUNTIME`，可以在运行时通过反射检测
- 标注此注解后，方法返回值会**直接返回**，不会被包装为 `ApiResult`

## 使用示例

### 基础示例

```java
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    
    // 自动包装
    @GetMapping("/object")
    public User getObject() {
        return new User(1L, "John");
    }
    
    // 自动包装
    @GetMapping("/string")
    public String getString() {
        return "Hello";
    }
    
    // 自动包装
    @GetMapping("/list")
    public List<User> getList() {
        return List.of(new User(1L, "John"));
    }
    
    // 不包装（已经是 ApiResult）
    @GetMapping("/api-result")
    public ApiResult<User> getApiResult() {
        return ApiResult.success(new User(1L, "John"));
    }
    
    // 不包装（ResponseEntity）
    @GetMapping("/response-entity")
    public ResponseEntity<User> getResponseEntity() {
        return ResponseEntity.ok(new User(1L, "John"));
    }
    
    // 不包装（使用注解）
    @GetMapping("/raw")
    @AdviceIgnore
    public User getRaw() {
        return new User(1L, "John");
    }
}
```

### 高级示例

```java
@RestController
@RequestMapping("/api/users")
public class AdvancedUserController {
    
    @Autowired
    private UserService userService;
    
    // 标准成功响应
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // 需要返回特定 HTTP 状态码
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    // 需要自定义响应格式
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

## 最佳实践

### 1. 优先使用自动包装

在大多数情况下，直接返回普通对象即可，享受自动包装带来的便利：

```java
// ✅ 推荐
@GetMapping("/user")
public User getUser() {
    return userService.getUser();
}

// ❌ 不推荐（多此一举）
@GetMapping("/user")
public ApiResult<User> getUser() {
    return ApiResult.success(userService.getUser());
}
```

### 2. 仅在必要时使用 ResponseEntity

当需要自定义 HTTP 状态码或响应头时，使用 `ResponseEntity`：

```java
// 需要返回 201 Created
@PostMapping
public ResponseEntity<User> createUser(@RequestBody User user) {
    User created = userService.create(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}

// 需要添加自定义响应头
@GetMapping("/cached")
public ResponseEntity<User> getCachedUser() {
    User user = userService.getUser();
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cache-Control", "max-age=3600");
    return ResponseEntity.ok().headers(headers).body(user);
}
```

### 3. 谨慎使用 @AdviceIgnore

只在确实需要返回原始数据时使用 `@AdviceIgnore`：

```java
// ✅ 合理场景：返回前端需要的特殊格式
@GetMapping("/chart-data")
@AdviceIgnore
public Map<String, Object> getChartData() {
    Map<String, Object> chartData = new HashMap<>();
    chartData.put("labels", List.of("Jan", "Feb", "Mar"));
    chartData.put("values", List.of(10, 20, 30));
    return chartData;
}

// ❌ 不推荐：没有特殊理由
@GetMapping("/user")
@AdviceIgnore  // 为什么不使用自动包装？
public User getUser() {
    return userService.getUser();
}
```

### 4. 保持一致性

在同一个项目中，尽量保持响应格式的一致性：

- 大部分接口使用自动包装
- 只有真正需要的接口才使用 `ResponseEntity` 或 `@AdviceIgnore`
- 避免混用多种响应格式，增加前端处理复杂度

### 5. 文档说明

对于使用 `@AdviceIgnore` 或使用特殊响应格式的接口，在 API 文档中明确说明：

```java
/**
 * 获取图表数据
 * 
 * @return 特殊的图表数据格式（未包装）
 * 
 * 响应示例:
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

## 技术细节

### 自动配置

`SpringWhaleWebMvcResponseBodyAdvice` 通过 Spring Boot 的自动配置机制自动注册：

```java
@RestControllerAdvice
public class SpringWhaleWebMvcResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    // ...
}
```

无需任何手动配置，只要引入 `spring-whale-webmvc` 依赖即可使用。

### 忽略列表

可以通过 `addIgnore()` 方法动态添加需要忽略的类型：

```java
@Autowired
private SpringWhaleWebMvcResponseBodyAdvice advice;

// 添加自定义忽略类型
advice.addIgnore(MyCustomType.class);
```

### 与全局异常处理的配合

`ResponseBodyAdvice` 与全局异常处理器（`SpringWhaleWebMvcExceptionHandler`）协同工作：

1. 正常响应 → `ResponseBodyAdvice` 包装 → 返回 `ApiResult.success()`
2. 异常情况 → `ExceptionHandler` 捕获 → 返回 `ApiResult.error()`

两者共同保证了响应格式的统一性。
