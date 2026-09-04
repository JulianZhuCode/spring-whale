package io.github.springwhale.database.datascope.annotation;

import java.lang.annotation.*;

/**
 * Marks a repository method (or entire repository interface) to skip all
 * SQL-level inspection — tenant isolation and data scope filtering.
 *
 * <p>Best placed on <strong>Repository</strong> methods where you write
 * complex custom SQL (joins, native queries, complex {@code @Query}) that
 * already handles filtering or should not be modified by the framework.</p>
 *
 * <p>When this annotation is present on a method, {@code TenantSqlInspector}
 * and {@code DataScopeInterceptor} will return SQL unchanged for all
 * repository calls within that method.</p>
 *
 * <p>When placed on a repository interface, all methods in that repository
 * are skipped.</p>
 *
 * <pre>{@code
 * &#64;Repository
 * public interface ReportRepository extends JpaRepository<Report, Long> {
 *
 *     // Complex join query — framework should NOT inject WHERE here
 *     &#64;SkipSqlInspector
 *     &#64;Query("SELECT r FROM Report r JOIN r.details d WHERE ...")
 *     List<Report> findComplexReport();
 * }
 * }</pre>
 *
 * @see TenantSqlInspector
 * @see DataScopeInterceptor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipSqlInspector {
}