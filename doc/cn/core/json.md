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
- ✅ **BigDecimal 全局定制** - 支持 BigDecimal 的精度控制和字符串序列化
- ✅ **数值溢出保护** - Long 和 Integer 反序列化时自动检查溢出
- ✅ **浮点数精度控制** - Double 和 Float 自动保留指定精度
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
      # ==================== BigDecimal 配置 ====================
      # 是否启用 BigDecimal 全局序列化定制（默认：true）
      big-decimal-enabled: true
      # BigDecimal 保留小数位数（默认：2）
      big-decimal-scale: 2
      # BigDecimal 舍入模式（默认：HALF_UP）
      big-decimal-rounding-mode: HALF_UP
      # 是否序列化为字符串防止前端精度丢失（默认：true）
      big-decimal-as-string: true
      # Float/Double 保留小数位数（默认：8）
      float-precision: 8
```

### 配置项说明

| 配置项                         | 类型           | 默认值                 | 说明                                                |
|-----------------------------|--------------|---------------------|---------------------------------------------------|
| `use-i18n`                  | boolean      | false               | 是否启用枚举描述的国际化                                      |
| `fallback-to-default-desc`  | boolean      | false               | 当找不到国际化 key 时，是否回退到枚举的默认描述                        |
| `date-time-format`          | String       | yyyy-MM-dd HH:mm:ss | Date 和 LocalDateTime 的格式化模式，设置为 `timestamp` 使用时间戳 |
| `date-format`               | String       | yyyy-MM-dd          | LocalDate 的格式化模式                                  |
| `time-format`               | String       | HH:mm:ss            | LocalTime 的格式化模式                                  |
| `big-decimal-enabled`       | boolean      | true                | 是否启用 BigDecimal 全局序列化定制                           |
| `big-decimal-scale`         | int          | 2                   | BigDecimal 的保留小数位数                                |
| `big-decimal-rounding-mode` | RoundingMode | HALF_UP             | BigDecimal 的舍入模式                                  |
| `big-decimal-as-string`     | boolean      | true                | 是否将 BigDecimal 序列化为字符串（防止前端精度丢失）                  |
| `float-precision`           | int          | 8                   | Float 和 Double 的保留小数位数                            |

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
{
  "date": 1711353045000
}

// 字符串时间戳
{
  "date": "1711353045000"
}
```

#### 2. 字符串格式反序列化

支持自动识别多种格式：

```json
// ISO 格式
{
  "date": "2024-03-25T14:30:45"
}
{
  "localDate": "2024-03-25"
}
{
  "localTime": "14:30:45"
}

// 中国格式
{
  "date": "2024/03/25 14:30:45"
}
{
  "localDate": "2024/03/25"
}

// 欧洲格式
{
  "date": "25-03-2024 14:30:45"
}
{
  "localDate": "25-03-2024"
}

// 美国格式
{
  "localDate": "03/25/2024"
}

// 无秒格式
{
  "localTime": "14:30"
}
{
  "localDateTime": "2024-03-25 14:30"
}

// 带毫秒格式
{
  "localTime": "14:30:45.123"
}
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
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

#### 2. 字符串格式（使用 id）

```json
{
  "status": "ACTIVE"
}
```

#### 3. 整数格式（使用 ordinal）

```json
{
  "status": 0
}
```

### 错误处理

#### 1. 无效的 ID

```json
{
  "status": "INVALID_ID"
}
```

抛出异常：

```
IllegalArgumentException: No enum constant with id: INVALID_ID in class: com.example.StatusEnum
```

#### 2. 无效的序号

```json
{
  "status": -1
}
{
  "status": 999
}
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

## BigDecimal 处理

### 序列化

#### 1. 默认配置序列化

配置：

```yaml
spring:
  whale:
    json:
      big-decimal-enabled: true
      big-decimal-scale: 2
      big-decimal-rounding-mode: HALF_UP
      big-decimal-as-string: true
```

代码：

```java
public class PriceDTO {
    private BigDecimal price;
}
```

输出：

```json
{
  "price": "99.99"
}
```

#### 5. Double/Float 精度控制

配置：

```yaml
spring:
  whale:
    json:
      float-precision: 4
```

代码：

```java
public class MeasurementDTO {
    private Double temperature;
    private Float humidity;
}
```

输出：

```json
{
  "temperature": 36.5678,
  "humidity": 65.1234
}
```

**注意**：Float 和 Double 会根据配置的精度自动四舍五入，避免浮点数精度问题。

## 数值类型处理

### Long/Integer 溢出保护

反序列化 Long 和 Integer 时，如果输入的数值超出类型范围，会抛出异常。

#### 1. Long 溢出保护

```json
// 正常值
{
  "value": 1234567890123456789
}

// 超出 Long.MAX_VALUE，抛出异常
{
  "value": 1234567890123456789000000
}

// 超出 Long.MIN_VALUE，抛出异常
{
  "value": -1234567890123456789000000
}
```

异常信息：

```
DatabindException: Long value overflow: 1234567890123456789000000
```

#### 2. Integer 溢出保护

```json
// 正常值
{
  "value": 2147483647
}

// 超出 Integer.MAX_VALUE，抛出异常
{
  "value": 3000000000
}

// 超出 Integer.MIN_VALUE，抛出异常
{
  "value": -3000000000
}
```

