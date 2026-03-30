# Spring Whale JSON 直列化

Spring Whale フレームワークは、Spring Boot と Tools Jackson をベースにした強力な JSON 直列化・逆直列化機能を提供し、カスタム時間形式、列挙型処理、国際化などの機能をサポートしています。

## 目次

- [機能概要](#機能概要)
- [設定](#設定)
- [時間型の処理](#時間型の処理)
- [列挙型の処理](#列挙型の処理)
- [使用例](#使用例)
- [ベストプラクティス](#ベストプラクティス)

## 機能概要

### コア機能

- ✅ **複数の時間形式サポート** - Date、LocalDate、LocalTime、LocalDateTime の直列化・逆直列化をサポート
- ✅ **柔軟な時間形式設定** - カスタム時間形式またはタイムスタンプモードをサポート
- ✅ **マルチフォーマット逆直列化** - 複数の国際的な日付・時間形式を自動認識
- ✅ **列挙型直列化** - BaseEnum インターフェースを実装した列挙型の直列化をサポート
- ✅ **国際化サポート** - 列挙型の説明に多言語 i18n をサポート
- ✅ **BigDecimal グローバルカスタマイズ** - BigDecimal の精度制御と文字列直列化をサポート
- ✅ **スレッドセーフ** - すべてのユーティリティクラスとメソッドはスレッドセーフに設計されています

### サポートされている時間形式

#### Date/LocalDateTime サポート形式（16+ 形式）

- **ISO 形式**: `yyyy-MM-dd'T'HH:mm:ss`
- **中国語形式**: `yyyy-MM-dd HH:mm:ss`, `yyyy/MM/dd HH:mm:ss`, `yyyy 年 MM 月 dd 日 HH 時 mm 分 ss 秒`
- **ヨーロッパ形式**: `dd-MM-yyyy HH:mm:ss`, `dd/MM/yyyy HH:mm:ss`, `dd.MM.yyyy HH:mm:ss`
- **米国形式**: `MM-dd-yyyy HH:mm:ss`, `MM/dd/yyyy HH:mm:ss`, `MM.dd.yyyy HH:mm:ss`
- **短縮形式**: `yyyy-MM-dd HH:mm`, `yyyy/MM/dd HH:mm`
- **タイムゾーン形式**: `yyyy-MM-dd HH:mm:ss Z`, `yyyy-MM-dd HH:mm:ssXXX`
- **秒なし形式**: `yyyy-MM-dd'T'HH:mm`, `yyyy-MM-dd'T'HH:mm:ss`

#### LocalDate サポート形式（13+ 形式）

- **ISO 形式**: `yyyy-MM-dd`
- **中国語形式**: `yyyy-MM-dd`, `yyyy/MM/dd`, `yyyy 年 MM 月 dd 日`
- **ヨーロッパ形式**: `dd-MM-yyyy`, `dd/MM/yyyy`, `dd.MM.yyyy`
- **米国形式**: `MM-dd-yyyy`, `MM/dd/yyyy`, `MM.dd.yyyy`
- **短縮形式**: `yy-MM-dd`, `yy/MM/dd`
- **月名形式**: `dd MMMM yyyy`, `dd MMM yyyy`, `MMMM dd, yyyy`, `MMM dd, yyyy`

#### LocalTime サポート形式（10+ 形式）

- **ISO 形式**: `HH:mm:ss`
- **標準形式**: `HH:mm:ss`, `HH:mm:ss.SSS`, `HH:mm:ss.SSSSSS`, `HH:mm:ss.SSSSSSSSS`
- **秒なし形式**: `HH:mm`, `H:mm`, `hh:mm a`, `hh:mm:ss a`
- **タイムゾーン形式**: `HH:mm:ss Z`, `HH:mm:ssXXX`

## 設定

### 設定ファイル

### 設定ファイル

`application.yml` に以下の設定を追加します：

```yaml
spring:
  whale:
    json:
      # 国際化を有効にするかどうか（デフォルト：false）
      use-i18n: true
      # i18n キーが見つからない場合にデフォルトの説明にフォールバックするかどうか（デフォルト：false）
      fallback-to-default-desc: true
      # 日時形式（デフォルト：yyyy-MM-dd HH:mm:ss、またはタイムスタンプモードには'timestamp'を設定）
      date-time-format: yyyy-MM-dd HH:mm:ss
      # 日付形式（デフォルト：yyyy-MM-dd）
      date-format: yyyy-MM-dd
      # 時間形式（デフォルト：HH:mm:ss）
      time-format: HH:mm:ss
      # ==================== BigDecimal 設定 ====================
      # BigDecimal グローバル直列化カスタマイズを有効にするかどうか（デフォルト：true）
      big-decimal-enabled: true
      # BigDecimal の小数点以下の桁数（デフォルト：2）
      big-decimal-scale: 2
      # BigDecimal の丸めモード（デフォルト：HALF_UP）
      big-decimal-rounding-mode: HALF_UP
      # フロントエンドでの精度低下を防ぐために文字列として直列化するかどうか（デフォルト：true）
      big-decimal-as-string: true
```

### 設定項目

| 項目 | 型 | デフォルト | 説明 |
|------|------|----------|------|
| `use-i18n` | boolean | false | 列挙型の説明に国際化を有効にするかどうか |
| `fallback-to-default-desc` | boolean | false | i18n キーが見つからない場合に列挙型のデフォルトの説明にフォールバックするかどうか |
| `date-time-format` | String | yyyy-MM-dd HH:mm:ss | Date と LocalDateTime の形式パターン、タイムスタンプモードには `timestamp` を設定 |
| `date-format` | String | yyyy-MM-dd | LocalDate の形式パターン |
| `time-format` | String | HH:mm:ss | LocalTime の形式パターン |
| `big-decimal-enabled` | boolean | true | BigDecimal グローバル直列化カスタマイズを有効にするかどうか |
| `big-decimal-scale` | int | 2 | BigDecimal の小数点以下の桁数 |
| `big-decimal-rounding-mode` | RoundingMode | HALF_UP | BigDecimal の丸めモード |
| `big-decimal-as-string` | boolean | true | フロントエンドでの精度低下を防ぐために文字列として直列化するかどうか |

## 時間型の処理

### 直列化

#### 1. デフォルト形式の直列化

```java
public class TimeRecord {
    private Date date;
    private LocalDateTime localDateTime;
    private LocalDate localDate;
    private LocalTime localTime;
}
```

設定：
```yaml
spring:
  whale:
    json:
      date-time-format: yyyy-MM-dd HH:mm:ss
      date-format: yyyy-MM-dd
      time-format: HH:mm:ss
```

出力：
```json
{
  "date": "2024-03-25 14:30:45",
  "localDateTime": "2024-03-25 14:30:45",
  "localDate": "2024-03-25",
  "localTime": "14:30:45"
}
```

#### 2. タイムスタンプの直列化

設定：
```yaml
spring:
  whale:
    json:
      date-time-format: timestamp
```

出力：
```json
{
  "date": 1711353045000,
  "localDateTime": 1711353045000
}
```

#### 3. カスタム形式の直列化

設定：
```yaml
spring:
  whale:
    json:
      date-time-format: dd/MM/yyyy HH:mm
      date-format: dd/MM/yyyy
      time-format: HH:mm
```

出力：
```json
{
  "date": "25/03/2024 14:30",
  "localDateTime": "25/03/2024 14:30",
  "localDate": "25/03/2024",
  "localTime": "14:30"
}
```

### 逆直列化

#### 1. タイムスタンプの逆直列化

```json
// 数値タイムスタンプ
{"date": 1711353045000}

// 文字列タイムスタンプ
{"date": "1711353045000"}
```

#### 2. 文字列形式の逆直列化

複数の形式の自動認識をサポート：

```json
// ISO 形式
{"date": "2024-03-25T14:30:45"}
{"localDate": "2024-03-25"}
{"localTime": "14:30:45"}

// 中国語形式
{"date": "2024/03/25 14:30:45"}
{"localDate": "2024/03/25"}

// ヨーロッパ形式
{"date": "25-03-2024 14:30:45"}
{"localDate": "25-03-2024"}

// 米国形式
{"localDate": "03/25/2024"}

// 秒なし形式
{"localTime": "14:30"}
{"localDateTime": "2024-03-25 14:30"}

// ミリ秒付き形式
{"localTime": "14:30:45.123"}
```

逆直列化ロジックは、サポートされているすべての形式を自動的に試し、一致する形式を見つけますまで続けます。

## 列挙型の処理

### 列挙型の定義

`BaseEnum` インターフェースを実装します：

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

### 直列化

#### 1. 国際化なしの場合

設定：
```yaml
spring:
  whale:
    json:
      use-i18n: false
```

出力：
```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

#### 2. 国際化ありの場合

設定：
```yaml
spring:
  whale:
    json:
      use-i18n: true
      fallback-to-default-desc: true
```

i18n リソースファイル `messages.properties` を作成：
```properties
ACTIVE=Active
INACTIVE=Inactive
PENDING=Pending
DELETED=Deleted
```

出力（英語ロケール）：
```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "Active"
  }
}
```

出力（中国語ロケール、翻訳がある場合）：
```json
{
  "status": {
    "id": "ACTIVE",
    "desc": "活跃"
  }
}
```

### 逆直列化

3 つの逆直列化方法をサポート：

#### 1. オブジェクト形式

```json
{"status": {"id": "ACTIVE", "desc": "Active"}}
```

#### 2. 文字列形式（id を使用）

```json
{"status": "ACTIVE"}
```

#### 3. 整数形式（ordinal を使用）

```json
{"status": 0}
```

### エラーハンドリング

#### 1. 無効な ID

```json
{"status": "INVALID_ID"}
```

例外をスロー：
```
IllegalArgumentException: No enum constant with id: INVALID_ID in class: com.example.StatusEnum
```

#### 2. 無効な ordinal

```json
{"status": -1}
{"status": 999}
```

例外をスロー：
```
ArrayIndexOutOfBoundsException / IllegalArgumentException
```

#### 3. i18n キーが不足していてフォールバックが無効

設定：
```yaml
spring:
  whale:
    json:
      use-i18n: true
      fallback-to-default-desc: false
```

対応する i18n キーが見つからない場合、`NoSuchMessageException` をスローします。

## BigDecimal の処理

### 直列化

#### 1. デフォルト設定の直列化

設定：
```yaml
spring:
  whale:
    json:
      big-decimal-enabled: true
      big-decimal-scale: 2
      big-decimal-rounding-mode: HALF_UP
      big-decimal-as-string: true
```

コード：
```java
public class PriceDTO {
    private BigDecimal price;
    private BigDecimal discount;
}
```

出力：
```json
{
  "price": "99.99",
  "discount": "0.15"
}
```

#### 2. 異なる精度設定

設定：
```yaml
spring:
  whale:
    json:
      big-decimal-scale: 4
      big-decimal-rounding-mode: HALF_DOWN
```

コード：
```java
BigDecimal value = new BigDecimal("123.456789");
```

出力：
```json
{
  "value": "123.4568"
}
```

#### 3. 数値形式の直列化

設定：
```yaml
spring:
  whale:
    json:
      big-decimal-as-string: false
```

出力：
```json
{
  "price": 99.99,
  "discount": 0.15
}
```

**注意**: 数値形式を使用するとフロントエンドの JavaScript で精度が失われる可能性があります。文字列形式を推奨します。

#### 4. グローバルカスタマイズの無効化

設定：
```yaml
spring:
  whale:
    json:
      big-decimal-enabled: false
```

出力（元の精度を維持）：
```json
{
  "price": 99.99123456789
}
```

### 逆直列化

3 つの逆直列化方法をサポート：

#### 1. 文字列形式（推奨）

```json
{"price": "99.99"}
```

#### 2. 数値形式

```json
{"price": 99.99}
```

#### 3. 指数表記

```json
{"price": "1.23E+2"}
```

### 丸めモードの説明

サポートされている丸めモード（`RoundingMode` 列挙型）：

| 丸めモード | 説明 | 例 (2.5) | 例 (2.6) |
|-----------|------|---------|---------|
| `HALF_UP` | 最も近い値に丸める。両方が等距離の場合は大きい方に丸める | 3 | 3 |
| `HALF_DOWN` | 最も近い値に丸める。両方が等距離の場合は小さい方に丸める | 2 | 3 |
| `HALF_EVEN` | 最も近い値に丸める。両方が等距離の場合は偶数の方に丸める（銀行家丸め） | 2 | 3 |
| `UP` | ゼロから遠ざかる方向に丸める | 3 | 3 |
| `DOWN` | ゼロに近づく方向に丸める | 2 | 2 |
| `CEILING` | 正の無限大方向に丸める | 3 | 3 |
| `FLOOR` | 負の無限大方向に丸める | 2 | 2 |
| `UNNECESSARY` | 丸め不要。必要な場合は例外をスロー | 例外 | 例外 |

### ベストプラクティス

#### 1. 金額フィールドの扱い

金額フィールドの場合、以下を推奨：
- `big-decimal-as-string: true` を使用してフロントエンドでの精度低下を防止
- 適切な精度（通常 2 桁）を設定
- 商業慣習に合わせて `HALF_UP` 丸めモードを使用

#### 2. 高精度計算

高精度が必要な科学計算の場合：
- `big-decimal-scale` を適切な値（例：10）に増やす
- 累積誤差を減らすために `HALF_EVEN` 銀行家丸めを検討

#### 3. データベース連携

データベースの精度が設定と一致することを確認：
- MySQL: `DECIMAL(10,2)` は `big-decimal-scale: 2` に対応
- Oracle: `NUMBER(10,4)` は `big-decimal-scale: 4` に対応

## 使用例

### 完全な例

#### 1. エンティティクラスの定義

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
    private BigDecimal salary;  // 新しい BigDecimal フィールド
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

#### 2. Controller の使用

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        // 受信した JSON は自動的に UserDTO に逆直列化されます
        // 時間フィールドは複数の形式をサポートします
        // 列挙型フィールドはオブジェクト、文字列、整数形式をサポートします
        return ResponseEntity.ok(userDTO);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        UserDTO userDTO = userService.getUser(id);
        // 返されるオブジェクトは自動的に JSON に直列化されます
        // 時間フィールドは設定された形式で出力されます
        // 列挙型フィールドは id と desc を含みます
        return ResponseEntity.ok(userDTO);
    }
}
```

#### 3. フロントエンドリクエスト例

```javascript
// POST /users
{
  "username": "山田太郎",
  "status": "ACTIVE",  // または {"id": "ACTIVE", "desc": "Active"} または 0
  "createTime": "2024-03-25T14:30:45",  // または 1711353045000
  "birthday": "2000-01-01",  // または 946684800000
  "lastLoginTime": "2024-03-25 14:30:45",  // または 1711353045000
  "salary": "12345.67"  // または 12345.67（文字列形式を推奨）
}
```

#### 4. レスポンス例

```json
{
  "id": 1,
  "username": "山田太郎",
  "status": {
    "id": "ACTIVE",
    "desc": "アクティブ"
  },
  "createTime": "2024-03-25 14:30:45",
  "birthday": "2000-01-01",
  "lastLoginTime": "2024-03-25 14:30:45",
  "salary": "12345.67"
}
```

### 国際化設定例

#### 1. I18N リソースファイルの作成

`resources/messages.properties` (デフォルト):
```properties
ACTIVE=Active
INACTIVE=Inactive
PENDING=Pending
DELETED=Deleted
```

`resources/messages_zh_CN.properties` (中国語):
```properties
ACTIVE=活跃
INACTIVE=非活跃
PENDING=待处理
DELETED=已删除
```

`resources/messages_ja_JP.properties` (日本語):
```properties
ACTIVE=アクティブ
INACTIVE=非アクティブ
PENDING=ペンディング
DELETED=削除済み
```

#### 2. 動的言語切り替え

```java
// リクエスト内でロケールを設定
LocaleContextHolder.setLocale(Locale.JAPAN);

// またはリクエストヘッダーに基づいてインターセプターで設定
@GetMapping(value = "/users/{id}", headers = "Accept-Language=ja-JP")
public ResponseEntity<UserDTO> getUserJapanese(@PathVariable Long id) {
    LocaleContextHolder.setLocale(Locale.JAPAN);
    UserDTO userDTO = userService.getUser(id);
    return ResponseEntity.ok(userDTO);
}
```

## ベストプラクティス

### 1. 時間形式の選択

- **内部システム**: タイムスタンプモードの使用を推奨（パフォーマンスが良く、タイムゾーンの問題がない）
- **公開 API**: ISO 形式（`yyyy-MM-dd HH:mm:ss`）の使用を推奨（可読性が高い）
- **モバイルアプリケーション**: タイムスタンプの使用を推奨（解析オーバーヘッドを削減）
- **国際化システム**: UTC タイムスタンプを統一して使用し、フロントエンドでローカル時間に変換することを推奨

### 2. 列挙型の使用基準

- すべてのビジネス列挙型は `BaseEnum` インターフェースを実装する必要があります
- `id` フィールドには意味のある文字列（例：`"ACTIVE"`）を使用し、純粋な数値は避けてください
- 完全な i18n リソースファイルを提供してください
- 本番環境では `fallback-to-default-desc: true` の有効化を推奨します

### 3. パフォーマンス最適化

- タイムスタンプ形式は文字列形式よりもパフォーマンスが優れています
- 不要な国際化サポートを無効にすると（`use-i18n: false`）、パフォーマンスが向上します
- 高頻度エンドポイントの場合は、DTO を使用して JSON 構造を最適化することを検討してください

### 4. 互換性の考慮

- API アップグレード時には、旧フィールドの互換性を確保してください
- 時間形式を変更する際は、新旧両方の形式を逆直列化でサポートしていることを確認してください
- 廃止された列挙値は、削除する前に一定期間保持してください

### 5. テストの推奨

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

## 注意事項

### 1. スレッドセーフティ

- `DateTimeFormats` ユーティリティクラスはスレッドセーフです
- すべての `DateTimeFormatter` インスタンスは不変です
- マルチスレッド環境でも安全に使用できます

### 2. タイムゾーンの扱い

- デフォルトではシステムのデフォルトタイムゾーンを使用します（`ZoneId.systemDefault()`）
- 分散システムでは UTC タイムゾーンの統一使用を推奨します
- フロントエンドでの表示時にローカル時間に変換します

### 3. 精度の問題

- タイムスタンプの精度はミリ秒です
- `LocalTime` はナノ秒精度をサポートしています（`HH:mm:ss.SSSSSSSSS`）
- データベースへの保存時に精度のマッチングを確認してください

### 4. null 値の扱い

- `null` 値は正常に JSON の `null` として直列化されます
- 逆直列化時には `null` 文字列はサポートされていません。特別な処理が必要な場合は、DTO で `@JsonInclude` を使用してください

### 5. BigDecimal の扱い

- デフォルトで BigDecimal グローバルカスタマイズが有効で、2 桁の小数を HALF_UP で丸めます
- フロントエンドでの精度低下を防ぐために `big-decimal-as-string: true` の有効化を推奨
- より高い精度が必要な場合は、`big-decimal-scale` 設定を調整
- グローバルカスタマイズを無効にした後、Jackson のデフォルト処理方法を使用

## 関連リソース

- [Jackson 公式ドキュメント](https://github.com/FasterXML/jackson-docs)
- [Spring Boot リファレンスガイド](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Java Time API ドキュメント](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
