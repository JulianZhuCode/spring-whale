package io.github.springwhale.database.datascope;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

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