package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Base class for Hibernate {@link StatementInspector} implementations that inject
 * WHERE conditions into SQL statements using AST-level analysis via JSqlParser.
 *
 * <h3>SQL safety</h3>
 * Unlike regex-based approaches, this uses JSqlParser to parse SQL into an AST
 * and inject conditions at the correct syntactic position. Unparseable SQL is
 * <strong>rejected</strong> (fail-closed) rather than silently passed through.
 *
 * <h3>Supported statement types</h3>
 * <ul>
 *   <li>SELECT — including CTE (WITH), UNION, subqueries, JOINs</li>
 *   <li>UPDATE — bulk updates with WHERE injection</li>
 *   <li>DELETE — bulk deletes with WHERE injection</li>
 * </ul>
 *
 * <h3>WHERE condition placement</h3>
 * Conditions are injected at the AST level into the correct {@code PlainSelect}
 * node. For UNION, each branch receives the condition. For CTE, the condition
 * is injected into the CTE definition if it references the target table, and
 * into the main query if it references the target table.
 *
 * <h3>String literal safety</h3>
 * JSqlParser correctly distinguishes SQL keywords from string literals, so
 * {@code WHERE note = 'where is x'} does not cause false-positive WHERE matching.
 */
@Slf4j
public abstract class SqlInspectorSupport implements StatementInspector {

    protected static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    protected static final String SQL_INJECTION_CHARS = "';\";\\--";

