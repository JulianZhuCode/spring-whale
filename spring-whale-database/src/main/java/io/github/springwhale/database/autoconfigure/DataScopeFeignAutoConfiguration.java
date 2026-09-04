package io.github.springwhale.database.autoconfigure;

import io.github.springwhale.database.datascope.DataScopeFeignClient;
import io.github.springwhale.database.datascope.DataScopeFeignInterceptor;
import io.github.springwhale.database.datascope.DataScopeProperties;
import io.github.springwhale.database.datascope.DataScopeSigner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Feign integration in the data scope module.
 *
 * <h3>Registered beans</h3>
 * <ul>
 *   <li>{@link DataScopeFeignInterceptor} — always registered when OpenFeign is
 *       on the classpath. Transmits scope type, module, and tenant ID via HTTP
 *       headers on all outgoing Feign requests.</li>
 *   <li>{@link DataScopeFeignClient} — only activated when
 *       {@code spring.whale.database.datascope.remote-rbac-url} is configured.
 *       Provides a Feign client for the RBAC data scope remote API.</li>
 * </ul>
 *
 * <p>Separated from {@link SpringWhaleDatabaseConfiguration} to avoid
 * {@code ClassNotFoundException} when OpenFeign is not on the classpath.</p>
 */
@AutoConfiguration
@ConditionalOnClass(EnableFeignClients.class)
public class DataScopeFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataScopeSigner dataScopeSigner(DataScopeProperties properties) {
        return new DataScopeSigner(
                properties.getHmacSecretKey(),
                properties.getTimestampWindow().toMillis());
    }

    @Bean
    @ConditionalOnMissingBean
    public DataScopeFeignInterceptor dataScopeFeignInterceptor(DataScopeProperties properties,
                                                                DataScopeSigner signer) {
        return new DataScopeFeignInterceptor(properties, signer);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "remote-rbac-url")
    @EnableFeignClients(basePackageClasses = DataScopeFeignClient.class)
    static class FeignClientConfiguration {
    }
}