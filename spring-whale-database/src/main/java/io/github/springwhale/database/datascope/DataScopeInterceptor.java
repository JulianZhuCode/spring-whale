package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Hibernate {@link StatementInspector} that injects data scope WHERE conditions
 * into SQL based on the current {@link DataScopeContext}.
 *
 * <p>Reads the scope, entity class, and field mappings from {@link DataScopeContext}
 * and appends conditions like {@code (dept_id IN (1,2,3) OR user_id = 5)}.
 * Supports multiple dept/user fields on the same entity via {@code @DeptIdField}
 * and {@code @UserIdField} annotations.</p>
 */
@Slf4j
public class DataScopeInterceptor extends SqlInspectorSupport {

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
}