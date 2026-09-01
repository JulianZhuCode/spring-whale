package io.github.springwhale.framework.webmvc.autoconfigure;

import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.framework.webmvc.security.SecurityFeignInterceptor;
import io.github.springwhale.framework.webmvc.security.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Feign interceptor that propagates JWT tokens
 * across service-to-service calls.
 */
@AutoConfiguration
@ConditionalOnClass(name = "feign.RequestInterceptor")
public class SecurityFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecurityFeignInterceptor securityFeignInterceptor(SecurityProperties props, JwtUtil jwtUtil) {
        return new SecurityFeignInterceptor(props, jwtUtil);
    }
}