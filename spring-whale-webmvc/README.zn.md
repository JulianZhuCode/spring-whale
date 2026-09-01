# spring-whale-webmvc

Spring Whale Web MVC 基础设施模块，基于 Spring Boot 自动配置，为 RESTful 服务提供统一响应封装、JWT 认证、全局异常处理、国际化等能力。

---

## 模块说明

```
spring-whale-webmvc
├── advice/                             响应体自动包装
├── autoconfigure/                      自动配置
├── exception/                          全局异常处理
└── security/                           JWT 认证
```

---

## 核心特性

| 特性                   | 类                                                       | 说明                                                                                             |
|----------------------|---------------------------------------------------------|------------------------------------------------------------------------------------------------|
| **响应体自动包装**          | `SpringWhaleWebMvcResponseBodyAdvice`                   | Controller 返回值自动包装为 `ApiResult`，支持 `@AdviceIgnore` 跳过 → [详细文档](doc/response-body-advice.zn.md) |
| **JWT 认证**           | `SecurityAutoConfiguration` / `JwtAuthenticationFilter` | 无状态认证，Header/Cookie 双通道 Token 提取，SPI 扩展，Feign 透传 → [详细文档](doc/security.zn.md)                  |
| **全局异常处理**           | `SpringWhaleWebMvcExceptionHandler`                     | 统一异常映射为 `ApiResult`，支持 i18n 错误消息 → [详细文档](doc/exception.zn.md)                                 |
| **国际化**              | `SpringWhaleI18nAutoConfiguration`                      | Cookie 持久化语言偏好，`?lang=zh_CN` 切换，默认关闭                                                           |
| **MessageSource 聚合** | `SpringWhaleMessageSourceAutoConfiguration`             | 合并 classpath 下所有 `messages-*.properties`，解决 ResourceBundle 只加载第一个的问题                           |

---

## Quick Start

### Maven 依赖

```xml

<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-webmvc</artifactId>
</dependency>
```

### 启用国际化（可选）

```yaml
spring:
  whale:
    i18n:
      enabled: true
```

### 配置 JWT 密钥（生产环境）

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

## 配置参考

| 配置项                                             | 默认值        | 说明             |
|-------------------------------------------------|------------|----------------|
| `spring.whale.web-mvc.security.jwt-secret`      | 内置默认值      | 生产环境务必修改       |
| `spring.whale.web-mvc.security.jwt-expiration`  | `86400000` | Token 过期时间（ms） |
| `spring.whale.web-mvc.security.permit-all-urls` | `[]`       | 免认证 URL        |
| `spring.whale.web-mvc.exception.enable-i18n`    | `false`    | 异常消息国际化        |
| `spring.whale.i18n.enabled`                     | `false`    | 启用 i18n 自动配置   |