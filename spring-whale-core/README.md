# spring-whale-core

Core framework of spring-whale, the foundation of all spring-whale modules.

---

## Module Structure

```
spring-whale-core
├── context/              Authentication Context
├── enums/                Base Enum Interface
├── exception/            Business Exception
├── json/                 JSON Serialization/Deserialization
├── model/                Models
└── utils/                Utilities
```

---

## Core Features

| Feature                | Class                                       | Description                                                                          |
|------------------------|---------------------------------------------|--------------------------------------------------------------------------------------|
| **JSON Serialization** ⭐ | `json/` package                             | Multi-format time, enum i18n, BigDecimal precision, numeric overflow protection → [Details](doc/json.md) |
| **Authentication Context** | `AuthenticationContextHolder` / `AuthUtil`  | ThreadLocal-based storage for current user identity (userId, username, tenantId)     |
| **Unified API Result** | `ApiResult<T>`                              | Encapsulates code + message + data, with `success()` / `error()` factory methods     |
| **Business Exception** | `BusinessException`                         | Supports errorCode, i18n (messageCode), module classification, extended data         |
| **Base Enum**          | `BaseEnum`                                  | Defines `getId()` / `getDesc()` contract, integrates with JSON module for i18n       |
| **Spring Context**     | `SpringContextUtils`                        | Obtain beans in non-Spring-managed classes                                           |
| **Edge TTS**           | `EdgeTtsUtil`                               | Invokes edge-tts CLI for speech synthesis, with concurrency and timeout support      |
| **Date Time Formats**  | `DateTimeFormats`                           | 40+ common format patterns for Date / LocalDate / LocalTime                          |
| **Exception Utility**  | `ExceptionUtil`                             | Converts exception stack traces to strings                                           |

---

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-core</artifactId>
</dependency>
```