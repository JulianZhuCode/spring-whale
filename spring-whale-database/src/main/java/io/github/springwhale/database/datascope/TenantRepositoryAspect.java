package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * AOP aspect that sets tenant metadata (tenant ID, tenant ID fields, entity class)
 * into {@link DataScopeContext} before JPA repository calls.
 *
 * <p>Execution order: {@code @Order(2)} — runs after {@code DataScopeAspect}
 * ({@code @Order(1)}) and before {@code DataScopeRepositoryAspect}
 * ({@code @Order(3)}).</p>
 *
 * <p>The tenant ID is resolved via {@link DataScopeHandler#resolveTenantId()},
 * which defaults to {@link io.github.springwhale.framework.core.utils.AuthUtil#getTenantId()}.</p>
 *
 * <p>If {@link DataScopeHandler#skipTenantScope()} returns {@code true}, the
 * aspect skips all processing. This is typically used for platform super
 * administrators who can see data across all tenants.</p>
 */
@Slf4j
@Aspect
@Order(2)
public class TenantRepositoryAspect {

    private final DataScopeProperties properties;
    private final DataScopeHelper dataScopeHelper;
    private final DataScopeHandler dataScopeHandler;

    public TenantRepositoryAspect(DataScopeProperties properties,
                                  DataScopeHelper dataScopeHelper,
                                  DataScopeHandler dataScopeHandler) {
        this.properties = properties;
        this.dataScopeHelper = dataScopeHelper;
        this.dataScopeHandler = dataScopeHandler;
    }

    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isTenantEnabled()) {
            return joinPoint.proceed();
        }

        if (dataScopeHandler.skipTenantScope()) {
            return joinPoint.proceed();
        }

        Class<?> entityClass = dataScopeHelper.resolveEntityClass(joinPoint.getTarget());
        if (entityClass == null) {
            return joinPoint.proceed();
        }

        Long tenantId = dataScopeHandler.resolveTenantId();
        List<String> tenantFields = dataScopeHelper.resolveTenantIdFields(entityClass);
        if (tenantId == null || tenantFields.isEmpty()) {
            return joinPoint.proceed();
        }

        DataScopeContext.setTenantId(tenantId);
        DataScopeContext.setTenantFields(tenantFields);
        DataScopeContext.setEntityClass(entityClass);

        log.debug("Tenant info set: entity={}, tenantId={}, fields={}",
                entityClass.getSimpleName(), tenantId, tenantFields);

        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clearEntityInfo();
        }
    }
}