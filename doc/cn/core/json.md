# Spring Whale JSON 序列化

Spring Whale 框架提供了强大的 JSON 序列化和反序列化功能，基于 Spring Boot 和 Tools Jackson 实现，支持自定义时间格式、枚举处理和国际化等功能。

## 目录

- [功能特性](#功能特性)
- [配置说明](#配置说明)
- [时间类型处理](#时间类型处理)
- [枚举类型处理](#枚举类型处理)
- [使用示例](#使用示例)
- [最佳实践](#最佳实践)

## 功能特性

### 核心特性

- ✅ **多时间格式支持** - 支持 Date、LocalDate、LocalTime、LocalDateTime 的序列化和反序列化
- ✅ **灵活的时间格式配置** - 支持自定义时间格式或时间戳模式
- ✅ **多格式反序列化** - 自动识别多种国际日期时间格式
- ✅ **枚举序列化** - 支持 BaseEnum 接口的枚举类型序列化
- ✅ **国际化支持** - 枚举描述支持多语言国际化
- ✅ **线程安全** - 所有工具类和方法均为线程安全设计

### 支持的时间格式

#### Date/LocalDateTime 支持格式（16+ 种）

- **ISO 格式**: `yyyy-MM-dd'T'HH:mm:ss`
- **中国格式**: `yyyy-MM-dd HH:mm:ss`, `yyyy/MM/dd HH:mm:ss`, `yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒`
- **欧洲格式**: `dd-MM-yyyy HH:mm:ss`, `dd/MM/yyyy HH:mm:ss`, `dd.MM.yyyy HH:mm:ss`
- **美国格式**: `MM-dd-yyyy HH:mm:ss`, `MM/dd/yyyy HH:mm:ss`, `MM.dd.yyyy HH:mm:ss`
- **简短格式**: `yyyy-MM-dd HH:mm`, `yyyy/MM/dd HH:mm`
- **时区格式**: `yyyy-MM-dd HH:mm:ss Z`, `yyyy-MM-dd HH:mm:ssXXX`
- **无秒格式**: `yyyy-MM-dd'T'HH:mm`, `yyyy-MM-dd'T'HH:mm:ss`

#### LocalDate 支持格式（13+ 种）

- **ISO 格式**: `yyyy-MM-dd`
- **中国格式**: `yyyy-MM-dd`, `yyyy/MM/dd`, `yyyy 年 MM 月 dd 日`
- **欧洲格式**: `dd-MM-yyyy`, `dd/MM/yyyy`, `dd.MM.yyyy`
- **美国格式**: `MM-dd-yyyy`, `MM/dd/yyyy`, `MM.dd.yyyy`
- **简短格式**: `yy-MM-dd`, `yy/MM/dd`
- **月份名称**: `dd MMMM yyyy`, `dd MMM yyyy`, `MMMM dd, yyyy`, `MMM dd, yyyy`

#### LocalTime 支持格式（10+ 种）

- **ISO 格式**: `HH:mm:ss`
- **标准格式**: `HH:mm:ss`, `HH:mm:ss.SSS`, `HH:mm:ss.SSSSSS`, `HH:mm:ss.SSSSSSSSS`
- **无秒格式**: `HH:mm`, `H:mm`, `hh:mm a`, `hh:mm:ss a`
- **时区格式**: `HH:mm:ss Z`, `HH:mm:ssXXX`

## 配置说明

### 配置文件

在 `application.yml` 中添加以下配置：

```yaml
spring:
  whale:
    json:
      # 是否启用国际化（默认：false）
      use-i18n: true
      # 找不到国际化 key 时是否回退到默认描述（默认：false）
      fallback-to-default-desc: true
      # 日期时间格式（默认：yyyy-MM-dd HH:mm:ss，或设置为 timestamp 使用时间戳）
      date-time-format: yyyy-MM-dd HH:mm:ss
      # 日期格式（默认：yyyy-MM-dd）
      date-format: yyyy-MM-dd
      # 时间格式（默认：HH:mm:ss）
      time-format: HH:mm:ss
```

### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `use-i18n` | boolean | false | 是否启用枚举描述的国际化 |
| `fallback-to-default-desc` | boolean | false | 当找不到国际化 key 时，是否回退到枚举的默认描述 |
| `date-time-format` | String | yyyy-MM-dd HH:mm:ss | Date 和 LocalDateTime 的格式化模式，设置为 `timestamp` 使用时间戳 |
| `date-format` | String | yyyy-MM-dd | LocalDate 的格式化模式 |
| `time-format` | String | HH:mm:ss | LocalTime 的格式化模式 |

## 时间类型处理

### 序列化

#### 1. 默认格式序列化

```java
public class TimeRecord {
    private Date date;
    private LocalDateTime localDateTime;
    private LocalDate localDate;
    private LocalTime localTime;
}
```

配置：
```yaml
spring:
  whale:
    json:
      date-time-format: yyyy-MM-dd HH:mm:ss
      date-format: yyyy-MM-dd
      time-format: HH:mm:ss
```

输出：
```json
{
  "date": "2024-03-25 14:30:45",
  "localDateTime": "2024-03-25 14:30:45",
  "localDate": "2024-03-25",
  "localTime": "14:30:45"
}
```

#### 2. 时间戳序列化

配置：
```yaml
spring:
  whale:
    json:
      date-time-format: timestamp
```

输出：
```json
{
  "date": 1711353045000,
  "localDateTime": 1711353045000
}
```

#### 3. 自定义格式序列化

配置：
```yaml
spring:
  whale:
    json:
      date-time-format: dd/MM/yyyy HH:mm
      date-format: dd/MM/yyyy
      time-format: HH:mm
```

输出：
```json
{
  "date": "25/03/2024 14:30",
  "localDateTime": "25/03/2024 14:30",
  "localDate": "25/03/2024",
  "localTime": "14:30"
}
```

### 反序列化

#### 1. 时间戳反序列化

```json
// 数字时间戳
{"date": 1711353045000}

// 字符串时间戳
{"date": "1711353045000"}
```

#### 2. 字符串格式反序列化

支持自动识别多种格式：

```json
// ISO 格式
{"date": "2024-03-25T14:30:45"}
{"localDate": "2024-03-25"}
{"localTime": "14:30:45"}

// 中国格式
{"date": "2024/03/25 14:30:45"}
{"localDate": "2024/03/25"}

// 欧洲格式
{"date": "25-03-2024 14:30:45"}
{"localDate": "25-03-2024"}

// 美国格式
{"localDate": "03/25/2024"}

// 无秒格式
{"localTime": "14:30"}
{"localDateTime": "2024-03-25 14:30"}

// 带毫秒格式
{"localTime": "14:30:45.123"}
```

反序列化逻辑会自动尝试所有支持的格式，直到找到匹配的格式为止。

## 枚举类型处理

### 定义枚举

实现 `BaseEnum` 接口：

```java
@AllArgsConstructor
public enum StatusEnum implements BaseEnum {
    ACTIVE("ACTIVE", "Active"),
    INACTIVE("INACTIVE", "Inactive"),
    PENDING("PENDING", "Pending"),
    DELETED("DELETED", "Deleted");

    @Getter
    private final String id;
    @Getter
    private final String desc;
}
```

### 序列化

#### 1. 不使用国际化

配置：
```yaml
spring:
  whale:
    json:
      use-i18n: false
```

输出：
```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

#### 2. 使用国际化

配置：
```yaml
spring:
  whale:
    json:
      use-i18n: true
      fallback-to-default-desc: true
```

创建国际化资源文件 `messages_zh_CN.properties`：
```properties
ACTIVE=活跃
INACTIVE=非活跃
PENDING=待处理
DELETED=已删除
```

输出（中文环境）：
```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "活跃"
  }
}
```

输出（英文环境，无翻译时回退）：
```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

### 反序列化

支持三种反序列化方式：

#### 1. 对象格式

```json
{"status": {"id": "ACTIVE", "desc": "Active"}}
```

#### 2. 字符串格式（使用 id）

```json
{"status": "ACTIVE"}
```

#### 3. 整数格式（使用 ordinal）

```json
{"status": 0}
```

### 错误处理

#### 1. 无效的 ID

```json
{"status": "INVALID_ID"}
```

抛出异常：
```
IllegalArgumentException: No enum constant with id: INVALID_ID in class: com.example.StatusEnum
```

#### 2. 无效的序号

```json
{"status": -1}
{"status": 999}
```

抛出异常：
```
ArrayIndexOutOfBoundsException / IllegalArgumentException
```

#### 3. 缺少国际化 key 且禁用回退

配置：
```yaml
spring:
  whale:
    json:
      use-i18n: true
      fallback-to-default-desc: false
```

如果找不到对应的国际化 key，将抛出 `NoSuchMessageException`。

## 使用示例

### 完整示例

#### 1. 实体类定义

```java
import io.github.springwhale.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private StatusEnum status;
    private Date createTime;
    private LocalDate birthday;
    private LocalDateTime lastLoginTime;
}

@AllArgsConstructor
@Getter
public enum StatusEnum implements BaseEnum {
    ACTIVE("ACTIVE", "Active"),
    INACTIVE("INACTIVE", "Inactive"),
    PENDING("PENDING", "Pending");

    private final String id;
    private final String desc;
}
```

#### 2. Controller 使用

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        // 接收的 JSON 会自动反序列化为 UserDTO
        // 时间字段支持多种格式
        // 枚举字段支持对象、字符串、整数格式
        return ResponseEntity.ok(userDTO);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO userDTO = userService.getUser(id);
        // 返回的对象会自动序列化为 JSON
        // 时间字段按配置的格式输出
        // 枚举字段包含 id 和 desc
        return ResponseEntity.ok(userDTO);
    }
}
```

#### 3. 前端请求示例

```javascript
// POST /users
{
  "username": "张三",
  "status": "ACTIVE",  // 或 {"id": "ACTIVE", "desc": "Active"} 或 0
  "createTime": "2024-03-25T14:30:45",  // 或 1711353045000
  "birthday": "2000-01-01",  // 或 946684800000
  "lastLoginTime": "2024-03-25 14:30:45"  // 或 1711353045000
}
```

#### 4. 响应示例

```json
{
  "id": 1,
  "username": "张三",
  "status": {
    "id": "ACTIVE",
    "desc": "活跃"
  },
  "createTime": "2024-03-25 14:30:45",
  "birthday": "2000-01-01",
  "lastLoginTime": "2024-03-25 14:30:45"
}
```

### 国际化配置示例

#### 1. 创建国际化资源文件

`resources/messages.properties` (默认):
```properties
ACTIVE=Active
INACTIVE=Inactive
PENDING=Pending
DELETED=Deleted
```

`resources/messages_zh_CN.properties` (中文):
```properties
ACTIVE=活跃
INACTIVE=非活跃
PENDING=待处理
DELETED=已删除
```

`resources/messages_ja_JP.properties` (日文):
```properties
ACTIVE=アクティブ
INACTIVE=非アクティブ
PENDING=ペンディング
DELETED=削除済み
```

#### 2. 动态切换语言

```java
// 在请求中设置语言环境
LocaleContextHolder.setLocale(Locale.JAPAN);

// 或者通过拦截器根据请求头设置
@GetMapping(value = "/users/{id}", headers = "Accept-Language=ja-JP")
public ResponseEntity<UserDTO> getUserJapanese(@PathVariable Long id) {
    LocaleContextHolder.setLocale(Locale.JAPAN);
    UserDTO userDTO = userService.getUser(id);
    return ResponseEntity.ok(userDTO);
}
```

## 最佳实践

### 1. 时间格式选择

- **内部系统**: 建议使用时间戳 (`timestamp`)，性能好，无时区问题
- **对外 API**: 建议使用 ISO 格式 (`yyyy-MM-dd HH:mm:ss`)，可读性好
- **移动端**: 建议使用时间戳，减少解析开销
- **国际化系统**: 建议统一使用 UTC 时间戳，前端转换为本地时间

### 2. 枚举使用规范

- 所有业务枚举都实现 `BaseEnum` 接口
- `id` 字段使用有意义的字符串（如 `"ACTIVE"`），避免使用纯数字
- 提供完整的国际化资源文件
- 生产环境建议开启 `fallback-to-default-desc: true`

### 3. 性能优化

- 时间戳格式比字符串格式序列化性能更好
- 关闭不必要的国际化支持（`use-i18n: false`）可提升性能
- 对于高频接口，考虑使用 DTO 专门优化 JSON 结构

### 4. 兼容性考虑

- API 升级时，旧字段做好兼容处理
- 时间格式变更时，确保反序列化支持新旧两种格式
- 枚举值废弃时，保留一段时间内不要立即删除

### 5. 测试建议

```java
@SpringBootTest
public class JsonSerializationTest {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    public void testUserDTOSerialization() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("test");
        user.setStatus(StatusEnum.ACTIVE);
        user.setCreateTime(new Date());
        
        String json = objectMapper.writeValueAsString(user);
        assertNotNull(json);
        assertTrue(json.contains("\"status\""));
    }
    
    @Test
    public void testUserDTODeserialization() throws Exception {
        String json = "{\"username\":\"test\",\"status\":\"ACTIVE\"}";
        UserDTO user = objectMapper.readValue(json, UserDTO.class);
        assertNotNull(user);
        assertEquals(StatusEnum.ACTIVE, user.getStatus());
    }
}
```

## 注意事项

### 1. 线程安全

- `DateTimeFormats` 工具类是线程安全的
- 所有 `DateTimeFormatter` 都是不可变对象
- 可以在多线程环境下放心使用

### 2. 时区处理

- 默认使用系统时区 (`ZoneId.systemDefault()`)
- 分布式系统建议统一使用 UTC 时区
- 前端展示时再转换为本地时间

### 3. 精度问题

- 时间戳精度为毫秒
- `LocalTime` 支持纳秒精度（`HH:mm:ss.SSSSSSSSS`）
- 注意数据库存储时的精度匹配

### 4. 空值处理

- `null` 值会正常序列化为 JSON `null`
- 反序列化时不支持 `null` 字符串，需要特殊处理可在 DTO 中使用 `@JsonInclude`

## 相关资源

- [Jackson 官方文档](https://github.com/FasterXML/jackson-docs)
- [Spring Boot 参考指南](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Java Time API 文档](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
