# spring-whale-webmvc

Spring Whale Web MVC infrastructure module. Based on Spring Boot auto-configuration, provides unified response wrapping, JWT authentication, global exception handling, i18n, and more for RESTful services.

---

## Module Structure

```
spring-whale-webmvc
├── advice/                             Response body auto-wrapping
├── autoconfigure/                      Auto-configuration
├── exception/                          Global exception handling
└── security/                           JWT authentication
```

---

## Core Features

| Feature | Class | Description |
|---------|-------|-------------|
| **Response Body Wrapping** | `SpringWhaleWebMvcResponseBodyAdvice` | Auto-wraps Controller return values in `ApiResult`, skip with `@AdviceIgnore` → [Details](doc/response-body-advice.md) |
| **JWT Authentication** | `SecurityAutoConfiguration` / `JwtAuthenticationFilter` | Stateless auth, Header/Cookie dual-channel token extraction, SPI extension, Feign propagation → [Details](doc/security.md) |
| **Global Exception Handling** | `SpringWhaleWebMvcExceptionHandler` | Unified exception mapping to `ApiResult`, i18n error messages → [Details](doc/exception.md) |
| **I18n** | `SpringWhaleI18nAutoConfiguration` | Cookie-persisted language preference, `?lang=en` switching, disabled by default |
| **MessageSource Aggregation** | `SpringWhaleMessageSourceAutoConfiguration` | Merges all `messages-*.properties` on classpath, solves ResourceBundle single-load issue |

---

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-webmvc</artifactId>
</dependency>
```

### Enable I18n (Optional)

```yaml
spring:
  whale:
    i18n:
      enabled: true
```

### Configure JWT Secret (Production)

```yaml
spring:
  whale:
    web-mvc:
      security:
        jwt-secret: ${JWT_SECRET}
        permit-all-urls:
          - /public/**
          - /api/login
```

---

## Configuration Reference

| Key | Default | Description |
|-----|---------|-------------|
| `spring.whale.web-mvc.security.jwt-secret` | Built-in default | Must change in production |
| `spring.whale.web-mvc.security.jwt-expiration` | `86400000` | Token expiration (ms) |
| `spring.whale.web-mvc.security.permit-all-urls` | `[]` | URLs bypassing authentication |
| `spring.whale.web-mvc.exception.enable-i18n` | `false` | Exception message i18n |
| `spring.whale.i18n.enabled` | `false` | Enable i18n auto-configuration |