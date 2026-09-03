package io.github.springwhale.database.datascope;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompositeStatementInspectorTest {

    @Test
    @DisplayName("inspect chains all inspectors sequentially")
    void testInspectChains() {
        StatementInspector appendA = sql -> sql + " A";
        StatementInspector appendB = sql -> sql + " B";
        CompositeStatementInspector composite = new CompositeStatementInspector(List.of(appendA, appendB));

        String result = composite.inspect("SELECT * FROM t");

        assertThat(result).isEqualTo("SELECT * FROM t A B");
    }

    @Test
    @DisplayName("inspect with empty list returns original SQL unchanged")
    void testInspectEmpty() {
        CompositeStatementInspector composite = new CompositeStatementInspector(List.of());

        String result = composite.inspect("SELECT * FROM t");

        assertThat(result).isEqualTo("SELECT * FROM t");
    }

    @Test
    @DisplayName("inspect with single inspector delegates correctly")
    void testInspectSingle() {
        StatementInspector upper = String::toUpperCase;
        CompositeStatementInspector composite = new CompositeStatementInspector(List.of(upper));

        String result = composite.inspect("select * from t");

        assertThat(result).isEqualTo("SELECT * FROM T");
    }

    @Test
    @DisplayName("inspect with multiple inspectors preserves order")
    void testInspectOrder() {
        StatementInspector first = sql -> sql + " FIRST";
        StatementInspector second = sql -> sql + " SECOND";
        CompositeStatementInspector composite = new CompositeStatementInspector(List.of(first, second));

        String result = composite.inspect("SQL");

        assertThat(result).isEqualTo("SQL FIRST SECOND");
    }
}