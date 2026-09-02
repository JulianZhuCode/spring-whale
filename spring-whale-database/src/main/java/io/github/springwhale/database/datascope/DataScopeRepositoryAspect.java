package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * AOP aspect that sets data scope metadata (entity class, department ID fields,
 * user ID fields) into {@link DataScopeContext} before JPA repository calls.
 *
 * <p>Execution order: {@code @Order(3)} — runs after {@code TenantRepositoryAspect}
 * ({@code @Order(2)}) to ensure tenant fields are set before data scope fields.</p>
 *
 * <p>When the current scope is empty (no dept/user IDs), it delegates to
 * {@link DataScopeHandler#resolveDeptIds} and {@link DataScopeHandler#resolveUserId}
 * to resolve the scope from the current authentication context. If the resolved
 * scope is still empty, {@link DataScopeResult#setDenied(boolean)} is set to
 * {@code true}, and the SQL interceptor injects {@code WHERE 1=0} to return an
 * empty result set.</p>
 *
 * <p>If {@link DataScopeHandler#skipDataScope()} returns {@code true}, the aspect
 * skips all processing and proceeds without data scope filtering.</p>
 */
@Slf4j
@Aspect
@Order(3)
public class DataScopeRepositoryAspect {

    private final DataScopeProperties properties;
    private final DataScopeHelper dataScopeHelper;
    private final DataScopeHandler dataScopeHandler;

    public DataScopeRepositoryAspect(DataScopeProperties properties,
                                     DataScopeHelper dataScopeHelper,
                                     DataScopeHandler dataScopeHandler) {
        this.properties = properties;
        this.dataScopeHelper = dataScopeHelper;
        this.dataScopeHandler = dataScopeHandler;
    }

    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        DataScopeResult scope = DataScopeContext.getScope();
        if (scope == null || !properties.isEnabled()) {
            return joinPoint.proceed();
        }

        if (dataScopeHandler.skipDataScope()) {
            return joinPoint.proceed();
        }

        Class<?> entityClass = dataScopeHelper.resolveEntityClass(joinPoint.getTarget());
        if (entityClass == null) {
            return joinPoint.proceed();
        }

        DataScopeResult resolvedScope = scope;
        boolean pushedResolved = false;
        if (scope.isEmpty()) {
            resolvedScope = resolveScope(scope);
            if (resolvedScope.isEmpty()) {
                resolvedScope.setDenied(true);
            }
            DataScopeContext.pushScope(resolvedScope);
            pushedResolved = true;
            log.debug("Resolved scope pushed: userId={}, deptIds={}, denied={}",
                    resolvedScope.getUserId(), resolvedScope.getDeptIds(), resolvedScope.isDenied());
        }

        DataScopeContext.setEntityClass(entityClass);

        List<String> deptFields = dataScopeHelper.resolveDeptIdFields(entityClass);
        List<String> userFields = dataScopeHelper.resolveUserIdFields(entityClass);

        if (!deptFields.isEmpty() || !userFields.isEmpty()) {
            DataScopeContext.setDeptFields(deptFields);
            DataScopeContext.setUserFields(userFields);
        }

        log.debug("Data scope enabled for entity: {}, deptFields={}, userFields={}, userId={}, deptIds={}",
                entityClass.getSimpleName(), deptFields, userFields,
                resolvedScope.getUserId(), resolvedScope.getDeptIds());

        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clearEntityInfo();
            if (pushedResolved) {
                DataScopeContext.popScope();
            }
        }
    }

    private DataScopeResult resolveScope(DataScopeResult original) {
        DataScopeResult resolved = new DataScopeResult();
        resolved.setScopeType(original.getScopeType());
        resolved.setModule(original.getModule());

        if (original.hasUserId()) {
            resolved.setUserId(original.getUserId());
        } else {
            resolved.setUserId(dataScopeHandler.resolveUserId());
        }

        if (original.hasDeptIds()) {
            resolved.setDeptIds(original.getDeptIds());
        } else {
            resolved.setDeptIds(dataScopeHandler.resolveDeptIds(
                    original.getScopeType(), original.getModule()));
        }

        return resolved;
    }
}