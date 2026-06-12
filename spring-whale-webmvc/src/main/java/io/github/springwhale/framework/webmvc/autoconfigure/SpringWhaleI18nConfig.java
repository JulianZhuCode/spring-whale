package io.github.springwhale.framework.webmvc.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * Internationalization (i18n) auto-configuration.
 * <p>
 * <b>Disabled by default.</b> Enable with:
 * <pre>{@code spring.whale.i18n.enabled=true}</pre>
 * </p>
 * <p>
 * Uses a cookie-based locale resolver so the user's language preference
 * persists across sessions. The default locale is English ({@code en}).
 * Users can switch languages via the {@code ?lang=} query parameter.
 * </p>
 * <p>
 * Registered via {@code AutoConfiguration.imports} and ordered before
 * {@link WebMvcAutoConfiguration} so that the custom {@code localeResolver}
 * bean takes precedence over Spring Boot's default
 * {@code AcceptHeaderLocaleResolver}, without needing
 * {@code spring.main.allow-bean-definition-overriding=true}.
 * </p>
 */
@AutoConfiguration
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.whale.i18n.enabled", havingValue = "true")
public class SpringWhaleI18nConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("sw_lang");
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieMaxAge(Duration.ofDays(365));
        resolver.setCookiePath("/");
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
