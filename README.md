<div align="center">

# Spring Whale 🐋

**A universal enterprise-grade development framework built for Spring Boot**, designed for rapid, standardized, and extensible enterprise application development.

![JDK](https://img.shields.io/badge/JDK-25-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.x-brightgreen)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

English · [中文](README.zn.md) · [日本語](README.ja.md)

</div>

---

Anyone who has built enterprise systems knows it: the time-consuming part is rarely the
business logic — it's the "foundation" you rewrite in every project: API response format,
exception handling, authentication, data-scope filtering, what happens when an event is
published but nobody consumes it. None of this code is hard to write, but missing one spot
means a production incident.

Spring Whale takes care of these common, error-prone concerns up front, delivered entirely
as **jar dependencies** — add them and they work, via Spring Boot auto-configuration. They
cover the common cross-cutting aspects of enterprise development: web, database, caching,
events, admin console, and more — see the module navigation below.

The framework also ships with a platform module providing a set of simple, out-of-the-box
business features. Small and medium-sized projects can use them directly, or treat them as
a reference for writing their own business modules.

## Module Navigation

**Core framework** (continuously evolved, with API stability commitment):

| Module | Responsibility | Docs |
|------|------|------|
| spring-whale-core | Cache abstraction (Caffeine / Redis), JSON serialization (enum i18n, date-time, BigDecimal), BusinessException, ApiResult | [Docs](spring-whale-core/README.md) |
| spring-whale-webmvc | JWT authentication (Header/Cookie + Feign propagation), global exception handling, response body wrapping, i18n, Security SPI | [Docs](spring-whale-webmvc/README.md) |
| spring-whale-database | JpaQueryWrapper fluent queries, six-level data scope, SQL-level multi-tenancy, resilient Flyway migration, BaseEntity | [Docs](spring-whale-database/README.md) |
| spring-whale-event | Unified Local / Kafka / RabbitMQ event API, durable failure retry, event versioning, Metrics SPI | [Docs](spring-whale-event/README.md) |
| spring-whale-thymeleaf | Pluggable admin console, menu SPI, permission-aware UI, declarative CRUD components | [Docs](spring-whale-thymeleaf/README.md) |

**Platform reference implementations** (simple business features out of the box, also demonstrating how framework capabilities come together):

| Module | Responsibility | Docs |
|------|------|------|
| spring-whale-platform-rbac | User / role / menu / department RBAC integrated with data scope, including Thymeleaf admin pages | [Docs](spring-whale-platform/rbac/README.md) |
| spring-whale-platform-task | Batch task engine: progress tracking, breakpoint resume, failure retry, virtual-thread concurrency, including admin pages | [Docs](spring-whale-platform/task/README.md) |

## Quick Start

**Option 1: Inherit the parent project** (recommended — versions managed centrally,
dependencies need no version tags; the parent already imports the Spring Boot / Spring Cloud BOMs):

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

**Option 2: Add dependencies directly** (manage versions yourself when not inheriting the parent):

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

All modules are activated through Spring Boot auto-configuration and start with zero
configuration by default. DDL scripts live under `src/main/resources/db/migration/` in
each module — with Flyway they migrate automatically; otherwise execute them manually in
order. See each module's documentation for detailed configuration.

## Tech Stack

| Category | Technologies |
|------|------|
| Language / Framework | Java 25 · Spring Boot 4.1.x · Spring Cloud 2025.1.x |
| Persistence | Spring Data JPA (Hibernate) · Flyway · Druid · PostgreSQL |
| Cache | Caffeine (local) · Redis (distributed) |
| Messaging | Spring Events · Kafka · RabbitMQ |
| Security | Spring Security · JJWT |
| Admin UI | Thymeleaf · Bootstrap 5 |

## Roadmap

| Version | Content |
|------|------|
| **1.0.0** ✅ | Stable APIs for the five core modules; RBAC / batch task reference implementations |
| 1.1 | WebSocket support |
| 1.2 | Workflow orchestration (orchestrating business steps / tasks / events) |

## Showcase

- [jp-cn-dict](https://github.com/JulianZhuCode/jp-cn-dict) — a Japanese–Chinese dictionary application built on Spring Whale (vocabulary / grammar / example management, Edge TTS pronunciation).

## License

[Apache License 2.0](LICENSE)

Spring Whale is a community-driven open-source project and is not an official product of
the Spring Framework or VMware.
