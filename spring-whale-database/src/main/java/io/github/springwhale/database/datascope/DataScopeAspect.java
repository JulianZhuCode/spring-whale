package io.github.springwhale.database.datascope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * AOP aspect that intercepts {@code @DataScope}-annotated methods and pushes
 * scope definitions into {@link DataScopeContext}.
 *
 * <p>Execution order: {@code @Order(1)} — runs before repository-level aspects.</p>
 *
 * <h3>{@code CALLER} delegation</h3>
 * When {@code @DataScope(scopeType = CALLER)}, the aspect reads the scope from
 * {@link DataScopeContext} (typically set by {@link DataScopeServerInterceptor}
 * from an upstream service's HTTP header). This enables cross-service scope
 * delegation in microservice architectures.
 */
@Slf4j
@Aspect
@Order(1)
@RequiredArgsConstructor
public class DataScopeAspect {

    private final DataScopeHandler dataScopeHandler;
    private final DataScopeProperties properties;

    @Around("@annotation(io.github.springwhale.database.datascope.DataScope)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed();
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DataScope dataScope = method.getAnnotation(DataScope.class);
        DataScopeType effectiveType = dataScope.scopeType();
        String effectiveModule = dataScope.module();

        if (dataScope.scopeType() == DataScopeType.CALLER) {
            DataScopeResult transmittedScope = DataScopeContext.getScope();
            if (transmittedScope == null) {
                return joinPoint.proceed();
            }
            effectiveType = transmittedScope.getScopeType();
            effectiveModule = transmittedScope.getModule();
        }

        DataScopeResult result = new DataScopeResult();
        result.setScopeType(effectiveType);
        result.setModule(effectiveModule);
        result.setDeptIds(dataScopeHandler.resolveDeptIds(effectiveType, effectiveModule));
        if (!result.hasDeptIds()) {
            result.setUserId(dataScopeHandler.resolveUserId());
        }

        DataScopeContext.pushScope(result);
        log.debug("DataScope pushed: type={}, module={}, userId={}, deptIds={}",
                effectiveType, effectiveModule, result.getUserId(), result.getDeptIds());

        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.popScope();
            log.debug("DataScope popped, remaining scope depth: {}",
                    DataScopeContext.getDepth());
        }
    }
}