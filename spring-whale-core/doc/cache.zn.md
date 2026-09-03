# Spring Whale 缓存

Spring Whale 框架提供了抽象缓存层，单体架构自动使用 Caffeine 本地缓存，微服务架构一键切换 Redis 分布式缓存，零代码改动。

## 目录

- [功能特性](#功能特性)
- [架构设计](#架构设计)
- [配置说明](#配置说明)
- [程序化 API](#程序化-api)
- [声明式 API](#声明式-api)
- [缓存穿透保护](#缓存穿透保护)
- [最佳实践](#最佳实践)
- [注意事项](#注意事项)

## 功能特性

### 核心特性

- ✅ **本地/分布式切换** - 默认 Caffeine 本地缓存，一行配置切换 Redis
- ✅ **Per-Key 过期时间** - 每个缓存 key 支持独立的过期时间
- ✅ **缓存穿透保护** - 自动缓存 null 值占位，防止缓存穿透攻击
- ✅ **Spring Cache 兼容** - 完全兼容 `@Cacheable` / `@CacheEvict` / `@CachePut` 注解
- ✅ **程序化 API** - `WhaleCacheManager` 提供丰富的程序化接口，支持精细控制
- ✅ **自动配置** - 根据 classpath 和配置自动选择合适的缓存后端
- ✅ **线程安全** - 所有缓存操作均为线程安全设计
- ✅ **单体零配置** - 单服务部署无需任何缓存配置

### 支持的缓存后端

| 后端         | 适用场景      | 默认 | 说明                           |
|-------------|-----------|------|------------------------------|
| **Caffeine** | 单体 / 本地  | ✅   | 高性能 JVM 本地缓存，零外部依赖          |
| **Redis**    | 微服务       |      | 分布式共享缓存，多实例数据一致性            |

## 架构设计

```
┌──────────────────────────────────────────────────────────────┐
│                        应用代码                               │
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
   │ (单体 - 默认)         │     │  (微服务)                │
   └─────────────────────┘     └─────────────────────────┘
```

## 配置说明

### 配置文件

在 `application.yml` 中添加以下配置：

```yaml
spring:
  whale:
    cache:
      # 缓存类型：LOCAL（默认）或 REDIS
      type: LOCAL
      # 默认过期时间（默认：30m）
      default-ttl: 30m
      # Redis key 前缀（仅 REDIS 模式生效）
      key-prefix: "whale:cache:"
      # 是否缓存 null 值以防止缓存穿透（默认：true）
      cache-null-values: true
      # null 值占位的过期时间（默认：1m）
      null-value-ttl: 1m
```

### 单体架构（默认）

零配置，自动使用 Caffeine 本地缓存：

```yaml
# 无需任何配置，自动使用 Caffeine 本地缓存
# 等价于：
spring:
  whale:
    cache:
      type: LOCAL
```

### 微服务架构

引入 `spring-boot-starter-data-redis` 依赖，配置 Redis 连接：

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
      default-ttl: 10m          # 微服务建议设短一些，保证数据一致性
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

### 配置项

| 配置项                  | 类型      | 默认值           | 说明                          |
|----------------------|---------|---------------|-----------------------------|
| `type`               | Enum    | LOCAL         | 缓存类型：LOCAL=Caffeine，REDIS=Redis |
| `default-ttl`        | Duration | 30m           | 所有缓存 key 的默认过期时间             |
| `key-prefix`         | String  | whale:cache:  | Redis key 前缀，防止 key 冲突       |
| `cache-null-values`  | boolean | true          | 是否缓存 null 值作为占位             |
| `null-value-ttl`     | Duration | 1m            | null 值占位的过期时间               |

## 程序化 API

注入 `WhaleCacheManager` 使用程序化接口：

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final WhaleCacheManager cacheManager;

    // ===== 简单读写 =====
    
    public UserDto getUser(Long userId) {
        WhaleCache cache = cacheManager.getCache("user");

        // 从缓存读取
        UserDto cached = cache.get("user:" + userId, UserDto.class);
        if (cached != null) {
            return cached;
        }

        UserDto user = userRepository.findById(userId);
        cache.put("user:" + userId, user);
        return user;
    }

    // ===== 读取或加载（自动防穿透） =====
    
    public UserDto getUserV2(Long userId) {
        WhaleCache cache = cacheManager.getCache("user");
        return cache.get("user:" + userId, UserDto.class, () -> {
            return userRepository.findById(userId);
        });
    }

    // ===== Per-Key 过期时间 =====

    public OrderDto getOrder(Long orderId) {
        WhaleCache cache = cacheManager.getCache("order");

        // 热点数据：10 秒过期
        return cache.get("order:" + orderId, OrderDto.class, () -> {
            return orderRepository.findById(orderId);
        }, Duration.ofSeconds(10));
    }

    // ===== 写入操作 =====

    public void updateUser(UserDto user) {
        WhaleCache cache = cacheManager.getCache("user");
        
        // 默认 TTL 写入
        cache.put("user:" + user.getId(), user);
        
        // 自定义 TTL 写入
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

### API 参考

| 方法 | 说明 |
|------|------|
| `getCache(name)` | 获取或创建缓存命名空间 |
| `get(key, type)` | 按 key 读取缓存值 |
| `get(key, type, loader)` | 读取或加载，使用默认 TTL |
| `get(key, type, loader, ttl)` | 读取或加载，使用自定义 TTL |
| `put(key, value)` | 写入，使用默认 TTL |
| `put(key, value, ttl)` | 写入，使用自定义 TTL |
| `evict(key)` | 删除单个缓存项 |
| `clear()` | 清空命名空间下所有缓存 |
| `exists(key)` | 检查 key 是否存在 |

## 声明式 API

完全兼容 Spring 的 `@Cacheable` / `@CacheEvict` / `@CachePut` 注解：

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

> **注意**：`@Cacheable` 不支持 per-key TTL，同一缓存命名空间下的所有 key 共享 `defaultTtl`。需要 per-key TTL 请使用程序化 API。

## 缓存穿透保护

当 `cache-null-values` 开启时（默认开启），loader 返回 null 时自动缓存 null 占位符：

```
请求 → 缓存未命中 → Loader 返回 null → 缓存 null 占位（短 TTL） → 返回 null
下次请求 → 缓存命中（null 占位） → 直接返回 null（不查数据库）
```

这可以防止大量请求查询不存在的数据绕过缓存直接打到数据库（缓存穿透攻击）。

### 配置

```yaml
spring:
  whale:
    cache:
      cache-null-values: true   # 启用 null 值缓存（默认开启）
      null-value-ttl: 1m        # null 占位短 TTL
```

## 最佳实践

### 1. 缓存 Key 设计

始终包含实体类型和 ID，以及变化参数：

```java
// 推荐：唯一且语义清晰
cache.put("user:" + userId, user);
cache.put("order:" + orderId + ":status:" + status, orders);

// 不推荐：容易冲突
cache.put(userId.toString(), user);
```

### 2. TTL 策略

| 数据类型 | 推荐 TTL | 原因 |
|---------|---------|------|
| 用户资料 | 30m ~ 1h | 变更频率低 |
| 用户权限 | 5m ~ 10m | 需要快速反映权限变更 |
| 配置数据 | 10m ~ 30m | 可能通过管理后台修改 |
| 热点数据 | 10s ~ 1m | 高并发，短 TTL 防止脏数据 |
| 参考数据 | 1h ~ 24h | 几乎不变 |

### 3. 单体 vs 微服务

| 架构 | 缓存类型 | TTL 建议 |
|------|---------|---------|
| 单体 | LOCAL（Caffeine） | 较长 TTL，无一致性问题 |
| 微服务 | REDIS | 较短 TTL（5m~10m），保证多实例数据一致性 |

### 4. Spring Cache 注解使用

读操作使用 `@Cacheable`，删除操作使用 `@CacheEvict`，更新操作使用 `@CachePut`：

```java
@Cacheable(value = "product", key = "#productId")
public ProductDto getProduct(Long productId) { ... }

@CacheEvict(value = "product", key = "#productId")
public void deleteProduct(Long productId) { ... }

@CachePut(value = "product", key = "#result.id")
public ProductDto updateProduct(ProductDto dto) { ... }
```

## 注意事项

1. **Redis 依赖**：`spring-whale-core` 中 `spring-boot-starter-data-redis` 声明为 `provided` scope。单体项目无需引入，微服务项目需显式添加。

2. **Key 序列化**：Redis 模式下，值通过 Jackson 序列化为 JSON 存储。确保缓存对象支持 Jackson 序列化。

3. **缓存命名空间**：每次 `getCache(name)` 创建独立的命名空间，不同命名空间的 key 不会冲突。

4. **线程安全**：所有缓存操作均为线程安全。Caffeine 内部使用 ConcurrentHashMap；Redis 使用 Lettuce 连接池。

5. **Spring Boot 4 兼容**：本模块针对 Spring Boot 4.x 设计和测试。Spring Boot 4 中已移除 `CacheAutoConfiguration`，因此 `WhaleCacheAutoConfiguration` 提供了自己的适配器。