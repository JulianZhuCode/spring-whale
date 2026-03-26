package io.github.springwhale.test.utils;

import io.github.springwhale.framework.core.utils.DateTimeFormats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DateTimeFormats utility class
 */
@DisplayName("DateTimeFormats Utility Tests")
public class DateTimeFormatsTest {

    @Test
    @DisplayName("Should parse Date from numeric timestamp")
    public void testParseDateFromNumericTimestamp() {
        long timestamp = 1711353045000L; // 2024-03-25 14:30:45 UTC
        Date date = DateTimeFormats.parseDateFromText(String.valueOf(timestamp));
        
        assertNotNull(date);
        assertEquals(timestamp, date.getTime());
    }

    @Test
    @DisplayName("Should parse Date from numeric string timestamp")
    public void testParseDateFromNumericStringTimestamp() {
        String timestampString = "1711353045000";
        Date date = DateTimeFormats.parseDateFromText(timestampString);
        
        assertNotNull(date);
        assertEquals(1711353045000L, date.getTime());
    }

    @Test
    @DisplayName("Should parse Date from ISO format string")
    public void testParseDateFromISOFormat() {
        String isoFormat = "2024-03-25T14:30:45";
        Date date = DateTimeFormats.parseDateFromText(isoFormat);
        
        assertNotNull(date);
        assertEquals(2024, date.toInstant().atZone(java.time.ZoneId.systemDefault()).getYear());
        assertEquals(3, date.toInstant().atZone(java.time.ZoneId.systemDefault()).getMonthValue());
        assertEquals(25, date.toInstant().atZone(java.time.ZoneId.systemDefault()).getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse Date from Chinese format")
    public void testParseDateFromChineseFormat() {
        String chineseFormat = "2024/03/25 14:30:45";
        Date date = DateTimeFormats.parseDateFromText(chineseFormat);
        
        assertNotNull(date);
    }

    @Test
    @DisplayName("Should parse Date from European format")
    public void testParseDateFromEuropeanFormat() {
        String europeanFormat = "25-03-2024 14:30:45";
        Date date = DateTimeFormats.parseDateFromText(europeanFormat);
        
        assertNotNull(date);
    }

    @Test
    @DisplayName("Should throw exception when Date format is invalid")
    public void testParseDateWithInvalidFormat() {
        String invalidFormat = "invalid-date-format";
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DateTimeFormats.parseDateFromText(invalidFormat);
        });
        
        assertTrue(exception.getMessage().contains("Cannot parse date"));
    }