    /**
     * Check if the SQL statement references the target table.
     * Uses JSqlParser's {@link TablesNamesFinder} for accurate table name extraction.
     */
    protected boolean isTargetQuery(String sql, String tableName) {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            TablesNamesFinder finder = new TablesNamesFinder();
            List<String> tables = finder.getTableList(stmt);
            return tables.stream().anyMatch(t ->
                    t.equalsIgnoreCase(tableName) || t.endsWith("." + tableName));
        } catch (JSQLParserException e) {
            log.error("Failed to parse SQL for table check: {}", sql, e);
            throw new SqlInspectionException("Unable to parse SQL statement for table check", e);
        }
    }

    protected String resolveTableName(Class<?> entityClass) {
        jakarta.persistence.Table tableAnnotation = entityClass.getAnnotation(jakarta.persistence.Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return DataScopeHelper.camelToSnake(entityClass.getSimpleName());
    }

    /**
     * Inject a WHERE condition into the SQL at the correct AST position.
     *
     * <p>Handles SELECT (CTE/UNION/subqueries), UPDATE, and DELETE.
     * Throws {@link SqlInspectionException} if the SQL cannot be parsed
     * or the injection point cannot be found (fail-closed).</p>
     */
    protected String applyCondition(String sql, String condition, String tableName) {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            Expression parsedCondition = CCJSqlParserUtil.parseExpression(condition);

            boolean modified = false;
            if (stmt instanceof Select select) {
                modified = injectIntoSelect(select, parsedCondition, tableName);
            } else if (stmt instanceof Update update) {
                modified = injectIntoUpdate(update, parsedCondition, tableName);
            } else if (stmt instanceof Delete delete) {
                modified = injectIntoDelete(delete, parsedCondition, tableName);
            } else {
                log.error("Unsupported statement type for SQL inspection: {}", stmt.getClass().getSimpleName());
                throw new SqlInspectionException(
                        "Unsupported SQL statement type: " + stmt.getClass().getSimpleName());
            }

            if (!modified) {
                log.error("Could not find injection point for table '{}' in SQL: {}", tableName, sql);
                throw new SqlInspectionException(
                        "Failed to locate injection point for table '" + tableName + "'");
            }

            return stmt.toString();
        } catch (JSQLParserException e) {
            log.error("Failed to parse SQL for inspection, rejecting statement: {}", sql, e);
            throw new SqlInspectionException(
                    "Unable to parse SQL for data scope/tenant filtering", e);
        }
    }

    /**
     * Inject condition into a SELECT statement, handling CTE and UNION.
     */
    private boolean injectIntoSelect(Select select, Expression condition, String tableName) {
        boolean modified = false;

        if (select.getWithItemsList() != null) {
            for (WithItem<?> withItem : select.getWithItemsList()) {
                ParenthesedSelect ps = withItem.getSelect();
                if (ps != null && ps.getSelect() != null) {
                    if (injectIntoSelectBody(ps.getSelect(), condition, tableName)) {
                        modified = true;
                    }
                }
            }
        }

        if (injectIntoSelectBody(select, condition, tableName)) {
            modified = true;
        }

        return modified;
    }

    /**
     * Recursively inject condition into a Select, handling UNION and subqueries.
     * In JSqlParser 5.x, {@link Select} is the base class for {@link PlainSelect}
     * and {@link SetOperationList}.
     */
    private boolean injectIntoSelectBody(Select select, Expression condition, String tableName) {
        if (select instanceof PlainSelect plainSelect) {
            String alias = resolveTableAlias(plainSelect, tableName);
            if (alias != null) {
                injectIntoPlainSelect(plainSelect, condition, alias);
                return true;
            }
        } else if (select instanceof SetOperationList setOpList) {
            boolean modified = false;
            if (setOpList.getSelects() != null) {
                for (Select body : setOpList.getSelects()) {
                    if (injectIntoSelectBody(body, condition, tableName)) {
                        modified = true;
                    }
                }
            }
            return modified;
        }
        return false;
    }

    /**
     * Inject condition into a PlainSelect's WHERE clause, wrapping existing
     * conditions in parentheses to prevent OR-based bypass.
     *
     * <p>When the target table has an alias (e.g. in JOIN scenarios), columns
     * in the condition are qualified with the alias to prevent "ambiguous column"
     * errors in databases like PostgreSQL.</p>
     */
    private void injectIntoPlainSelect(PlainSelect plainSelect, Expression condition, String alias) {
        Expression qualified = qualifyColumns(condition, alias);
        Expression existingWhere = plainSelect.getWhere();
        if (existingWhere != null) {
            plainSelect.setWhere(new AndExpression(qualified, new ParenthesedExpressionList<>(existingWhere)));
        } else {
            plainSelect.setWhere(qualified);
        }
    }

    /**
     * Inject condition into an UPDATE statement's WHERE clause.
     */
    private boolean injectIntoUpdate(Update update, Expression condition, String tableName) {
        String updateTable = update.getTable().getName();
        if (!updateTable.equalsIgnoreCase(tableName)
                && !updateTable.endsWith("." + tableName)) {
            return false;
        }
        Expression existingWhere = update.getWhere();
        if (existingWhere != null) {
            update.setWhere(new AndExpression(condition, new ParenthesedExpressionList<>(existingWhere)));
        } else {
            update.setWhere(condition);
        }
        return true;
    }

    /**
     * Inject condition into a DELETE statement's WHERE clause.
     */
    private boolean injectIntoDelete(Delete delete, Expression condition, String tableName) {
        String deleteTable = delete.getTable().getName();
        if (!deleteTable.equalsIgnoreCase(tableName)
                && !deleteTable.endsWith("." + tableName)) {
            return false;
        }
        Expression existingWhere = delete.getWhere();
        if (existingWhere != null) {
            delete.setWhere(new AndExpression(condition, new ParenthesedExpressionList<>(existingWhere)));
        } else {
            delete.setWhere(condition);
        }
        return true;
    }

    /**
     * Resolve the alias used for the target table in a PlainSelect's FROM clause.
     * Checks the main FROM item and all JOIN items.
     *
     * @return the alias (or table name if no alias), or {@code null} if the table
     * is not referenced in this PlainSelect
     */
    private String resolveTableAlias(PlainSelect plainSelect, String tableName) {
        String alias = resolveFromItemAlias(plainSelect.getFromItem(), tableName);
        if (alias != null) {
            return alias;
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                alias = resolveFromItemAlias(join.getFromItem(), tableName);
                if (alias != null) {
                    return alias;
                }
            }
        }

        return null;
    }

    /**
     * Check if a FromItem references the target table and return its alias.
     * Returns the alias if present, otherwise the table name itself.
     */
    private String resolveFromItemAlias(FromItem fromItem, String tableName) {
        if (fromItem == null) {
            return null;
        }

        Table table = null;
        String alias = null;

        if (fromItem instanceof Table t) {
            table = t;
            alias = t.getAlias() != null ? t.getAlias().getName() : null;
        } else {
            // For sub-selects etc., check if the alias matches the table name
            alias = fromItem.getAlias() != null ? fromItem.getAlias().getName() : null;
            String itemName = fromItem.toString().toLowerCase();
            if (itemName.contains(tableName.toLowerCase())) {
                return alias != null ? alias : tableName;
            }
            return null;
        }

        if (table == null) {
            return null;
        }

        String actualTableName = table.getName().toLowerCase();
        String target = tableName.toLowerCase();

        if (actualTableName.equals(target) || actualTableName.endsWith("." + target)) {
            return alias != null ? alias : table.getName();
        }

        return null;
    }

    /**
     * Qualify all unqualified column references in an expression with a table alias.
     * Uses JSqlParser's {@link ExpressionVisitorAdapter} to traverse the expression
     * tree and prefix {@link Column} nodes that lack a table qualifier.
     *
     * <p>This prevents "ambiguous column" errors in JOIN scenarios where multiple
     * tables share the same column name (e.g. {@code tenant_id}).</p>
     */
    private Expression qualifyColumns(Expression expression, String alias) {
        if (expression == null || alias == null) {
            return expression;
        }

        expression.accept(new ExpressionVisitorAdapter<Void>() {
            @Override
            public <S> Void visit(Column column, S context) {
                if (column.getTable() == null || column.getTable().getName() == null) {
                    column.setTable(new Table(alias));
                }
                return null;
            }
        });

        return expression;
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