# spring-whale-core

spring-whale 核心框架，是所有 spring-whale 模块的基石。

---

## 模块说明

```
spring-whale-core
├── context/              认证上下文
├── enums/                基础枚举接口
├── exception/            业务异常
├── json/                 JSON 序列化/反序列化
├── model/                模型
└── utils/                工具类 
```

---

## 核心特性

| 特性             | 类                                          | 说明                                                        |
|----------------|--------------------------------------------|-----------------------------------------------------------|
| **JSON 序列化** ⭐ | `json/` 包                                  | 时间多格式、枚举国际化、BigDecimal 精度、数值溢出保护 → [详细文档](doc/json.zn.md) |
| **认证上下文**      | `AuthenticationContextHolder` / `AuthUtil` | ThreadLocal 存储当前用户身份（userId、username、tenantId）            |
| **统一返回结果**     | `ApiResult<T>`                             | 封装 code + message + data，提供 `success()` / `error()` 工厂方法  |
| **业务异常**       | `BusinessException`                        | 支持 errorCode、i18n（messageCode）、模块分类（module）、扩展数据（data）    |
| **基础枚举**       | `BaseEnum`                                 | 定义 `getId()` / `getDesc()` 契约，配合 JSON 模块自动序列化与国际化         |
| **Spring 上下文** | `SpringContextUtils`                       | 在非 Spring 管理的类中获取 Bean                                    |
| **Edge TTS**   | `EdgeTtsUtil`                              | 调用 edge-tts 命令行工具生成语音，支持并发与超时配置                           |
| **日期时间格式**     | `DateTimeFormats`                          | Date / LocalDate / LocalTime 的 40+ 种常用格式常量                |
| **异常工具**       | `ExceptionUtil`                            | 将异常堆栈转换为字符串                                               |

---

## Quick Start

### Maven 依赖

```xml

<dependency>
    <groupId>io.github.julianzhucode</groupId>
    <artifactId>spring-whale-core</artifactId>
</dependency>
```
