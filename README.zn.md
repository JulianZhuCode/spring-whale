<div align="center">

# Spring Whale 🐋

**一款专为 Spring Boot 打造的通用企业级开发框架**，旨在实现快速、标准化、可扩展的企业级应用开发。

![JDK](https://img.shields.io/badge/JDK-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.x-brightgreen)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

**English** · [中文](README.zn.md) · [日本語](README.ja.md)

</div>

---

做过企业系统的人都有体会：真正耗时间的不是业务逻辑，而是那些每个项目都要重写一遍的"地基"——接口返回格式、异常处理、权限校验、数据权限过滤、消息发出去没人消费怎么办。这些代码写起来不难，但漏一处就是线上事故。

Spring Whale 把这些通用又易错的事提前做好了，全部以 **jar 包依赖**的方式交付，引入即用、自动装配，能力覆盖 Web、数据库、缓存、事件、管理后台等企业开发的常见切面，具体见下方模块导航。

此外，框架还附带 platform 模块，提供了一组开箱即用的简单业务功能：中小项目可以直接使用，也可以作为业务模块的开发参考。

## 模块导航

**核心框架**（持续演进，API 稳定性承诺）：

| 模块 | 职责 | 文档 |
|------|------|------|
| spring-whale-core | 缓存抽象（Caffeine / Redis）、JSON 序列化（枚举 i18n、时间、BigDecimal）、BusinessException、ApiResult | [文档](spring-whale-core/README.zn.md) |
| spring-whale-webmvc | JWT 认证（Header/Cookie 双通道 + Feign 透传）、全局异常、响应体自动包装、i18n、Security SPI | [文档](spring-whale-webmvc/README.zn.md) |
| spring-whale-database | JpaQueryWrapper 链式查询、六级数据权限、SQL 级多租户、Flyway 容错迁移、BaseEntity | [文档](spring-whale-database/README.zn.md) |
| spring-whale-event | Local / Kafka / RabbitMQ 统一事件 API、失败持久化重试、事件版本化、Metrics SPI | [文档](spring-whale-event/README.zn.md) |
| spring-whale-thymeleaf | 可插拔管理后台、菜单 SPI、权限感知 UI、声明式 CRUD 组件 | [文档](spring-whale-thymeleaf/README.zn.md) |

**平台参考实现**（开箱即用的简单业务功能，也演示框架能力如何落地）：

| 模块 | 职责 | 文档 |
|------|------|------|
| spring-whale-platform-rbac | 用户 / 角色 / 菜单 / 部门 RBAC，与数据权限联动，含 Thymeleaf 后台页面 | [文档](spring-whale-platform/rbac/README.zn.md) |
| spring-whale-platform-task | 批量任务引擎：进度跟踪、断点续跑、失败重试、虚拟线程并发，含后台页面 | [文档](spring-whale-platform/task/README.zn.md) |

## 快速开始

**方式一：继承父工程**（推荐，版本统一管理，依赖无需写版本号；父工程已包含
Spring Boot / Spring Cloud BOM）：

```xml
<parent>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale</artifactId>
    <version>1.0.0</version>
</parent>

<dependencies>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-webmvc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-database</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-event-core</artifactId>
    </dependency>
</dependencies>
```

**方式二：直接引入依赖**（不继承父工程时，自行管理版本号）：

```xml
<properties>
    <spring-whale.version>1.0.0</spring-whale.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-webmvc</artifactId>
        <version>${spring-whale.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-database</artifactId>
        <version>${spring-whale.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.julianzhucode</groupId>
        <artifactId>spring-whale-event-core</artifactId>
        <version>${spring-whale.version}</version>
    </dependency>
</dependencies>
```

所有模块基于 Spring Boot 自动装配，默认零配置即可启动；建表 SQL 位于各模块的 `src/main/resources/db/migration/` 目录，使用 Flyway 时自动迁移，未使用时按序手动执行即可。各模块详细配置见模块文档。

## 技术栈

| 分类 | 技术 |
|------|------|
| 语言 / 框架 | Java 25 · Spring Boot 4.1.x · Spring Cloud 2025.1.x |
| 持久层 | Spring Data JPA (Hibernate) · Flyway · Druid · PostgreSQL |
| 缓存 | Caffeine（本地）· Redis（分布式） |
| 消息 | Spring Events · Kafka · RabbitMQ |
| 认证 | Spring Security · JJWT |
| 管理端 | Thymeleaf · Bootstrap 5 |

## Roadmap

| 版本 | 内容 |
|------|------|
| **1.0.0** ✅ | 五大核心模块 API 稳定；RBAC / 批量任务参考实现 |
| 1.1 | WebSocket 支持 |
| 1.2 | 流程编排（业务步骤 / 任务 / 事件的工作流编排） |

## 实战案例

- [jp-cn-dict](https://github.com/JulianZhuCode/jp-cn-dict) — 基于 Spring Whale 构建的日汉词典应用（词汇 / 语法 / 例句管理、Edge TTS 发音）。

## License

[Apache License 2.0](LICENSE)

Spring Whale 是社区驱动的开源项目，并非 Spring Framework 或 VMware 的官方产品。
