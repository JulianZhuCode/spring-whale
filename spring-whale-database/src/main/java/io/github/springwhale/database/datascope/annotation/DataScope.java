package io.github.springwhale.database.datascope.annotation;


import io.github.springwhale.database.datascope.DataScopeInterceptor;
import io.github.springwhale.database.datascope.DataScopeType;

import java.lang.annotation.*;

/**
 * Marks a controller method for data scope filtering.
 *
 * <p>Methods annotated with {@code @DataScope} will have their SQL queries
 * automatically filtered by department and/or user through the
 * {@link DataScopeInterceptor}.</p>
 *
 * <pre>{@code
 * @DataScope(scopeType = DataScopeType.DEPT_AND_CHILD, module = "order")
 * @GetMapping("/orders")
 * public List<Order> listOrders() { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    DataScopeType scopeType() default DataScopeType.AUTO;

    String module() default "";
}