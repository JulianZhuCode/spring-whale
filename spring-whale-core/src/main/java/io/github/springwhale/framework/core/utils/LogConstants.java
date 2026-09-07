package io.github.springwhale.framework.core.utils;

/**
 * Constants shared by log sanitization call sites.
 */
public final class LogConstants {

    /**
     * Regex matching any Unicode line-break sequence (CRLF, LF, CR, NEL, ...).
     */
    public static final String LINE_BREAKS = "\\R";

    /**
     * Placeholder replacing line breaks in log records.
     */
    public static final String PLACEHOLDER = "_";

    private LogConstants() {
    }
}
