package io.github.springwhale.test.json;


import io.github.springwhale.framework.core.enums.BaseEnum;
import io.github.springwhale.framework.core.json.SpringWhaleJsonProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JSON serialization and deserialization of enums
 */
@SpringBootTest
public class SpringWhaleJacksonComponentTest {

    private final SpringWhaleJsonProperties jsonConfigBackup = new SpringWhaleJsonProperties();
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private SpringWhaleJsonProperties jsonProperties;
    private Locale originalDefaultLocale;
    private Locale originalContextLocale;

    @BeforeEach
    void setUp() {
        // Save original locale settings
        originalDefaultLocale = Locale.getDefault();
        originalContextLocale = LocaleContextHolder.getLocale();
        BeanUtils.copyProperties(jsonProperties, jsonConfigBackup);
    }

    @AfterEach
    void tearDown() {
        // Restore original locale settings
        Locale.setDefault(originalDefaultLocale);
        LocaleContextHolder.setLocale(originalContextLocale);
        // Reset config to default values
        BeanUtils.copyProperties(jsonConfigBackup, jsonProperties);
    }

    /**
     * Test enum serialization without i18n
     */
    @Test
    @DisplayName("Should serialize enum with default description when i18n is disabled")
    public void testEnumSerializationWithoutI18n() {
        jsonProperties.setUseI18n(false);
        EnumTestRecord record = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("Active", node.get("desc").asString());
        assertEquals("ACTIVE", node.get("id").asString());
    }

    /**
     * Test that exception is thrown when i18n key is missing and fallback is disabled
     */
    @Test
    @DisplayName("Should throw exception when i18n key is missing and fallback is disabled")
    public void testEnumSerializationWithMissingI18nKeyAndNoFallback() {
        jsonProperties.setFallbackToDefaultDesc(false);
        EnumTestRecord record = new EnumTestRecord(StatusEnum.DELETED);

        assertThrows(DatabindException.class, () -> mapper.writeValueAsString(record));
    }

    /**
     * Test enum serialization with Japanese locale (fallback to English)
     */
    @Test
    @DisplayName("Should serialize enum with English description when Japanese locale is set and translation is missing")
    public void testEnumSerializationWithJapaneseLocaleAndMissingTranslation() {
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);