异常信息：

```
DatabindException: Integer value overflow: 3000000000
```

### Double/Float 精度控制

#### 1. Double 序列化

配置：

```yaml
spring:
  whale:
    json:
      float-precision: 6
```

代码：

```java
Double value = 123.456789012;
```

输出：

```json
{
  "value": 123.456789
}
```

#### 2. Float 序列化

配置：

```yaml
spring:
  whale:
    json:
      float-precision: 4
```

代码：

```java
Float value = 0.123456789f;
```

输出：

```json
{
  "value": 0.1235
}
```

**说明**：

- 精度控制使用 `BigDecimal` 进行精确计算
- 舍入模式使用配置的 `big-decimal-rounding-mode`（默认 HALF_UP）
- 会自动去除末尾的零（stripTrailingZeros）

## 使用示例

### 完整示例

#### 1. 实体类定义

#### 2. 不同精度配置

配置：

```yaml
spring:
  whale:
    json:
      big-decimal-scale: 4
      big-decimal-rounding-mode: HALF_DOWN
```

代码：

```java
BigDecimal value = new BigDecimal("123.456789");
```

输出：

```json
{
  "value": "123.4568"
}
```

#### 3. 数字格式序列化

配置：

```yaml
spring:
  whale:
    json:
      big-decimal-as-string: false
```

输出：

```json
{
  "price": 99.99,
  "discount": 0.15
}
```

**注意**：使用数字格式可能导致前端 JavaScript 精度丢失，建议使用字符串格式。

#### 4. 禁用全局定制

配置：

```yaml
spring:
  whale:
    json:
      big-decimal-enabled: false
```

输出（保持原始精度）：

```json
{
  "price": 99.99123456789
}
```

### 反序列化

支持三种反序列化方式：

#### 1. 字符串格式（推荐）

```json
{
  "price": "99.99"
}
```

#### 2. 数字格式

```json
{
  "price": 99.99
}
```

#### 3. 科学计数法

```json
{
  "price": "1.23E+2"
}
```

### 舍入模式说明

支持的舍入模式（`RoundingMode` 枚举）：

| 舍入模式          | 说明          | 示例 (2.5) | 示例 (2.6) |
|---------------|-------------|----------|----------|
| `HALF_UP`     | 四舍五入        | 3        | 3        |
| `HALF_DOWN`   | 五舍六入        | 2        | 3        |
| `HALF_EVEN`   | 银行家舍入法      | 2        | 3        |
| `UP`          | 远离零方向舍入     | 3        | 3        |
| `DOWN`        | 向零方向舍入      | 2        | 2        |
| `CEILING`     | 向正无穷方向舍入    | 3        | 3        |
| `FLOOR`       | 向负无穷方向舍入    | 2        | 2        |
| `UNNECESSARY` | 不需要舍入，否则抛异常 | 异常       | 异常       |

### 最佳实践

#### 1. 金额字段处理

对于金额字段，建议：

- 使用 `big-decimal-as-string: true` 避免前端精度丢失
- 设置合适的精度（通常 2 位小数）
- 使用 `HALF_UP` 舍入模式符合商业习惯

#### 2. 高精度计算

对于需要高精度的科学计算：

- 增加 `big-decimal-scale` 到合适的值（如 10）
- 考虑使用 `HALF_EVEN` 银行家舍入法减少累积误差

#### 3. 数据库对接

确保数据库精度与配置一致：

- MySQL: `DECIMAL(10,2)` 对应 `big-decimal-scale: 2`
- Oracle: `NUMBER(10,4)` 对应 `big-decimal-scale: 4`

## BigDecimal 处理

### 序列化

#### 1. 默认配置序列化

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
    private BigDecimal salary;
    private Double latitude;
    private Float accuracy;
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
  "lastLoginTime": "2024-03-25 14:30:45",  // 或 1711353045000
  "salary": "12345.67",  // 或 12345.67（推荐字符串格式）
  "latitude": 39.9042,  // 自动保留配置的精度
  "accuracy": 10.5  // 自动保留配置的精度
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
  "lastLoginTime": "2024-03-25 14:30:45",
  "salary": "12345.67",
  "latitude": 39.9042,
  "accuracy": 10.5
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

### 5. BigDecimal 处理

- 默认启用全局 BigDecimal 定制，保留 2 位小数，四舍五入
- 建议开启 `big-decimal-as-string: true` 避免前端精度丢失
- 如需更高精度，调整 `big-decimal-scale` 配置
- 禁用全局定制后，使用 Jackson 默认处理方式

### 6. 数值溢出保护

- Long 和 Integer 反序列化时会自动检查是否溢出
- 溢出时会抛出 `DatabindException` 异常
- 建议在接收前端传入的数值型参数时做好异常处理

### 7. 浮点数精度

- Double 和 Float 会根据 `float-precision` 配置自动保留精度
- 精度控制使用 BigDecimal 进行精确计算
- 避免 JavaScript 等语言的浮点数精度问题

## 相关资源

- [Jackson 官方文档](https://github.com/FasterXML/jackson-docs)
- [Spring Boot 参考指南](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Java Time API 文档](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
