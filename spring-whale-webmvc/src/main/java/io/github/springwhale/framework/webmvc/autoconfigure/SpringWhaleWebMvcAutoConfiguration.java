package io.github.springwhale.framework.webmvc.autoconfigure;

import io.github.springwhale.framework.webmvc.advice.SpringWhaleWebMvcResponseBodyAdvice;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionHandler;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionProperties;
import io.github.springwhale.framework.webmvc.security.JwtAuthenticationFilter;
import io.github.springwhale.framework.webmvc.security.JwtUtil;
import io.github.springwhale.framework.webmvc.security.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Unified auto-configuration for Spring Whale Web MVC module.
 * <p>
 * Registers all framework beans explicitly via {@code @Bean} methods,
 * eliminating the need for {@code @ComponentScan} on the
 * {@code io.github.springwhale.framework.webmvc} package.
 * </p>
 * <p>
 * Registered via {@code AutoConfiguration.imports}.
 * </p>
 */
@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(SpringWhaleWebMvcExceptionProperties.class)
public class SpringWhaleWebMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpringWhaleWebMvcResponseBodyAdvice springWhaleWebMvcResponseBodyAdvice() {
        return new SpringWhaleWebMvcResponseBodyAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringWhaleWebMvcExceptionHandler springWhaleWebMvcExceptionHandler(
            MessageSource messageSource,
            SpringWhaleWebMvcExceptionProperties properties) {
        return new SpringWhaleWebMvcExceptionHandler(messageSource, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil(SecurityProperties securityProperties) {
        return new JwtUtil(securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(UserDetailsService.class)
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }
}