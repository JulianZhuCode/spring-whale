package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DataScopeInterceptor implements StatementInspector {

    private static final Pattern FROM_PATTERN = Pattern.compile(
            "\\bFROM\\s+([a-zA-Z0-9_.]+)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern WHERE_PATTERN = Pattern.compile(
            "\\bWHERE\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SELECT_PATTERN = Pattern.compile(
            "^\\s*(SELECT|UPDATE|DELETE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern CLAUSE_PATTERN = Pattern.compile(
            "\\b(GROUP\\s+BY|HAVING|ORDER\\s+BY|LIMIT|OFFSET|FOR\\s+UPDATE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private static final String SQL_INJECTION_CHARS = "';\";\\--";

    @Override
    public String inspect(String sql) {
        DataScopeResult scope = DataScopeContext.getScope();
        Class<?> entityClass = DataScopeContext.getEntityClass();
        List<String> deptFields = DataScopeContext.getDeptFields();
        List<String> userFields = DataScopeContext.getUserFields();

        if (scope == null || scope.isEmpty() || entityClass == null) {
            return sql;
        }

        String tableName = resolveTableName(entityClass);
        if (tableName == null) {
            return sql;
        }

        if (!isTargetQuery(sql, tableName)) {
            return sql;
        }

        String condition = buildCondition(scope, deptFields, userFields);
        if (condition.isEmpty()) {
            return sql;
        }

        String modifiedSql = applyCondition(sql, condition);
        log.debug("Data scope applied: entity={}, table={}, condition={}",
                entityClass.getSimpleName(), tableName, condition);
        log.trace("Original SQL: {}", sql);
        log.trace("Modified SQL: {}", modifiedSql);
        return modifiedSql;
    }

    private boolean isTargetQuery(String sql, String tableName) {
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

    private String resolveTableName(Class<?> entityClass) {
        jakarta.persistence.Table tableAnnotation = entityClass.getAnnotation(jakarta.persistence.Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return camelToSnake(entityClass.getSimpleName());
    }

    private String camelToSnake(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0 && !Character.isUpperCase(name.charAt(i - 1))) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private String buildCondition(DataScopeResult scope, List<String> deptFields, List<String> userFields) {
        StringBuilder condition = new StringBuilder();

        if (scope.hasDeptIds() && deptFields != null && !deptFields.isEmpty()) {
            StringBuilder deptCondition = new StringBuilder();
            for (String field : deptFields) {
                String escaped = escapeIdentifier(field);
                if (escaped == null) {
                    continue;
                }
                if (!deptCondition.isEmpty()) {
                    deptCondition.append(" OR ");
                }
                deptCondition.append(escaped);
                deptCondition.append(" IN (").append(buildInClause(scope.getDeptIds())).append(")");
            }
            if (!deptCondition.isEmpty()) {
                condition.append("(").append(deptCondition).append(")");
            }
        }

        if (scope.hasUserId() && userFields != null && !userFields.isEmpty()) {
            StringBuilder userCondition = new StringBuilder();
            for (String field : userFields) {
                String escaped = escapeIdentifier(field);
                if (escaped == null) {
                    continue;
                }
                if (!userCondition.isEmpty()) {
                    userCondition.append(" OR ");
                }
                userCondition.append(escaped);
                userCondition.append(" = ").append(formatValue(scope.getUserId()));
            }
            if (!userCondition.isEmpty()) {
                if (!condition.isEmpty()) {
                    condition.append(" OR ");
                }
                condition.append("(").append(userCondition).append(")");
            }
        }

        return condition.toString();
    }

    private String escapeIdentifier(String fieldName) {
        if (fieldName == null || !IDENTIFIER_PATTERN.matcher(fieldName).matches()) {
            log.warn("Unsafe field name detected: {}, skipping data scope condition", fieldName);
            return null;
        }
        return "`" + fieldName + "`";
    }

    private String formatValue(Object value) {
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

    private String escapeString(String value) {
        return value.replace("'", "''");
    }

    private boolean containsSqlInjectionChars(String value) {
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

    private String buildInClause(List<Object> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(formatValue(values.get(i)));
        }
        return sb.toString();
    }

    private String applyCondition(String sql, String condition) {
        Matcher selectMatcher = SELECT_PATTERN.matcher(sql);
        if (!selectMatcher.find()) {
            return sql;
        }

        String sqlUpper = sql.toUpperCase();
        int whereIndex = findWhereClause(sqlUpper);

        if (whereIndex > 0) {
            int afterWhere = whereIndex + "WHERE".length();
            return sql.substring(0, afterWhere) + " (" + condition + ") AND " + sql.substring(afterWhere);
        }

        int clauseIndex = findClauseStart(sqlUpper);
        if (clauseIndex > 0) {
            return sql.substring(0, clauseIndex) + " WHERE " + condition + " " + sql.substring(clauseIndex);
        }

        return sql + " WHERE " + condition;
    }

    private int findWhereClause(String sqlUpper) {
        Matcher matcher = WHERE_PATTERN.matcher(sqlUpper);
        int lastMatch = -1;
        while (matcher.find()) {
            lastMatch = matcher.start();
        }
        return lastMatch;
    }

    private int findClauseStart(String sqlUpper) {
        Matcher matcher = CLAUSE_PATTERN.matcher(sqlUpper);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }
}