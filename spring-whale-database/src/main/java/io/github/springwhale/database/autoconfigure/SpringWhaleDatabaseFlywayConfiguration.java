package io.github.springwhale.database.autoconfigure;

import io.github.springwhale.database.datascope.DataScopeFeignInterceptor;
import io.github.springwhale.database.datascope.DataScopeProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(feign.RequestInterceptor.class)
public class SpringWhaleDatabaseFlywayConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataScopeFeignInterceptor dataScopeFeignInterceptor(DataScopeProperties properties) {
        return new DataScopeFeignInterceptor(properties);
    }
}
