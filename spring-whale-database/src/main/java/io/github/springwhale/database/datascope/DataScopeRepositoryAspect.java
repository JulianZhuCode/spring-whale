package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Slf4j
@Aspect
@Order(2)
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

        DataScopeResult resolvedScope = scope;
        boolean pushedResolved = false;
        if (scope.isEmpty()) {
            resolvedScope = resolveScope(scope);
            if (resolvedScope.isEmpty()) {
                log.debug("Data scope resolved but still empty, skip filtering");
                return joinPoint.proceed();
            }
            DataScopeContext.pushScope(resolvedScope);
            pushedResolved = true;
            log.debug("Resolved scope pushed: userId={}, deptIds={}",
                    resolvedScope.getUserId(), resolvedScope.getDeptIds());
        }

        Class<?> entityClass = resolveEntityClass(joinPoint.getTarget());
        if (entityClass == null) {
            log.debug("Cannot resolve entity class from repository: {}", joinPoint.getTarget().getClass());
            if (pushedResolved) {
                DataScopeContext.popScope();
            }
            return joinPoint.proceed();
        }

        List<String> deptFields = dataScopeHelper.resolveDeptIdFields(entityClass);
        List<String> userFields = dataScopeHelper.resolveUserIdFields(entityClass);

        if (deptFields.isEmpty() && userFields.isEmpty()) {
            log.debug("Entity {} has no @DeptIdField/@UserIdField annotations, skip data scope",
                    entityClass.getSimpleName());
            if (pushedResolved) {
                DataScopeContext.popScope();
            }
            return joinPoint.proceed();
        }

        DataScopeContext.setEntityClass(entityClass);
        DataScopeContext.setDeptFields(deptFields);
        DataScopeContext.setUserFields(userFields);

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

    private Class<?> resolveEntityClass(Object target) {
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Type type : clazz.getGenericInterfaces()) {
                if (type instanceof ParameterizedType pt) {
                    Type rawType = pt.getRawType();
                    if (rawType instanceof Class<?> rawClass && JpaRepository.class.isAssignableFrom(rawClass)) {
                        return (Class<?>) pt.getActualTypeArguments()[0];
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}