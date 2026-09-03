package io.github.springwhale.database.autoconfigure;

import io.github.springwhale.database.datascope.*;
import io.github.springwhale.framework.core.cache.WhaleCacheManager;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Auto-configuration for data scope and tenant isolation.
 *
 * <h3>Conditional assembly matrix</h3>
 * <table>
 *   <tr><th>{@code enabled}</th><th>{@code tenant-enabled}</th><th>Registered beans</th></tr>
 *   <tr><td>{@code true}</td><td>{@code true}</td><td>DataScopeInterceptor + TenantSqlInspector → CompositeStatementInspector</td></tr>
 *   <tr><td>{@code true}</td><td>{@code false}</td><td>DataScopeInterceptor only</td></tr>
 *   <tr><td>{@code false}</td><td>{@code true}</td><td>TenantSqlInspector only</td></tr>
 *   <tr><td>{@code false}</td><td>{@code false}</td><td>None (all data scope/tenant beans disabled)</td></tr>
 * </table>
 *
 * <h3>{@code @Order} chain</h3>
 * <pre>
 * DataScopeAspect (1) → TenantRepositoryAspect (2) → DataScopeRepositoryAspect (3)
 * </pre>
 */
@AutoConfiguration
@AutoConfigureAfter(DataScopeFeignAutoConfiguration.class)
@EnableConfigurationProperties(DataScopeProperties.class)
public class SpringWhaleDatabaseConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataScopeHelper dataScopeHelper() {
        return new DataScopeHelper();
    }

    @Bean
    @ConditionalOnMissingBean(DataScopeHandler.class)
    @ConditionalOnBean(DataScopeRemoteApi.class)
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "remote-rbac-url")
    public SmartDataScopeHandler smartDataScopeHandler(WhaleCacheManager cacheManager,
                                                       DataScopeRemoteApi dataScopeRemoteApi,
                                                       DataScopeProperties properties) {
        return new SmartDataScopeHandler(cacheManager, dataScopeRemoteApi, properties);
    }

    @Bean
    @ConditionalOnMissingBean(DataScopeHandler.class)
    public DefaultDataScopeHandler defaultDataScopeHandler() {
        return new DefaultDataScopeHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataScopeAspect dataScopeAspect(DataScopeHandler dataScopeHandler, DataScopeProperties properties) {
        return new DataScopeAspect(dataScopeHandler, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(JpaRepository.class)
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "tenant-enabled", havingValue = "true", matchIfMissing = true)
    public TenantRepositoryAspect tenantRepositoryAspect(DataScopeProperties properties,
                                                         DataScopeHelper dataScopeHelper,
                                                         DataScopeHandler dataScopeHandler) {
        return new TenantRepositoryAspect(properties, dataScopeHelper, dataScopeHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(JpaRepository.class)
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DataScopeRepositoryAspect dataScopeRepositoryAspect(DataScopeProperties properties,
                                                               DataScopeHelper dataScopeHelper,
                                                               DataScopeHandler dataScopeHandler) {
        return new DataScopeRepositoryAspect(properties, dataScopeHelper, dataScopeHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "enabled", havingValue = "true", matchIfMissing = true)
    public DataScopeInterceptor dataScopeInterceptor() {
        return new DataScopeInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "tenant-enabled", havingValue = "true", matchIfMissing = true)
    public TenantSqlInspector tenantSqlInspector(DataScopeProperties properties, DataScopeHandler dataScopeHandler) {
        return new TenantSqlInspector(properties, dataScopeHandler);
    }

    @Bean
    @ConditionalOnMissingBean(name = "statementInspector")
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "tenant-enabled", havingValue = "true", matchIfMissing = true)
    public HibernatePropertiesCustomizer dataScopeHibernateCustomizer(DataScopeInterceptor dataScopeInterceptor,
                                                                      TenantSqlInspector tenantSqlInspector) {
        return properties -> {
            CompositeStatementInspector compositeInspector = new CompositeStatementInspector(
                    List.of(tenantSqlInspector, dataScopeInterceptor));
            properties.put("hibernate.session_factory.statement_inspector", compositeInspector);
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "statementInspector")
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "tenant-enabled", havingValue = "false")
    public HibernatePropertiesCustomizer dataScopeOnlyHibernateCustomizer(DataScopeInterceptor dataScopeInterceptor) {
        return properties -> properties.put("hibernate.session_factory.statement_inspector", dataScopeInterceptor);
    }

    @Bean
    @ConditionalOnMissingBean(name = "statementInspector")
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "tenant-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "enabled", havingValue = "false")
    public HibernatePropertiesCustomizer tenantOnlyHibernateCustomizer(TenantSqlInspector tenantSqlInspector) {
        return properties -> properties.put("hibernate.session_factory.statement_inspector", tenantSqlInspector);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(DispatcherServlet.class)
    public DataScopeServerInterceptor dataScopeServerInterceptor(DataScopeProperties properties) {
        return new DataScopeServerInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(DispatcherServlet.class)
    public TenantWebMvcInterceptor tenantWebMvcInterceptor(DataScopeProperties properties) {
        return new TenantWebMvcInterceptor(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(DispatcherServlet.class)
    public WebMvcConfigurer dataScopeWebMvcConfigurer(DataScopeServerInterceptor serverInterceptor,
                                                      TenantWebMvcInterceptor tenantInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(serverInterceptor)
                        .addPathPatterns("/**")
                        .order(0);
                registry.addInterceptor(tenantInterceptor)
                        .addPathPatterns("/**")
                        .order(1);
            }
        };
    }
}