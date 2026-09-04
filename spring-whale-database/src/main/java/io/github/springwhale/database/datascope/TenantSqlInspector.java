package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Hibernate {@link org.hibernate.resource.jdbc.spi.StatementInspector} that injects tenant isolation WHERE
 * conditions into SQL.
 *
 * <p>Supports multiple tenant fields per entity via {@code @TenantIdField},
 * enabling cross-tenant scenarios like {@code tenant_id = 100 OR target_tenant_id = 100}.</p>
 *
 * <p>Controlled by {@code spring.whale.database.datascope.tenant-enabled} and
 * can be skipped per-method via {@code @NonTenant}.</p>
 */
@Slf4j
public class TenantSqlInspector extends SqlInspectorSupport {

    private final DataScopeProperties properties;
    private final DataScopeHandler dataScopeHandler;

    public TenantSqlInspector(DataScopeProperties properties, DataScopeHandler dataScopeHandler) {
        this.properties = properties;
        this.dataScopeHandler = dataScopeHandler;
    }

    @Override
    public String inspect(String sql) {
        if (!properties.isTenantEnabled() || DataScopeContext.isSkipTenant()
                || DataScopeContext.isSkipSqlInspector() || dataScopeHandler.skipTenantScope()) {
            return sql;
        }

        Object tenantId = DataScopeContext.getTenantId();
        Class<?> entityClass = DataScopeContext.getEntityClass();
        List<String> tenantFields = DataScopeContext.getTenantFields();

        if (tenantId == null || entityClass == null || tenantFields == null || tenantFields.isEmpty()) {
            return sql;
        }

        String tableName = resolveTableName(entityClass);
        if (tableName == null) {
            return sql;
        }

        if (!isTargetQuery(sql, tableName)) {
            return sql;
        }

        String condition = buildCondition(tenantId, tenantFields);
        if (condition.isEmpty()) {
            return sql;
        }

        String modifiedSql = applyCondition(sql, condition, tableName);
        log.debug("Tenant filter applied: entity={}, table={}, tenantId={}, condition={}",
                entityClass.getSimpleName(), tableName, tenantId, condition);
        log.trace("Original SQL: {}", sql);
        log.trace("Modified SQL: {}", modifiedSql);
        return modifiedSql;
    }

    private String buildCondition(Object tenantId, List<String> tenantFields) {
        StringBuilder condition = new StringBuilder();
        for (String field : tenantFields) {
            String escaped = escapeIdentifier(field);
            if (escaped == null) {
                continue;
            }
            if (!condition.isEmpty()) {
                condition.append(" OR ");
            }
            condition.append(escaped).append(" = ").append(formatValue(tenantId));
        }
        return condition.toString();
    }
}