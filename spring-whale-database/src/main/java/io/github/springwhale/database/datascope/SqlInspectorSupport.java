package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for Hibernate {@link StatementInspector} implementations that inject
 * WHERE conditions into SQL statements for data scope and tenant isolation.
 *
 * <h3>SQL safety</h3>
 * All dynamically-injected values are validated against SQL injection patterns.
 * Field names are checked against {@link #IDENTIFIER_PATTERN} and values are
 * checked for injection characters before being embedded in SQL.
 *
 * <h3>WHERE condition placement</h3>
 * When a WHERE clause already exists, the injected condition is AND-ed with the
 * original wrapped in parentheses: {@code WHERE (injected) AND (original)}.
 * This prevents OR conditions in the original SQL from bypassing the filter.
 */
@Slf4j
public abstract class SqlInspectorSupport implements StatementInspector {

    protected static final Pattern FROM_PATTERN = Pattern.compile(
            "\\bFROM\\s+([a-zA-Z0-9_.]+)",
            Pattern.CASE_INSENSITIVE
    );

    protected static final Pattern WHERE_PATTERN = Pattern.compile(
            "\\bWHERE\\b",
            Pattern.CASE_INSENSITIVE
    );

    protected static final Pattern SELECT_PATTERN = Pattern.compile(
            "^\\s*(SELECT|UPDATE|DELETE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    protected static final Pattern CLAUSE_PATTERN = Pattern.compile(
            "\\b(GROUP\\s+BY|HAVING|ORDER\\s+BY|LIMIT|OFFSET|FOR\\s+UPDATE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    protected static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    protected static final String SQL_INJECTION_CHARS = "';\";\\--";

    protected boolean isTargetQuery(String sql, String tableName) {
        Matcher fromMatcher = FROM_PATTERN.matcher(sql);
        while (fromMatcher.find()) {
            String fromTable = fromMatcher.group(1).trim();
            if (fromTable.equalsIgnoreCase(tableName) ||
                    fromTable.endsWith("." + tableName)) {
                return true;
            }
        }
        return false;
    }

    protected String resolveTableName(Class<?> entityClass) {
        jakarta.persistence.Table tableAnnotation = entityClass.getAnnotation(jakarta.persistence.Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return DataScopeHelper.camelToSnake(entityClass.getSimpleName());
    }

    protected String applyCondition(String sql, String condition) {
        Matcher selectMatcher = SELECT_PATTERN.matcher(sql);
        if (!selectMatcher.find()) {
            return sql;
        }

        String sqlUpper = sql.toUpperCase();
        int whereIndex = findWhereClause(sqlUpper);

        if (whereIndex > 0) {
            int afterWhere = whereIndex + "WHERE".length();
            return sql.substring(0, afterWhere) + " (" + condition + ") AND (" + sql.substring(afterWhere) + ")";
        }

        int clauseIndex = findClauseStart(sqlUpper);
        if (clauseIndex > 0) {
            return sql.substring(0, clauseIndex) + " WHERE " + condition + " " + sql.substring(clauseIndex);
        }

        return sql + " WHERE " + condition;
    }

    protected int findWhereClause(String sqlUpper) {
        Matcher matcher = WHERE_PATTERN.matcher(sqlUpper);
        int lastMatch = -1;
        while (matcher.find()) {
            lastMatch = matcher.start();
        }
        return lastMatch;
    }

    protected int findClauseStart(String sqlUpper) {
        Matcher matcher = CLAUSE_PATTERN.matcher(sqlUpper);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    protected String escapeIdentifier(String fieldName) {
        if (fieldName == null || !IDENTIFIER_PATTERN.matcher(fieldName).matches()) {
            log.warn("Unsafe field name detected: {}, skipping condition", fieldName);
            return null;
        }
        return fieldName;
    }

    protected String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        String strValue = value.toString();
        if (containsSqlInjectionChars(strValue)) {
            log.warn("Potential SQL injection in value: {}, returning NULL", strValue);
            return "NULL";
        }
        return "'" + escapeString(strValue) + "'";
    }

    protected String escapeString(String value) {
        return value.replace("'", "''");
    }

    protected boolean containsSqlInjectionChars(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < SQL_INJECTION_CHARS.length(); i++) {
            if (value.indexOf(SQL_INJECTION_CHARS.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }
}