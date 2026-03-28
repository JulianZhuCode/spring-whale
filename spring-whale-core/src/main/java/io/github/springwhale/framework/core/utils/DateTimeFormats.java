package io.github.springwhale.framework.core.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Utility class for date and time formatting
 * Provides common date/time format patterns and utilities for parsing
 */
public class DateTimeFormats {
    
    /**
     * Common date formats for Date/LocalDateTime deserialization
     */
    public static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = Arrays.asList(
        // ISO formats
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        
        // Chinese formats
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒"),
        
        // European formats
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
        
        // US formats
        DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
        DateTimeFormatter.ofPattern("MM.dd.yyyy HH:mm:ss"),
        
        // Short formats
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
        
        // With timezone
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"),
        
        // Without seconds
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    );
    
    /**
     * Common date-only formats for LocalDate deserialization
     */
    public static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
        // ISO format
        DateTimeFormatter.ISO_LOCAL_DATE,
        
        // Chinese formats
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日"),
        
        // European formats
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        
        // US formats
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("MM.dd.yyyy"),
        
        // Short formats
        DateTimeFormatter.ofPattern("yy-MM-dd"),
        DateTimeFormatter.ofPattern("yy/MM/dd"),
        
        // With month name
        DateTimeFormatter.ofPattern("dd MMMM yyyy"),
        DateTimeFormatter.ofPattern("dd MMM yyyy"),
        DateTimeFormatter.ofPattern("MMMM dd, yyyy"),
        DateTimeFormatter.ofPattern("MMM dd, yyyy")
    );
    
    /**
     * Common time-only formats for LocalTime deserialization
     */
    public static final List<DateTimeFormatter> TIME_FORMATTERS = Arrays.asList(
        // ISO format
        DateTimeFormatter.ISO_LOCAL_TIME,
        
        // Standard formats
        DateTimeFormatter.ofPattern("HH:mm:ss"),
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS"),
        
        // Without seconds
        DateTimeFormatter.ofPattern("HH:mm"),
        DateTimeFormatter.ofPattern("H:mm"),
        DateTimeFormatter.ofPattern("hh:mm a"),
        DateTimeFormatter.ofPattern("hh:mm:ss a"),
        
        // With timezone
        DateTimeFormatter.ofPattern("HH:mm:ss Z"),
        DateTimeFormatter.ofPattern("HH:mm:ssXXX")
    );
    
    /**
     * Attempt to parse a date-time string using all common date-time formatters
     * @param text the date-time string to parse
     * @return the parsed LocalDateTime, or null if no formatter matched
     */
    public static java.time.LocalDateTime parseDateTime(String text) {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return java.time.LocalDateTime.parse(text, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        return null;
    }
    
    /**
     * Attempt to parse a date string using all common date formatters
     * @param text the date string to parse
     * @return the parsed LocalDate, or null if no formatter matched
     */
    public static java.time.LocalDate parseDate(String text) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return java.time.LocalDate.parse(text, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        return null;
    }
    
    /**
     * Attempt to parse a time string using all common time formatters
     * @param text the time string to parse
     * @return the parsed LocalTime, or null if no formatter matched
     */
    public static LocalTime parseTime(String text) {
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(text, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        return null;
    }
    
    /**
     * Parse a string to Date by trying multiple formats
     * First attempts numeric timestamp parsing, then falls back to format iteration
     * @param text the date string to parse
     * @return the parsed Date
     * @throws IllegalArgumentException if the string cannot be parsed as any supported format
     */
    public static Date parseDateFromText(String text) {
        // Try to parse as numeric timestamp first
        try {
            long timestamp = Long.parseLong(text);
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            // Not a numeric string, try common date formats using iterator pattern
            for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
                try {
                    java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(text, formatter);
                    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
                } catch (Exception ex) {
                    // Try next formatter
                }
            }
            throw new IllegalArgumentException("Cannot parse date: " + text + 
                ". Supported formats include: yyyy-MM-dd HH:mm:ss, ISO_LOCAL_DATE_TIME, etc.");
        }
    }
    
    /**
     * Parse a string to LocalDate by trying multiple formats
     * First attempts numeric timestamp parsing, then falls back to format iteration
     * @param text the date string to parse
     * @return the parsed LocalDate
     * @throws IllegalArgumentException if the string cannot be parsed as any supported format
     */
    public static LocalDate parseLocalDateFromText(String text) {
        // Try to parse as numeric timestamp first
        try {
            long timestamp = Long.parseLong(text);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalDate();
        } catch (NumberFormatException e) {
            // Not a numeric string, try common date formats using iterator pattern
            LocalDate parsedDate = parseDate(text);
            if (parsedDate != null) {
                return parsedDate;
            }
            throw new IllegalArgumentException("Cannot parse local date: " + text + 
                ". Supported formats include: yyyy-MM-dd, ISO_LOCAL_DATE, dd/MM/yyyy, etc.");
        }
    }
    
    /**
     * Parse a string to LocalTime by trying multiple formats
     * First attempts numeric timestamp parsing, then falls back to format iteration
     * @param text the time string to parse
     * @return the parsed LocalTime
     * @throws IllegalArgumentException if the string cannot be parsed as any supported format
     */
    public static LocalTime parseLocalTimeFromText(String text) {
        // Try to parse as numeric timestamp first
        try {
            long timestamp = Long.parseLong(text);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).toLocalTime();
        } catch (NumberFormatException e) {
            // Not a numeric string, try common time formats using iterator pattern
            LocalTime parsedTime = parseTime(text);
            if (parsedTime != null) {
                return parsedTime;
            }
            throw new IllegalArgumentException("Cannot parse local time: " + text + 
                ". Supported formats include: HH:mm:ss, ISO_LOCAL_TIME, HH:mm, etc.");
        }
    }
    
    /**
     * Parse a string to LocalDateTime by trying multiple formats
     * First attempts numeric timestamp parsing, then falls back to format iteration
     * @param text the date-time string to parse
     * @return the parsed LocalDateTime
     * @throws IllegalArgumentException if the string cannot be parsed as any supported format
     */
    public static LocalDateTime parseLocalDateTimeFromText(String text) {
        // Try to parse as numeric timestamp first
        try {
            long timestamp = Long.parseLong(text);
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        } catch (NumberFormatException e) {
            // Not a numeric string, try common date-time formats using iterator pattern
            LocalDateTime parsedDateTime = parseDateTime(text);
            if (parsedDateTime != null) {
                return parsedDateTime;
            }
            throw new IllegalArgumentException("Cannot parse local date time: " + text + 
                ". Supported formats include: yyyy-MM-dd HH:mm:ss, ISO_LOCAL_DATE_TIME, etc.");
        }
    }
}
