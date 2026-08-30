package io.github.springwhale.database.autoconfigure;

import io.github.springwhale.database.datascope.*;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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

@AutoConfiguration
@EnableConfigurationProperties(DataScopeProperties.class)
public class SpringWhaleDatabaseConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataScopeHelper dataScopeHelper() {
        return new DataScopeHelper();
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
    @ConditionalOnProperty(prefix = "spring.whale.database.datascope", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HibernatePropertiesCustomizer dataScopeHibernateCustomizer(DataScopeInterceptor interceptor) {
        return properties -> {
            properties.put("hibernate.session_factory.statement_inspector", interceptor);
        };
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
    public WebMvcConfigurer dataScopeWebMvcConfigurer(DataScopeServerInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/**")
                        .order(0);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(feign.RequestInterceptor.class)
    public DataScopeFeignInterceptor dataScopeFeignInterceptor(DataScopeProperties properties) {
        return new DataScopeFeignInterceptor(properties);
    }
}