package io.github.springwhale.test.json;

import io.github.springwhale.test.json.JsonTestRecords.TimeRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DateTimeJacksonComponentTest extends BaseJacksonTest {

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

    @Test
    @DisplayName("Should serialize LocalDateTime as timestamp when configured")
    public void testLocalDateTimeSerializationAsTimestamp() {
        jsonProperties.setDateTimeFormat("timestamp");
        LocalDateTime dateTime = LocalDateTime.of(2024, 3, 25, 14, 30, 45);
        TimeRecord record = new TimeRecord(null, dateTime, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("localDateTime");

        assertTrue(node.isNumber(), "LocalDateTime should be serialized as timestamp number");
        long timestamp = node.asLong();
        assertEquals(dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), timestamp);
    }

    @Test
    @DisplayName("Should serialize Date with configured format")
    public void testDateSerialization() {
        jsonProperties.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(1711353045000L);
        TimeRecord record = new TimeRecord(date, null, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("date");

        assertNotNull(node.asString(), "Date should be serialized as string");
        assertTrue(node.asString().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "Date should match format yyyy-MM-dd HH:mm:ss");
    }

    @Test
    @DisplayName("Should serialize Date with custom format")
    public void testDateSerializationWithCustomFormat() {
        jsonProperties.setDateTimeFormat("dd/MM/yyyy HH:mm");
        Date date = new Date(1711353045000L);
        TimeRecord record = new TimeRecord(date, null, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("date");

        assertNotNull(node.asString(), "Date should be serialized as string");
        assertTrue(node.asString().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"),
                "Date should match format dd/MM/yyyy HH:mm");
    }

    @Test
    @DisplayName("Should serialize Date as timestamp when configured")
    public void testDateSerializationAsTimestamp() {
        jsonProperties.setDateTimeFormat("timestamp");
        Date date = new Date(1711353045000L);
        TimeRecord record = new TimeRecord(date, null, null, null);

        String json = mapper.writeValueAsString(record);
        JsonNode node = mapper.readTree(json).get("date");

        assertTrue(node.isNumber(), "Date should be serialized as timestamp number");
        long timestamp = node.asLong();
        assertEquals(1711353045000L, timestamp);
    }

    @Test
    @DisplayName("Should deserialize Date from multiple supported formats")
    public void testDateDeserializationWithMultipleFormats() {
        String isoJson = "{\"date\":\"2024-03-25T14:30:45\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.date());
        assertEquals(2024, record.date().toInstant().atZone(ZoneId.systemDefault()).getYear());
        assertEquals(3, record.date().toInstant().atZone(ZoneId.systemDefault()).getMonthValue());
        assertEquals(25, record.date().toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth());

        String chineseJson = "{\"date\":\"2024/03/25 14:30:45\"}";
        record = mapper.readValue(chineseJson, TimeRecord.class);
        assertNotNull(record.date());

        String europeanJson = "{\"date\":\"25-03-2024 14:30:45\"}";
        record = mapper.readValue(europeanJson, TimeRecord.class);
        assertNotNull(record.date());

        String timestampJson = "{\"date\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.date());
        assertEquals(1711353045000L, record.date().getTime());

        String timestampStringJson = "{\"date\":\"1711353045000\"}";
        record = mapper.readValue(timestampStringJson, TimeRecord.class);
        assertNotNull(record.date());
        assertEquals(1711353045000L, record.date().getTime());
    }

    @Test
    @DisplayName("Should deserialize LocalDate from multiple supported formats")
    public void testLocalDateDeserializationWithMultipleFormats() {
        String isoJson = "{\"localDate\":\"2024-03-25\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.localDate());
        assertEquals(2024, record.localDate().getYear());
        assertEquals(3, record.localDate().getMonthValue());
        assertEquals(25, record.localDate().getDayOfMonth());

        String chineseJson = "{\"localDate\":\"2024/03/25\"}";
        record = mapper.readValue(chineseJson, TimeRecord.class);
        assertNotNull(record.localDate());

        String europeanJson = "{\"localDate\":\"25-03-2024\"}";
        record = mapper.readValue(europeanJson, TimeRecord.class);
        assertNotNull(record.localDate());

        String usJson = "{\"localDate\":\"03/25/2024\"}";
        record = mapper.readValue(usJson, TimeRecord.class);
        assertNotNull(record.localDate());

        String timestampJson = "{\"localDate\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.localDate());
    }

    @Test
    @DisplayName("Should deserialize LocalTime from multiple supported formats")
    public void testLocalTimeDeserializationWithMultipleFormats() {
        String isoJson = "{\"localTime\":\"14:30:45\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.localTime());
        assertEquals(14, record.localTime().getHour());
        assertEquals(30, record.localTime().getMinute());
        assertEquals(45, record.localTime().getSecond());

        String noSecondsJson = "{\"localTime\":\"14:30\"}";
        record = mapper.readValue(noSecondsJson, TimeRecord.class);
        assertNotNull(record.localTime());
        assertEquals(14, record.localTime().getHour());
        assertEquals(30, record.localTime().getMinute());

        String withMillisJson = "{\"localTime\":\"14:30:45.123\"}";
        record = mapper.readValue(withMillisJson, TimeRecord.class);
        assertNotNull(record.localTime());
        assertEquals(14, record.localTime().getHour());
        assertEquals(30, record.localTime().getMinute());

        String timestampJson = "{\"localTime\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.localTime());
    }

    @Test
    @DisplayName("Should deserialize LocalDateTime from multiple supported formats")
    public void testLocalDateTimeDeserializationWithMultipleFormats() {
        String isoJson = "{\"localDateTime\":\"2024-03-25T14:30:45\"}";
        TimeRecord record = mapper.readValue(isoJson, TimeRecord.class);
        assertNotNull(record.localDateTime());
        assertEquals(2024, record.localDateTime().getYear());
        assertEquals(3, record.localDateTime().getMonthValue());
        assertEquals(25, record.localDateTime().getDayOfMonth());
        assertEquals(14, record.localDateTime().getHour());
        assertEquals(30, record.localDateTime().getMinute());

        String chineseJson = "{\"localDateTime\":\"2024/03/25 14:30:45\"}";
        record = mapper.readValue(chineseJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        String europeanJson = "{\"localDateTime\":\"25-03-2024 14:30:45\"}";
        record = mapper.readValue(europeanJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        String noSecondsJson = "{\"localDateTime\":\"2024-03-25 14:30\"}";
        record = mapper.readValue(noSecondsJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        String timestampJson = "{\"localDateTime\":1711353045000}";
        record = mapper.readValue(timestampJson, TimeRecord.class);
        assertNotNull(record.localDateTime());

        String timestampStringJson = "{\"localDateTime\":\"1711353045000\"}";
        record = mapper.readValue(timestampStringJson, TimeRecord.class);
        assertNotNull(record.localDateTime());
    }

    @Test
    @DisplayName("Should throw exception when Date format is invalid")
    public void testDateDeserializationWithInvalidFormat() {
        String invalidJson = "{\"date\":\"invalid-date-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse date"));
    }

    @Test
    @DisplayName("Should throw exception when LocalDate format is invalid")
    public void testLocalDateDeserializationWithInvalidFormat() {
        String invalidJson = "{\"localDate\":\"invalid-date-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse local date"));
    }

    @Test
    @DisplayName("Should throw exception when LocalTime format is invalid")
    public void testLocalTimeDeserializationWithInvalidFormat() {
        String invalidJson = "{\"localTime\":\"invalid-time-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse local time"));
    }

    @Test
    @DisplayName("Should throw exception when LocalDateTime format is invalid")
    public void testLocalDateTimeDeserializationWithInvalidFormat() {
        String invalidJson = "{\"localDateTime\":\"invalid-datetime-format\"}";

        Exception exception = assertThrows(Exception.class, () -> mapper.readValue(invalidJson, TimeRecord.class));

        assertTrue(exception.getMessage().contains("Cannot parse local date time"));
    }

}