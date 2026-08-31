package io.github.springwhale.database.datascope;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.List;

/**
 * Composite {@link StatementInspector} that chains multiple inspectors sequentially.
 * Each inspector transforms the SQL and passes the result to the next.
 *
 * <p>Order: {@code TenantSqlInspector} → {@code DataScopeInterceptor},
 * ensuring tenant isolation conditions are applied before data scope conditions.</p>
 */
public class CompositeStatementInspector implements StatementInspector {

    private final List<StatementInspector> inspectors;

    public CompositeStatementInspector(List<StatementInspector> inspectors) {
        this.inspectors = inspectors;
    }

    @Override
    public String inspect(String sql) {
        for (StatementInspector inspector : inspectors) {
            sql = inspector.inspect(sql);
        }
        return sql;
    }
}