    @Test
    @DisplayName("Should parse LocalDate from numeric timestamp")
    public void testParseLocalDateFromNumericTimestamp() {
        long timestamp = 1711353045000L;
        LocalDate localDate = DateTimeFormats.parseLocalDateFromText(String.valueOf(timestamp));
        
        assertNotNull(localDate);
        assertEquals(2024, localDate.getYear());
        assertEquals(3, localDate.getMonthValue());
        assertEquals(25, localDate.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDate from numeric string timestamp")
    public void testParseLocalDateFromNumericStringTimestamp() {
        String timestampString = "1711353045000";
        LocalDate localDate = DateTimeFormats.parseLocalDateFromText(timestampString);
        
        assertNotNull(localDate);
        assertEquals(2024, localDate.getYear());
        assertEquals(3, localDate.getMonthValue());
        assertEquals(25, localDate.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDate from ISO format")
    public void testParseLocalDateFromISOFormat() {
        String isoFormat = "2024-03-25";
        LocalDate localDate = DateTimeFormats.parseLocalDateFromText(isoFormat);
        
        assertNotNull(localDate);
        assertEquals(2024, localDate.getYear());
        assertEquals(3, localDate.getMonthValue());
        assertEquals(25, localDate.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDate from Chinese format")
    public void testParseLocalDateFromChineseFormat() {
        String chineseFormat = "2024/03/25";
        LocalDate localDate = DateTimeFormats.parseLocalDateFromText(chineseFormat);
        
        assertNotNull(localDate);
        assertEquals(2024, localDate.getYear());
        assertEquals(3, localDate.getMonthValue());
        assertEquals(25, localDate.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDate from European format")
    public void testParseLocalDateFromEuropeanFormat() {
        String europeanFormat = "25-03-2024";
        LocalDate localDate = DateTimeFormats.parseLocalDateFromText(europeanFormat);
        
        assertNotNull(localDate);
        assertEquals(2024, localDate.getYear());
        assertEquals(3, localDate.getMonthValue());
        assertEquals(25, localDate.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDate from US format")
    public void testParseLocalDateFromUSFormat() {
        String usFormat = "03/25/2024";
        LocalDate localDate = DateTimeFormats.parseLocalDateFromText(usFormat);
        
        assertNotNull(localDate);
        assertEquals(2024, localDate.getYear());
        assertEquals(3, localDate.getMonthValue());
        assertEquals(25, localDate.getDayOfMonth());
    }

    @Test
    @DisplayName("Should throw exception when LocalDate format is invalid")
    public void testParseLocalDateWithInvalidFormat() {
        String invalidFormat = "invalid-date";
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DateTimeFormats.parseLocalDateFromText(invalidFormat);
        });
        
        assertTrue(exception.getMessage().contains("Cannot parse local date"));
    }

    @Test
    @DisplayName("Should parse LocalTime from numeric timestamp")
    public void testParseLocalTimeFromNumericTimestamp() {
        long timestamp = 1711353045000L;
        LocalTime localTime = DateTimeFormats.parseLocalTimeFromText(String.valueOf(timestamp));
        
        assertNotNull(localTime);
    }

    @Test
    @DisplayName("Should parse LocalTime from numeric string timestamp")
    public void testParseLocalTimeFromNumericStringTimestamp() {
        String timestampString = "1711353045000";
        LocalTime localTime = DateTimeFormats.parseLocalTimeFromText(timestampString);
        
        assertNotNull(localTime);
    }

    @Test
    @DisplayName("Should parse LocalTime from ISO format")
    public void testParseLocalTimeFromISOFormat() {
        String isoFormat = "14:30:45";
        LocalTime localTime = DateTimeFormats.parseLocalTimeFromText(isoFormat);
        
        assertNotNull(localTime);
        assertEquals(14, localTime.getHour());
        assertEquals(30, localTime.getMinute());
        assertEquals(45, localTime.getSecond());
    }

    @Test
    @DisplayName("Should parse LocalTime without seconds")
    public void testParseLocalTimeWithoutSeconds() {
        String noSecondsFormat = "14:30";
        LocalTime localTime = DateTimeFormats.parseLocalTimeFromText(noSecondsFormat);
        
        assertNotNull(localTime);
        assertEquals(14, localTime.getHour());
        assertEquals(30, localTime.getMinute());
    }

    @Test
    @DisplayName("Should parse LocalTime with milliseconds")
    public void testParseLocalTimeWithMilliseconds() {
        String withMillisFormat = "14:30:45.123";
        LocalTime localTime = DateTimeFormats.parseLocalTimeFromText(withMillisFormat);
        
        assertNotNull(localTime);
        assertEquals(14, localTime.getHour());
        assertEquals(30, localTime.getMinute());
    }

    @Test
    @DisplayName("Should throw exception when LocalTime format is invalid")
    public void testParseLocalTimeWithInvalidFormat() {
        String invalidFormat = "invalid-time";
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DateTimeFormats.parseLocalTimeFromText(invalidFormat);
        });
        
        assertTrue(exception.getMessage().contains("Cannot parse local time"));
    }

    @Test
    @DisplayName("Should parse LocalDateTime from numeric timestamp")
    public void testParseLocalDateTimeFromNumericTimestamp() {
        long timestamp = 1711353045000L;
        LocalDateTime localDateTime = DateTimeFormats.parseLocalDateTimeFromText(String.valueOf(timestamp));
        
        assertNotNull(localDateTime);
        assertEquals(2024, localDateTime.getYear());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(25, localDateTime.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDateTime from numeric string timestamp")
    public void testParseLocalDateTimeFromNumericStringTimestamp() {
        String timestampString = "1711353045000";
        LocalDateTime localDateTime = DateTimeFormats.parseLocalDateTimeFromText(timestampString);
        
        assertNotNull(localDateTime);
        assertEquals(2024, localDateTime.getYear());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(25, localDateTime.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDateTime from ISO format")
    public void testParseLocalDateTimeFromISOFormat() {
        String isoFormat = "2024-03-25T14:30:45";
        LocalDateTime localDateTime = DateTimeFormats.parseLocalDateTimeFromText(isoFormat);
        
        assertNotNull(localDateTime);
        assertEquals(2024, localDateTime.getYear());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(25, localDateTime.getDayOfMonth());
        assertEquals(14, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
    }

    @Test
    @DisplayName("Should parse LocalDateTime from Chinese format")
    public void testParseLocalDateTimeFromChineseFormat() {
        String chineseFormat = "2024/03/25 14:30:45";
        LocalDateTime localDateTime = DateTimeFormats.parseLocalDateTimeFromText(chineseFormat);
        
        assertNotNull(localDateTime);
        assertEquals(2024, localDateTime.getYear());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(25, localDateTime.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDateTime from European format")
    public void testParseLocalDateTimeFromEuropeanFormat() {
        String europeanFormat = "25-03-2024 14:30:45";
        LocalDateTime localDateTime = DateTimeFormats.parseLocalDateTimeFromText(europeanFormat);
        
        assertNotNull(localDateTime);
        assertEquals(2024, localDateTime.getYear());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(25, localDateTime.getDayOfMonth());
    }

    @Test
    @DisplayName("Should parse LocalDateTime without seconds")
    public void testParseLocalDateTimeWithoutSeconds() {
        String noSecondsFormat = "2024-03-25 14:30";
        LocalDateTime localDateTime = DateTimeFormats.parseLocalDateTimeFromText(noSecondsFormat);
        
        assertNotNull(localDateTime);
        assertEquals(2024, localDateTime.getYear());
        assertEquals(3, localDateTime.getMonthValue());
        assertEquals(25, localDateTime.getDayOfMonth());
        assertEquals(14, localDateTime.getHour());
        assertEquals(30, localDateTime.getMinute());
    }

    @Test
    @DisplayName("Should throw exception when LocalDateTime format is invalid")
    public void testParseLocalDateTimeWithInvalidFormat() {
        String invalidFormat = "invalid-datetime";
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DateTimeFormats.parseLocalDateTimeFromText(invalidFormat);
        });
        
        assertTrue(exception.getMessage().contains("Cannot parse local date time"));
    }

    @Test
    @DisplayName("Should parse null when no format matches for LocalDate")
    public void testParseDateReturnsNullWhenNoMatch() {
        LocalDate result = DateTimeFormats.parseDate("invalid-format");
        assertNull(result);
    }

    @Test
    @DisplayName("Should parse null when no format matches for LocalTime")
    public void testParseTimeReturnsNullWhenNoMatch() {
        LocalTime result = DateTimeFormats.parseTime("invalid-format");
        assertNull(result);
    }

    @Test
    @DisplayName("Should parse null when no format matches for LocalDateTime")
    public void testParseDateTimeReturnsNullWhenNoMatch() {
        LocalDateTime result = DateTimeFormats.parseDateTime("invalid-format");
        assertNull(result);
    }
}
