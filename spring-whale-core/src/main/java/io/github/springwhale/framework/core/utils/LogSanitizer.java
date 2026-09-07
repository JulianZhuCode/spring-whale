package io.github.springwhale.framework.core.utils;

/**
 * Sanitizes values before they are written to log records.
 *
 * <p>Prevents <b>log injection</b> (CRLF injection): untrusted input that
 * contains carriage-return or line-feed characters could otherwise forge
 * additional log entries and mislead operators or auditing tools.</p>
 */
public final class LogSanitizer {

    private static final String CRLF_PATTERN = "[\\r\\n]";
    private static final String REPLACEMENT = "_";

    private LogSanitizer() {
    }

    /**
     * Removes CR/LF characters from a string value.
     *
     * @param value the value to sanitize
     * @return the sanitized value, or {@code null} when the input is {@code null}
     */
    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll(CRLF_PATTERN, REPLACEMENT);
    }

    /**
     * Stringifies an arbitrary value and removes CR/LF characters.
     *
     * @param value the value to sanitize
     * @return the sanitized string, or {@code null} when the input is {@code null}
     */
    public static String sanitize(Object value) {
        if (value == null) {
            return null;
        }
        return sanitize(String.valueOf(value));
    }
}
