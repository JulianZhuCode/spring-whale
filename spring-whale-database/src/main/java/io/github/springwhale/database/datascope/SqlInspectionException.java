package io.github.springwhale.database.datascope;

/**
 * Thrown when SQL inspection fails — either because the SQL cannot be parsed
 * or because the statement type is not supported for WHERE injection.
 *
 * <p>This implements <strong>fail-closed</strong> semantics: instead of
 * silently allowing unfiltered SQL to execute (fail-open), the statement
 * is rejected with an exception, preventing data leaks.</p>
 */
public class SqlInspectionException extends RuntimeException {

    public SqlInspectionException(String message) {
        super(message);
    }

    public SqlInspectionException(String message, Throwable cause) {
        super(message, cause);
    }
}