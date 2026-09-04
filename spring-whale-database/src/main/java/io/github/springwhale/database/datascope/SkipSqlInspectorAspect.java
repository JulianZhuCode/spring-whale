package io.github.springwhale.database.datascope;

import io.github.springwhale.database.datascope.annotation.SkipSqlInspector;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

/**
 * AOP aspect that intercepts {@code @SkipSqlInspector}-annotated methods
 * (or all methods in an annotated repository interface) and sets a
 * ThreadLocal flag to skip all SQL-level inspection.
 *
 * <p>Execution order: {@code @Order(0)} — runs before {@code DataScopeAspect}
 * ({@code @Order(1)}) to ensure the skip flag is set before any scope or
 * entity info is pushed into {@link DataScopeContext}.</p>
 *
 * <p>When the flag is set, both {@link TenantSqlInspector} and
 * {@link DataScopeInterceptor} return SQL unchanged, allowing custom SQL
 * to be executed without automatic WHERE clause injection.</p>
 *
 * <p><strong>Best practice:</strong> place {@code @SkipSqlInspector} on
 * individual Repository methods that contain complex custom SQL (joins,
 * native queries, complex {@code @Query}). For repositories that are
 * entirely custom, place it on the interface itself.</p>
 */
@Slf4j
@Aspect
@Order(0)
public class SkipSqlInspectorAspect {

    @Pointcut("@annotation(io.github.springwhale.database.datascope.annotation.SkipSqlInspector)")
    public void annotatedMethod() {}

    @Pointcut("@within(io.github.springwhale.database.datascope.annotation.SkipSqlInspector)")
    public void annotatedClass() {}

    @Around("annotatedMethod() || annotatedClass()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        DataScopeContext.setSkipSqlInspector(true);
        log.debug("@SkipSqlInspector active, all SQL inspection skipped for: {}",
                joinPoint.getSignature().toShortString());
        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.setSkipSqlInspector(false);
            log.debug("@SkipSqlInspector cleared");
        }
    }
}