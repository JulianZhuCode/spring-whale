# Spring Whale Cache

Spring Whale framework provides an abstract caching layer that seamlessly switches between Caffeine (local cache) for
monolithic deployments and Redis (distributed cache) for microservice architectures, with zero code changes.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Configuration](#configuration)
- [Programmatic API](#programmatic-api)
- [Declarative API](#declarative-api)
- [Cache Penetration Protection](#cache-penetration-protection)
- [Best Practices](#best-practices)
- [Notes](#notes)

## Features

### Core Features

- ✅ **Local/Distributed Switch** - Caffeine local cache by default, switch to Redis with one config line
- ✅ **Per-Key TTL** - Each cache key supports independent expiration time
- ✅ **Cache Penetration Protection** - Automatically caches null values as placeholders to prevent cache penetration
- ✅ **Spring Cache Compatible** - Fully compatible with `@Cacheable` / `@CacheEvict` / `@CachePut` annotations
- ✅ **Programmatic API** - `WhaleCacheManager` provides a rich programmatic API for fine-grained control
- ✅ **Automatic Configuration** - Auto-configures the appropriate cache backend based on classpath and config
- ✅ **Thread-Safe** - All cache operations are designed to be thread-safe
- ✅ **Zero Config for Monolith** - No configuration required for single-service deployments

### Supported Cache Backends

| Backend     | Scenario           | Default | Description                                                        |
|-------------|--------------------|---------|--------------------------------------------------------------------|
| **Caffeine** | Monolithic / Local | ✅      | High-performance JVM-local cache, zero external dependencies       |
| **Redis**    | Microservices      |         | Distributed shared cache, multi-instance data consistency          |

## Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      Application Code                        │
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
   │ (Monolith - Default) │     │  (Microservices)        │
   └─────────────────────┘     └─────────────────────────┘
```

## Configuration

### Configuration File

Add the following configuration to `application.yml`:

```yaml
spring:
  whale:
    cache:
      # Cache type: LOCAL (default) or REDIS
      type: LOCAL
      # Default TTL for all cache keys (default: 30m)
      default-ttl: 30m
      # Redis key prefix (only effective in REDIS mode)
      key-prefix: "whale:cache:"
      # Whether to cache null values to prevent cache penetration (default: true)
      cache-null-values: true
      # TTL for null value placeholders (default: 1m)
      null-value-ttl: 1m
```

### Monolithic Architecture (Default)

Zero configuration required. Caffeine local cache is used by default:

```yaml
# No configuration needed - Caffeine local cache is used automatically
# Equivalent to:
spring:
  whale:
    cache:
      type: LOCAL
```

### Microservice Architecture

Add `spring-boot-starter-data-redis` dependency and configure Redis:

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
      default-ttl: 10m          # Shorter TTL recommended for microservices
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

### Configuration Items

| Item                 | Type    | Default        | Description                                                             |
|----------------------|---------|----------------|-------------------------------------------------------------------------|
| `type`               | Enum    | LOCAL          | Cache type: `LOCAL` for Caffeine, `REDIS` for Redis                     |
| `default-ttl`        | Duration | 30m            | Default expiration time for all cache keys                              |
| `key-prefix`         | String  | whale:cache:   | Key prefix for Redis (prevents key collisions)                          |
| `cache-null-values`  | boolean | true           | Whether to cache null values as placeholders                            |
| `null-value-ttl`     | Duration | 1m             | Expiration time for null value placeholders                             |

## Programmatic API

Inject `WhaleCacheManager` to use the programmatic API:

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final WhaleCacheManager cacheManager;

    // ===== Simple Read/Write =====
    
    public UserDto getUser(Long userId) {
        WhaleCache cache = cacheManager.getCache("user");

        // Read from cache
        UserDto cached = cache.get("user:" + userId, UserDto.class);
        if (cached != null) {
            return cached;
        }

        UserDto user = userRepository.findById(userId);
        cache.put("user:" + userId, user);
        return user;
    }

    // ===== Get-or-Load (with cache penetration protection) =====
    
    public UserDto getUserV2(Long userId) {
        WhaleCache cache = cacheManager.getCache("user");
        return cache.get("user:" + userId, UserDto.class, () -> {
            return userRepository.findById(userId);
        });
    }

    // ===== Per-Key TTL =====

    public OrderDto getOrder(Long orderId) {
        WhaleCache cache = cacheManager.getCache("order");

        // Hot data: 10-second TTL
        return cache.get("order:" + orderId, OrderDto.class, () -> {
            return orderRepository.findById(orderId);
        }, Duration.ofSeconds(10));
    }

    // ===== Write Operations =====

    public void updateUser(UserDto user) {
        WhaleCache cache = cacheManager.getCache("user");
        
        // Write with default TTL
        cache.put("user:" + user.getId(), user);
        
        // Write with custom TTL
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

### API Reference

| Method | Description |
|--------|-------------|
| `getCache(name)` | Get or create a cache namespace |
| `get(key, type)` | Read cached value by key |
| `get(key, type, loader)` | Get or load with default TTL |
| `get(key, type, loader, ttl)` | Get or load with custom TTL |
| `put(key, value)` | Write with default TTL |
| `put(key, value, ttl)` | Write with custom TTL |
| `evict(key)` | Delete a single cache entry |
| `clear()` | Clear all entries in the namespace |
| `exists(key)` | Check if key exists |

## Declarative API

Fully compatible with Spring's `@Cacheable` / `@CacheEvict` / `@CachePut` annotations:

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

> **Note**: `@Cacheable` does not support per-key TTL. All keys in the same cache namespace share the `defaultTtl`.
> Use the programmatic API for per-key TTL.

## Cache Penetration Protection

When `cache-null-values` is enabled (default), null values returned by the loader are cached as placeholders:

```
Request → Cache Miss → Loader returns null → Cache null placeholder (short TTL) → Return null
Next Request → Cache Hit (null placeholder) → Return null directly (no DB call)
```

This prevents cache penetration attacks where a large number of requests for non-existent data bypass the cache and hit
the database.

### Configuration

```yaml
spring:
  whale:
    cache:
      cache-null-values: true   # Enable null value caching (default)
      null-value-ttl: 1m        # Short TTL for null placeholders
```

## Best Practices

### 1. Cache Key Design

Always include the entity type and ID, plus any variation parameters:

```java
// Good: unique and descriptive
cache.put("user:" + userId, user);
cache.put("order:" + orderId + ":status:" + status, orders);

// Bad: easy to collide
cache.put(userId.toString(), user);
```

### 2. TTL Strategy

| Data Type | Recommended TTL | Reason |
|-----------|----------------|--------|
| User profile | 30m ~ 1h | Changes infrequently |
| User permissions | 5m ~ 10m | Need to reflect permission changes quickly |
| Configuration data | 10m ~ 30m | May change via admin panel |
| Hotspot data | 10s ~ 1m | High concurrency, short TTL prevents stale data |
| Reference data | 1h ~ 24h | Rarely changes |

### 3. Monolith vs Microservice

| Architecture | Cache Type | TTL Recommendation |
|-------------|-----------|-------------------|
| Monolith | LOCAL (Caffeine) | Longer TTL, no consistency concerns |
| Microservices | REDIS | Shorter TTL (5m ~ 10m), ensure data consistency across instances |

### 4. Spring Cache Annotations

Use `@Cacheable` for read operations, `@CacheEvict` for delete operations, and `@CachePut` for update operations:

```java
@Cacheable(value = "product", key = "#productId")
public ProductDto getProduct(Long productId) { ... }

@CacheEvict(value = "product", key = "#productId")
public void deleteProduct(Long productId) { ... }

@CachePut(value = "product", key = "#result.id")
public ProductDto updateProduct(ProductDto dto) { ... }
```

## Notes

1. **Redis Dependency**: `spring-boot-starter-data-redis` is declared as `provided` scope in `spring-whale-core`.
   Monolithic projects do not need to add it; microservice projects should add it explicitly.

2. **Key Serialization**: In Redis mode, values are serialized to JSON using Jackson. Ensure your cached objects are
   Jackson-serializable.

3. **Cache Namespace**: Each `getCache(name)` call creates an isolated namespace. Keys in different namespaces do not
   conflict.

4. **Thread Safety**: All cache operations are thread-safe. Caffeine uses ConcurrentHashMap internally; Redis uses
   Lettuce connection pooling.

5. **Spring Boot 4 Compatibility**: This module is designed and tested for Spring Boot 4.x. The `CacheAutoConfiguration`
   class has been removed in Spring Boot 4, so `WhaleCacheAutoConfiguration` provides its own adapter.