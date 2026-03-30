# Spring Whale JSON Serialization

Spring Whale framework provides powerful JSON serialization and deserialization capabilities, based on Spring Boot and
Tools Jackson, supporting custom time formats, enum handling, and internationalization features.

## Table of Contents

- [Features](#features)
- [Configuration](#configuration)
- [Time Type Handling](#time-type-handling)
- [Enum Type Handling](#enum-type-handling)
- [Usage Examples](#usage-examples)
- [Best Practices](#best-practices)

## Features

### Core Features

- ✅ **Multiple Time Format Support** - Supports serialization and deserialization of Date, LocalDate, LocalTime,
  LocalDateTime
- ✅ **Flexible Time Format Configuration** - Supports custom time formats or timestamp mode
- ✅ **Multi-format Deserialization** - Automatically recognizes multiple international date/time formats
- ✅ **Enum Serialization** - Supports serialization of enum types implementing BaseEnum interface
- ✅ **Internationalization Support** - Enum descriptions support multi-language i18n
- ✅ **BigDecimal Global Customization** - Supports BigDecimal precision control and string serialization
- ✅ **Numeric Overflow Protection** - Automatic overflow checking for Long and Integer deserialization
- ✅ **Floating-Point Precision Control** - Automatic precision retention for Double and Float
- ✅ **Thread-Safe** - All utility classes and methods are designed to be thread-safe

### Supported Time Formats

#### Date/LocalDateTime Supported Formats (16+ formats)

- **ISO formats**: `yyyy-MM-dd'T'HH:mm:ss`
- **Chinese formats**: `yyyy-MM-dd HH:mm:ss`, `yyyy/MM/dd HH:mm:ss`, `yyyy 年 MM 月 dd 日 HH 时 mm 分 ss 秒`
- **European formats**: `dd-MM-yyyy HH:mm:ss`, `dd/MM/yyyy HH:mm:ss`, `dd.MM.yyyy HH:mm:ss`
- **US formats**: `MM-dd-yyyy HH:mm:ss`, `MM/dd/yyyy HH:mm:ss`, `MM.dd.yyyy HH:mm:ss`
- **Short formats**: `yyyy-MM-dd HH:mm`, `yyyy/MM/dd HH:mm`
- **Timezone formats**: `yyyy-MM-dd HH:mm:ss Z`, `yyyy-MM-dd HH:mm:ssXXX`
- **Without seconds**: `yyyy-MM-dd'T'HH:mm`, `yyyy-MM-dd'T'HH:mm:ss`

#### LocalDate Supported Formats (13+ formats)

- **ISO format**: `yyyy-MM-dd`
- **Chinese formats**: `yyyy-MM-dd`, `yyyy/MM/dd`, `yyyy 年 MM 月 dd 日`
- **European formats**: `dd-MM-yyyy`, `dd/MM/yyyy`, `dd.MM.yyyy`
- **US formats**: `MM-dd-yyyy`, `MM/dd/yyyy`, `MM.dd.yyyy`
- **Short formats**: `yy-MM-dd`, `yy/MM/dd`
- **Month name formats**: `dd MMMM yyyy`, `dd MMM yyyy`, `MMMM dd, yyyy`, `MMM dd, yyyy`

#### LocalTime Supported Formats (10+ formats)

- **ISO format**: `HH:mm:ss`
- **Standard formats**: `HH:mm:ss`, `HH:mm:ss.SSS`, `HH:mm:ss.SSSSSS`, `HH:mm:ss.SSSSSSSSS`
- **Without seconds**: `HH:mm`, `H:mm`, `hh:mm a`, `hh:mm:ss a`
- **Timezone formats**: `HH:mm:ss Z`, `HH:mm:ssXXX`

## Configuration

### Configuration File

### Configuration File

Add the following configuration to `application.yml`:

```yaml
spring:
  whale:
    json:
      # Whether to enable internationalization (default: false)
      use-i18n: true
      # Whether to fallback to default description when i18n key is not found (default: false)
      fallback-to-default-desc: true
      # Date-time format (default: yyyy-MM-dd HH:mm:ss, or set to 'timestamp' for timestamp mode)
      date-time-format: yyyy-MM-dd HH:mm:ss
      # Date format (default: yyyy-MM-dd)
      date-format: yyyy-MM-dd
      # Time format (default: HH:mm:ss)
      time-format: HH:mm:ss
      # ==================== BigDecimal Configuration ====================
      # Whether to enable BigDecimal global serialization customization (default: true)
      big-decimal-enabled: true
      # Number of decimal places to retain for BigDecimal (default: 2)
      big-decimal-scale: 2
      # Rounding mode for BigDecimal (default: HALF_UP)
      big-decimal-rounding-mode: HALF_UP
      # Whether to serialize as string to prevent precision loss in frontend (default: true)
      big-decimal-as-string: true
      # Number of decimal places for Float/Double (default: 8)
      float-precision: 8
```

### Configuration Items

| Item                        | Type         | Default             | Description                                                                      |
|-----------------------------|--------------|---------------------|----------------------------------------------------------------------------------|
| `use-i18n`                  | boolean      | false               | Whether to enable internationalization for enum descriptions                     |
| `fallback-to-default-desc`  | boolean      | false               | Whether to fallback to enum's default description when i18n key is not found     |
| `date-time-format`          | String       | yyyy-MM-dd HH:mm:ss | Format pattern for Date and LocalDateTime, set to `timestamp` for timestamp mode |
| `date-format`               | String       | yyyy-MM-dd          | Format pattern for LocalDate                                                     |
| `time-format`               | String       | HH:mm:ss            | Format pattern for LocalTime                                                     |
| `big-decimal-enabled`       | boolean      | true                | Whether to enable BigDecimal global serialization customization                  |
| `big-decimal-scale`         | int          | 2                   | Number of decimal places to retain for BigDecimal                                |
| `big-decimal-rounding-mode` | RoundingMode | HALF_UP             | Rounding mode for BigDecimal                                                     |
| `big-decimal-as-string`     | boolean      | true                | Whether to serialize BigDecimal as string (prevents precision loss in frontend)  |
| `float-precision`           | int          | 8                   | Number of decimal places for Float and Double                                    |

## Time Type Handling

### Serialization

#### 1. Default Format Serialization

```java
public class TimeRecord {
    private Date date;
    private LocalDateTime localDateTime;
    private LocalDate localDate;
    private LocalTime localTime;
}
```

Configuration:

```yaml
spring:
  whale:
    json:
      date-time-format: yyyy-MM-dd HH:mm:ss
      date-format: yyyy-MM-dd
      time-format: HH:mm:ss
```

Output:

```json
{
  "date": "2024-03-25 14:30:45",
  "localDateTime": "2024-03-25 14:30:45",
  "localDate": "2024-03-25",
  "localTime": "14:30:45"
}
```

#### 2. Timestamp Serialization

Configuration:

```yaml
spring:
  whale:
    json:
      date-time-format: timestamp
```

Output:

```json
{
  "date": 1711353045000,
  "localDateTime": 1711353045000
}
```

#### 3. Custom Format Serialization

Configuration:

```yaml
spring:
  whale:
    json:
      date-time-format: dd/MM/yyyy HH:mm
      date-format: dd/MM/yyyy
      time-format: HH:mm
```

Output:

```json
{
  "date": "25/03/2024 14:30",
  "localDateTime": "25/03/2024 14:30",
  "localDate": "25/03/2024",
  "localTime": "14:30"
}
```

### Deserialization

#### 1. Timestamp Deserialization

```json
// Numeric timestamp
{
  "date": 1711353045000
}

// String timestamp
{
  "date": "1711353045000"
}
```

#### 2. String Format Deserialization

Supports automatic recognition of multiple formats:

```json
// ISO format
{
  "date": "2024-03-25T14:30:45"
}
{
  "localDate": "2024-03-25"
}
{
  "localTime": "14:30:45"
}

// Chinese format
{
  "date": "2024/03/25 14:30:45"
}
{
  "localDate": "2024/03/25"
}

// European format
{
  "date": "25-03-2024 14:30:45"
}
{
  "localDate": "25-03-2024"
}

// US format
{
  "localDate": "03/25/2024"
}

// Without seconds
{
  "localTime": "14:30"
}
{
  "localDateTime": "2024-03-25 14:30"
}

// With milliseconds
{
  "localTime": "14:30:45.123"
}
```

The deserialization logic automatically tries all supported formats until it finds a matching one.

## Enum Type Handling

### Defining Enums

Implement the `BaseEnum` interface:

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

### Serialization

#### 1. Without Internationalization

Configuration:

```yaml
spring:
  whale:
    json:
      use-i18n: false
```

Output:

```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

#### 2. With Internationalization

Configuration:

```yaml
spring:
  whale:
    json:
      use-i18n: true
      fallback-to-default-desc: true
```

Create i18n resource file `messages.properties`:

```properties
ACTIVE=Active
INACTIVE=Inactive
PENDING=Pending
DELETED=Deleted
```

Output (English locale):

```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

Output (Chinese locale with translations):

```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "活跃"
  }
}
```

### Deserialization

Supports three deserialization methods:

#### 1. Object Format

```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

#### 2. String Format (using id)

```json
{
  "status": "ACTIVE"
}
```

#### 3. Integer Format (using ordinal)

```json
{
  "status": 0
}
```

### Error Handling

#### 1. Invalid ID

```json
{
  "status": "INVALID_ID"
}
```

Throws exception:

```
IllegalArgumentException: No enum constant with id: INVALID_ID in class: com.example.StatusEnum
```

#### 2. Invalid Ordinal

```json
{
  "status": -1
}
{
  "status": 999
}
```

Throws exception:

```
ArrayIndexOutOfBoundsException / IllegalArgumentException
```

#### 3. Missing i18n Key with Fallback Disabled

Configuration:

```yaml
spring:
  whale:
    json:
      use-i18n: true
      fallback-to-default-desc: false
```

Throws `NoSuchMessageException` if the corresponding i18n key is not found.

## BigDecimal Handling

### Serialization

#### 1. Default Configuration Serialization

Configuration:

```yaml
spring:
  whale:
    json:
      big-decimal-enabled: true
      big-decimal-scale: 2
      big-decimal-rounding-mode: HALF_UP
      big-decimal-as-string: true
```

Code:

```java
public class PriceDTO {
    private BigDecimal price;
}
```

Output:

```json
{
  "price": "99.99"
}
```

#### 5. Double/Float Precision Control

Configuration:

```yaml
spring:
  whale:
    json:
      float-precision: 4
```

Code:

```java
public class MeasurementDTO {
    private Double temperature;
    private Float humidity;
}
```

Output:

```json
{
  "temperature": 36.5678,
  "humidity": 65.1234
}
```

**Note**: Float and Double will be automatically rounded according to the configured precision to avoid floating-point
precision issues.

## Numeric Type Handling

### Long/Integer Overflow Protection

When deserializing Long and Integer, if the input value exceeds the type range, an exception will be thrown.

#### 1. Long Overflow Protection

```json
// Normal value
{
  "value": 1234567890123456789
}

// Exceeds Long.MAX_VALUE, throws exception
{
  "value": 1234567890123456789000000
}

// Exceeds Long.MIN_VALUE, throws exception
{
  "value": -1234567890123456789000000
}
```

Exception message:

```
DatabindException: Long value overflow: 1234567890123456789000000
```

#### 2. Integer Overflow Protection

```json
// Normal value
{
  "value": 2147483647
}

// Exceeds Integer.MAX_VALUE, throws exception
{
  "value": 3000000000
}

// Exceeds Integer.MIN_VALUE, throws exception
{
  "value": -3000000000
}
```

Exception message:

```
DatabindException: Integer value overflow: 3000000000
```

### Double/Float Precision Control

#### 1. Double Serialization

Configuration:

```yaml
spring:
  whale:
    json:
      float-precision: 6
```

Code:

```java
Double value = 123.456789012;
```

Output:

```json
{
  "value": 123.456789
}
```

#### 2. Float Serialization

Configuration:

```yaml
spring:
  whale:
    json:
      float-precision: 4
```

Code:

```java
Float value = 0.123456789f;
```

Output:

```json
{
  "value": 0.1235
}
```

**Notes**:

- Precision control uses `BigDecimal` for accurate calculations
- Rounding mode uses the configured `big-decimal-rounding-mode` (default HALF_UP)
- Automatically strips trailing zeros (stripTrailingZeros)

## Usage Examples

### Complete Example

#### 1. Entity Class Definition

#### 2. Different Precision Configuration

Configuration:

```yaml
spring:
  whale:
    json:
      big-decimal-scale: 4
      big-decimal-rounding-mode: HALF_DOWN
```

Code:

```java
BigDecimal value = new BigDecimal("123.456789");
```

Output:

```json
{
  "value": "123.4568"
}
```

#### 3. Numeric Format Serialization

Configuration:

```yaml
spring:
  whale:
    json:
      big-decimal-as-string: false
```

Output:

```json
{
  "price": 99.99,
  "discount": 0.15
}
```

**Note**: Using numeric format may cause precision loss in frontend JavaScript. String format is recommended.

#### 4. Disable Global Customization

Configuration:

```yaml
spring:
  whale:
    json:
      big-decimal-enabled: false
```

Output (keeps original precision):

```json
{
  "price": 99.99123456789
}
```

### Deserialization

Supports three deserialization methods:

#### 1. String Format (Recommended)

```json
{
  "price": "99.99"
}
```

#### 2. Numeric Format

```json
{
  "price": 99.99
}
```

#### 3. Scientific Notation

```json
{
  "price": "1.23E+2"
}
```

### Rounding Mode Description

Supported rounding modes (`RoundingMode` enum):

| Rounding Mode | Description                                                                                             | Example (2.5) | Example (2.6) |
|---------------|---------------------------------------------------------------------------------------------------------|---------------|---------------|
| `HALF_UP`     | Round towards nearest neighbor, unless both neighbors are equidistant, then round up                    | 3             | 3             |
| `HALF_DOWN`   | Round towards nearest neighbor, unless both neighbors are equidistant, then round down                  | 2             | 3             |
| `HALF_EVEN`   | Round towards nearest neighbor, unless both neighbors are equidistant, then round towards even neighbor | 2             | 3             |
| `UP`          | Round away from zero                                                                                    | 3             | 3             |
| `DOWN`        | Round towards zero                                                                                      | 2             | 2             |
| `CEILING`     | Round towards positive infinity                                                                         | 3             | 3             |
| `FLOOR`       | Round towards negative infinity                                                                         | 2             | 2             |
| `UNNECESSARY` | Do not round, throw exception if rounding is necessary                                                  | Exception     | Exception     |

### Best Practices

#### 1. Monetary Amount Handling

For monetary amount fields, it is recommended to:

- Use `big-decimal-as-string: true` to avoid precision loss in frontend
- Set appropriate precision (usually 2 decimal places)
- Use `HALF_UP` rounding mode to conform to business practices

#### 2. High-Precision Calculations

For scientific calculations requiring high precision:

- Increase `big-decimal-scale` to an appropriate value (e.g., 10)
- Consider using `HALF_EVEN` banker's rounding to reduce cumulative errors

#### 3. Database Integration

Ensure database precision matches configuration:

- MySQL: `DECIMAL(10,2)` corresponds to `big-decimal-scale: 2`
- Oracle: `NUMBER(10,4)` corresponds to `big-decimal-scale: 4`

## BigDecimal Handling

### Serialization

#### 1. Default Configuration Serialization

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

#### 2. Controller Usage

```java

@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        // Received JSON will be automatically deserialized to UserDTO
        // Time fields support multiple formats
        // Enum fields support object, string, and integer formats
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO userDTO = userService.getUser(id);
        // Returned object will be automatically serialized to JSON
        // Time fields output according to configured format
        // Enum fields include id and desc
        return ResponseEntity.ok(userDTO);
    }
}
```

#### 3. Frontend Request Example

```javascript
// POST /users
{
  "username": "John Doe",
  "status": "ACTIVE",  // or {"id": "ACTIVE", "desc": "Active"} or 0
  "createTime": "2024-03-25T14:30:45",  // or 1711353045000
  "birthday": "2000-01-01",  // or 946684800000
  "lastLoginTime": "2024-03-25 14:30:45",  // or 1711353045000
  "salary": "12345.67",  // or 12345.67 (string format recommended)
  "latitude": 39.9042,  // automatically retains configured precision
  "accuracy": 10.5  // automatically retains configured precision
}
```

#### 4. Response Example

```json
{
  "id": 1,
  "username": "John Doe",
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  },
  "createTime": "2024-03-25 14:30:45",
  "birthday": "2000-01-01",
  "lastLoginTime": "2024-03-25 14:30:45",
  "salary": "12345.67",
  "latitude": 39.9042,
  "accuracy": 10.5
}
```

### Internationalization Configuration Example

#### 1. Create I18N Resource Files

`resources/messages.properties` (Default):

```properties
ACTIVE=Active
INACTIVE=Inactive
PENDING=Pending
DELETED=Deleted
```

`resources/messages_zh_CN.properties` (Chinese):

```properties
ACTIVE=活跃
INACTIVE=非活跃
PENDING=待处理
DELETED=已删除
```

`resources/messages_ja_JP.properties` (Japanese):

```properties
ACTIVE=アクティブ
INACTIVE=非アクティブ
PENDING=ペンディング
DELETED=削除済み
```

#### 2. Dynamic Language Switching

```java
// Set locale in request
LocaleContextHolder.setLocale(Locale.JAPAN);

// Or set via interceptor based on request header
@GetMapping(value = "/users/{id}", headers = "Accept-Language=ja-JP")
public ResponseEntity<UserDTO> getUserJapanese(@PathVariable Long id) {
    LocaleContextHolder.setLocale(Locale.JAPAN);
    UserDTO userDTO = userService.getUser(id);
    return ResponseEntity.ok(userDTO);
}
```

## Best Practices

### 1. Time Format Selection

- **Internal Systems**: Recommend using timestamp mode for better performance and no timezone issues
- **Public APIs**: Recommend using ISO format (`yyyy-MM-dd HH:mm:ss`) for better readability
- **Mobile Applications**: Recommend using timestamp to reduce parsing overhead
- **Internationalized Systems**: Recommend using UTC timestamps uniformly, then converting to local time on frontend

### 2. Enum Usage Standards

- All business enums should implement `BaseEnum` interface
- Use meaningful strings for `id` field (e.g., `"ACTIVE"`), avoid pure numbers
- Provide complete i18n resource files
- In production, recommend enabling `fallback-to-default-desc: true`

### 3. Performance Optimization

- Timestamp format has better performance than string format
- Disabling unnecessary i18n support (`use-i18n: false`) improves performance
- For high-frequency endpoints, consider using DTOs to optimize JSON structure

### 4. Compatibility Considerations

- Ensure compatibility for old fields during API upgrades
- When changing time formats, ensure deserialization supports both old and new formats
- Keep deprecated enum values for a period before removing them

### 5. Testing Recommendations

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

## Notes

### 1. Thread Safety

- `DateTimeFormats` utility class is thread-safe
- All `DateTimeFormatter` instances are immutable
- Safe to use in multi-threaded environments

### 2. Timezone Handling

- Uses system default timezone by default (`ZoneId.systemDefault()`)
- Distributed systems should use UTC timezone uniformly
- Convert to local time on frontend for display

### 3. Precision Issues

- Timestamp precision is milliseconds
- `LocalTime` supports nanosecond precision (`HH:mm:ss.SSSSSSSSS`)
- Ensure precision matching when storing in database

### 4. Null Value Handling

- `null` values are serialized as JSON `null` normally
- `null` strings are not supported during deserialization; use `@JsonInclude` in DTOs for special handling

### 5. BigDecimal Handling

- BigDecimal global customization is enabled by default, retaining 2 decimal places with HALF_UP rounding
- It is recommended to enable `big-decimal-as-string: true` to avoid precision loss in frontend
- For higher precision requirements, adjust the `big-decimal-scale` configuration
- After disabling global customization, Jackson's default processing method is used

### 6. Numeric Overflow Protection

- Long and Integer automatically check for overflow during deserialization
- A `DatabindException` exception is thrown when overflow occurs
- It is recommended to handle exceptions properly when receiving numeric parameters from frontend

### 7. Floating-Point Precision

- Double and Float automatically retain precision according to `float-precision` configuration
- Precision control uses BigDecimal for accurate calculations
- Avoids floating-point precision issues in languages like JavaScript

## Related Resources

- [Jackson Official Documentation](https://github.com/FasterXML/jackson-docs)
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Java Time API Documentation](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