        EnumTestRecord record = new EnumTestRecord(StatusEnum.DELETED);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("Deleted", node.get("desc").asString());
        assertEquals("DELETED", node.get("id").asString());
    }

    /**
     * Test enum serialization with Japanese locale (has translation)
     */
    @Test
    @DisplayName("Should serialize enum with Japanese description when translation exists")
    public void testEnumSerializationWithJapaneseLocaleAndTranslation() {
        Locale.setDefault(Locale.JAPAN);
        LocaleContextHolder.setLocale(Locale.JAPAN);

        EnumTestRecord record = new EnumTestRecord(StatusEnum.INACTIVE);
        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("status");

        assertEquals("非アクティブ", node.get("desc").asString());
        assertEquals("INACTIVE", node.get("id").asString());
    }

    /**
     * Test enum deserialization from object format
     */
    @Test
    @DisplayName("Should deserialize enum from object format with id and desc")
    public void testEnumDeserializationFromObjectFormat() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = "{\"status\":{\"id\":\"ACTIVE\",\"desc\":\"Active\"}}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    /**
     * Test enum deserialization from string format
     */
    @Test
    @DisplayName("Should deserialize enum from string format using id")
    public void testEnumDeserializationFromStringFormat() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = "{\"status\":\"ACTIVE\"}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    /**
     * Test enum deserialization from integer format (ordinal)
     */
    @Test
    @DisplayName("Should deserialize enum from integer format using ordinal")
    public void testEnumDeserializationFromIntFormat() {
        EnumTestRecord expected = new EnumTestRecord(StatusEnum.ACTIVE);
        String json = "{\"status\":0}";

        EnumTestRecord actual = mapper.readValue(json, EnumTestRecord.class);

        assertEquals(expected, actual);
    }

    /**
     * Test enum deserialization with invalid integer (negative)
     */
    @Test
    @DisplayName("Should throw exception when deserializing from negative integer")
    public void testEnumDeserializationWithInvalidNegativeInt() {
        String json = "{\"status\":-1}";

        assertThrows(Exception.class, () -> mapper.readValue(json, EnumTestRecord.class));
    }

    /**
     * Test enum deserialization with invalid float number
     */
    @Test
    @DisplayName("Should throw exception when deserializing from float number")
    public void testEnumDeserializationWithInvalidFloat() {
        String json = "{\"status\":1.0}";

        assertThrows(Exception.class, () -> mapper.readValue(json, EnumTestRecord.class));
    }

    /**
     * Test enum deserialization with invalid id
     */
    @Test
    @DisplayName("Should throw exception when deserializing from invalid id")
    public void testEnumDeserializationWithInvalidId() {
        String json = "{\"status\":\"ACTIVE2\"}";

        assertThrows(Exception.class, () -> mapper.readValue(json, EnumTestRecord.class));
    }

    /**
     * Test LocalDate serialization with default format
     */
    @Test
    @DisplayName("Should serialize LocalDate with configured format")
    public void testLocalDateSerialization() {
        jsonProperties.setDateFormat("yyyy-MM-dd");
        LocalDate date = LocalDate.of(2024, 3, 25);
        TimeRecord record = new TimeRecord(null, null, date, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localDate");

        assertEquals("2024-03-25", node.asString());
    }

    /**
     * Test LocalDate serialization with custom format
     */
    @Test
    @DisplayName("Should serialize LocalDate with custom format")
    public void testLocalDateSerializationWithCustomFormat() {
        jsonProperties.setDateFormat("dd/MM/yyyy");
        LocalDate date = LocalDate.of(2024, 3, 25);
        TimeRecord record = new TimeRecord(null, null, date, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localDate");

        assertEquals("25/03/2024", node.asString());
    }

    /**
     * Test LocalTime serialization with default format
     */
    @Test
    @DisplayName("Should serialize LocalTime with configured format")
    public void testLocalTimeSerialization() {
        jsonProperties.setTimeFormat("HH:mm:ss");
        LocalTime time = LocalTime.of(14, 30, 45);
        TimeRecord record = new TimeRecord(null, null, null, time);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localTime");

        assertEquals("14:30:45", node.asString());
    }

    /**
     * Test LocalTime serialization with custom format
     */
    @Test
    @DisplayName("Should serialize LocalTime with custom format")
    public void testLocalTimeSerializationWithCustomFormat() {
        jsonProperties.setTimeFormat("HH:mm");
        LocalTime time = LocalTime.of(14, 30, 45);
        TimeRecord record = new TimeRecord(null, null, null, time);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localTime");

        assertEquals("14:30", node.asString());
    }

    /**
     * Test LocalDateTime serialization with default format
     */
    @Test
    @DisplayName("Should serialize LocalDateTime with configured format")
    public void testLocalDateTimeSerialization() {
        jsonProperties.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 25, 14, 30, 45);
        TimeRecord record = new TimeRecord(null, dateTime, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localDateTime");

        assertEquals("2024-03-25 14:30:45", node.asString());
    }

    /**
     * Test LocalDateTime serialization with custom format
     */
    @Test
    @DisplayName("Should serialize LocalDateTime with custom format")
    public void testLocalDateTimeSerializationWithCustomFormat() {
        jsonProperties.setDateTimeFormat("dd/MM/yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 25, 14, 30, 45);
        TimeRecord record = new TimeRecord(null, dateTime, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localDateTime");

        assertEquals("25/03/2024 14:30", node.asString());
    }

    /**
     * Test LocalDateTime serialization as timestamp
     */
    @Test
    @DisplayName("Should serialize LocalDateTime as timestamp when configured")
    public void testLocalDateTimeSerializationAsTimestamp() {
        jsonProperties.setDateTimeFormat("timestamp");
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 25, 14, 30, 45);
        TimeRecord record = new TimeRecord(null, dateTime, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localDateTime");

        // Verify it's a number (timestamp)
        assertTrue(node.isNumber(), "LocalDateTime should be serialized as timestamp number");
        long timestamp = node.asLong();
        assertEquals(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), timestamp);
    }

    /**
     * Test Date serialization with default format
     */
    @Test
    @DisplayName("Should serialize Date with configured format")
    public void testDateSerialization() {
        jsonProperties.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        // Use a specific date and verify the format (not the exact time)
        Date date = new Date(1711353045000L); // 2024-03-25 14:30:45 UTC
        TimeRecord record = new TimeRecord(date, null, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("date");

        // Verify the format is correct (the actual time will depend on timezone)
        assertNotNull(node.asString(), "Date should be serialized as string");
        assertTrue(node.asString().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "Date should match format yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Test Date serialization with custom format
     */
    @Test
    @DisplayName("Should serialize Date with custom format")
    public void testDateSerializationWithCustomFormat() {
        jsonProperties.setDateTimeFormat("dd/MM/yyyy HH:mm");
        Date date = new Date(1711353045000L); // 2024-03-25 14:30:45 UTC
        TimeRecord record = new TimeRecord(date, null, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("date");

        // Verify the format is correct (the actual time will depend on timezone)
        assertNotNull(node.asString(), "Date should be serialized as string");
        assertTrue(node.asString().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"),
                "Date should match format dd/MM/yyyy HH:mm");
    }

    /**
     * Test Date serialization as timestamp
     */
    @Test
    @DisplayName("Should serialize Date as timestamp when configured")
    public void testDateSerializationAsTimestamp() {
        jsonProperties.setDateTimeFormat("timestamp");
        Date date = new Date(1711353045000L); // 2024-03-25 14:30:45 UTC
        TimeRecord record = new TimeRecord(date, null, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("date");

        // Verify it's a number (timestamp)
        assertTrue(node.isNumber(), "Date should be serialized as timestamp number");
        long timestamp = node.asLong();
        assertEquals(1711353045000L, timestamp);
    }

    /**
     * Test Date deserialization from various formats using iterator pattern
     */
    @Test
    @DisplayName("Should deserialize Date from multiple supported formats")
    public void testDateDeserializationWithMultipleFormats() {
        // Test ISO format
        String isoJson = "{\"date\":\"2024-03-25T14:30:45\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.date());
        assertEquals(2024, record.date().toInstant().atZone(ZoneId.systemDefault()).getYear());
        assertEquals(3, record.date().toInstant().atZone(ZoneId.systemDefault()).getMonthValue());
        assertEquals(25, record.date().toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth());

        // Test Chinese format
        String chineseJson = "{\"date\":\"2024/03/25 14:30:45\"}";
        record = mapper.readValue(chineseJson, TimeRecord.class);
        assertNotNull(record.date());

        // Test European format
        String europeanJson = "{\"date\":\"25-03-2024 14:30:45\"}";
        record = mapper.readValue(europeanJson, TimeRecord.class);
        assertNotNull(record.date());

        // Test numeric timestamp
        String timestampJson = "{\"date\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.date());
        assertEquals(1711353045000L, record.date().getTime());

        // Test numeric string timestamp
        String timestampStringJson = "{\"date\":\"1711353045000\"}";
        record = mapper.readValue(timestampStringJson, TimeRecord.class);
        assertNotNull(record.date());
        assertEquals(1711353045000L, record.date().getTime());
    }

    /**
     * Test LocalDate deserialization from various formats using iterator pattern
     */
    @Test
    @DisplayName("Should deserialize LocalDate from multiple supported formats")
    public void testLocalDateDeserializationWithMultipleFormats() {
        // Test ISO format
        String isoJson = "{\"localDate\":\"2024-03-25\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.localDate());
        assertEquals(2024, record.localDate().getYear());
        assertEquals(3, record.localDate().getMonthValue());
        assertEquals(25, record.localDate().getDayOfMonth());

        // Test Chinese format
        String chineseJson = "{\"localDate\":\"2024/03/25\"}";
        record = mapper.readValue(chineseJson, TimeRecord.class);
        assertNotNull(record.localDate());

        // Test European format
        String europeanJson = "{\"localDate\":\"25-03-2024\"}";
        record = mapper.readValue(europeanJson, TimeRecord.class);
        assertNotNull(record.localDate());

        // Test US format
        String usJson = "{\"localDate\":\"03/25/2024\"}";
        record = mapper.readValue(usJson, TimeRecord.class);
        assertNotNull(record.localDate());

        // Test numeric timestamp
        String timestampJson = "{\"localDate\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.localDate());
    }

    /**
     * Test LocalTime deserialization from various formats using iterator pattern
     */
    @Test
    @DisplayName("Should deserialize LocalTime from multiple supported formats")
    public void testLocalTimeDeserializationWithMultipleFormats() {
        // Test ISO format
        String isoJson = "{\"localTime\":\"14:30:45\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.localTime());
        assertEquals(14, record.localTime().getHour());
        assertEquals(30, record.localTime().getMinute());
        assertEquals(45, record.localTime().getSecond());

        // Test without seconds
        String noSecondsJson = "{\"localTime\":\"14:30\"}";
        record = mapper.readValue(noSecondsJson, TimeRecord.class);
        assertNotNull(record.localTime());
        assertEquals(14, record.localTime().getHour());
        assertEquals(30, record.localTime().getMinute());

        // Test with milliseconds
        String withMillisJson = "{\"localTime\":\"14:30:45.123\"}";
        record = mapper.readValue(withMillisJson, TimeRecord.class);
        assertNotNull(record.localTime());
        assertEquals(14, record.localTime().getHour());
        assertEquals(30, record.localTime().getMinute());

        // Test numeric timestamp
        String timestampJson = "{\"localTime\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.localTime());
    }

    /**
     * Test LocalDateTime deserialization from various formats using iterator pattern
     */
    @Test
    @DisplayName("Should deserialize LocalDateTime from multiple supported formats")
    public void testLocalDateTimeDeserializationWithMultipleFormats() {
        // Test ISO format
        String isoJson = "{\"localDateTime\":\"2024-03-25T14:30:45\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.localDateTime());
        assertEquals(2024, record.localDateTime().getYear());
        assertEquals(3, record.localDateTime().getMonthValue());
        assertEquals(25, record.localDateTime().getDayOfMonth());
        assertEquals(14, record.localDateTime().getHour());
        assertEquals(30, record.localDateTime().getMinute());

        // Test Chinese format
        String chineseJson = "{\"localDateTime\":\"2024/03/25 14:30:45\"}";
        record = mapper.readValue(chineseJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        // Test European format
        String europeanJson = "{\"localDateTime\":\"25-03-2024 14:30:45\"}";
        record = mapper.readValue(europeanJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        // Test without seconds
        String noSecondsJson = "{\"localDateTime\":\"2024-03-25 14:30\"}";
        record = mapper.readValue(noSecondsJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        // Test numeric timestamp
        String timestampJson = "{\"localDateTime\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        // Test numeric string timestamp
        String timestampStringJson = "{\"localDateTime\":\"1711353045000\"}";
        record = mapper.readValue(timestampStringJson, TimeRecord.class);
        assertNotNull(record.localDateTime());
    }

    /**
     * Test Date deserialization with invalid format throws exception
     */
    @Test
    @DisplayName("Should throw exception when Date format is invalid")
    public void testDateDeserializationWithInvalidFormat() {
        String invalidJson = "{\"date\":\"invalid-date-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse date"));
    }

    /**
     * Test LocalDate deserialization with invalid format throws exception
     */
    @Test
    @DisplayName("Should throw exception when LocalDate format is invalid")
    public void testLocalDateDeserializationWithInvalidFormat() {
        String invalidJson = "{\"localDate\":\"invalid-date-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse local date"));
    }

    /**
     * Test LocalTime deserialization with invalid format throws exception
     */
    @Test
    @DisplayName("Should throw exception when LocalTime format is invalid")
    public void testLocalTimeDeserializationWithInvalidFormat() {
        String invalidJson = "{\"localTime\":\"invalid-time-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse local time"));
    }

    /**
     * Test LocalDateTime deserialization with invalid format throws exception
     */
    @Test
    @DisplayName("Should throw exception when LocalDateTime format is invalid")
    public void testLocalDateTimeDeserializationWithInvalidFormat() {
        String invalidJson = "{\"localDateTime\":\"invalid-datetime-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse local date time"));
    }

    @Test
    @DisplayName("Should serialize BigDecimal correctly")
    public void testBigDecimalSerialization() {
        BigDecimalRecord record = new BigDecimalRecord(BigDecimal.valueOf(123.456));
        assertEquals("{\"decimal\":\"123.46\"}", mapper.writeValueAsString(record));
        jsonProperties.setBigDecimalAsString(false);
        assertEquals("{\"decimal\":123.46}", mapper.writeValueAsString(record));
        jsonProperties.setBigDecimalEnabled(false);
        assertEquals("{\"decimal\":123.456}", mapper.writeValueAsString(record));
    }

    @Test
    @DisplayName("Should deserialize BigDecimal correctly")
    public void testBigDecimalDeserialization() {
        assertEquals(new BigDecimal("123.123"), mapper.readValue("{\"decimal\":123.123}", BigDecimalRecord.class).decimal);
        assertEquals(new BigDecimal("123.123"), mapper.readValue("{\"decimal\":\"123.123\"}", BigDecimalRecord.class).decimal);
    }

    @Test
    @DisplayName("Should deserialize Long correctly")
    public void testLongDeserialization() {
        assertEquals(Long.valueOf(1234567890123456789L), mapper.readValue("{\"value\":1234567890123456789}", LongRecord.class).value);
        DatabindException illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":1234567890123456789000000}", LongRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("1234567890123456789000000"));
        illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":-1234567890123456789000000}", LongRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("-1234567890123456789000000"));
    }

    @Test
    @DisplayName("Should deserialize Integer correctly")
    public void testIntegerDeserialization() {
        assertEquals(Integer.valueOf(123), mapper.readValue("{\"value\":123}", IntRecord.class).value);
        DatabindException illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":1234567890123456789000000}", IntRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("1234567890123456789000000"));
        illegalArgumentException = assertThrows(DatabindException.class, () -> mapper.readValue("{\"value\":-1234567890123456789000000}", IntRecord.class));
        assertTrue(illegalArgumentException.getMessage().contains("-1234567890123456789000000"));
    }

    @Test
    @DisplayName("Should serializer double correctly")
    public void testDoubleSerialization() {
        assertEquals("{\"value\":123.12345679}", mapper.writeValueAsString(new DoubleRecord(123.123456789)));
    }

    @Test
    @DisplayName("Should serializer float correctly")
    public void testFloatSerialization() {
        assertEquals("{\"value\":123.12346}", mapper.writeValueAsString(new FloatRecord(123.123456789f)));
    }

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

    public record EnumTestRecord(StatusEnum status) {
    }

    public record TimeRecord(Date date, LocalDateTime localDateTime, LocalDate localDate, LocalTime localTime) {
    }

    public record BigDecimalRecord(BigDecimal decimal) {
    }

    public record LongRecord(Long value) {
    }

    public record IntRecord(Integer value) {
    }

    public record DoubleRecord(Double value) {
    }

    public record FloatRecord(Float value) {
    }
}
