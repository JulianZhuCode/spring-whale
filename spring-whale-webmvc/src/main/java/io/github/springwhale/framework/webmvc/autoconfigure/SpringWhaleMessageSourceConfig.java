package io.github.springwhale.framework.webmvc.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Unified message source auto-configuration for Spring Whale Framework.
 * <p>
 * Scans the classpath for all {@code messages-*.properties} files across
 * framework modules and merges them into a single {@link MessageSource}.
 * This avoids the standard Java {@code ResourceBundle} limitation where
 * only the first {@code messages.properties} found on the classpath is loaded,
 * causing keys from other modules to be silently ignored.
 * </p>
 * <p>
 * User-defined {@code messages.properties} and any custom basenames configured
 * via {@code spring.messages.basename} take precedence over framework messages.
 * </p>
 */
@AutoConfiguration
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
@EnableConfigurationProperties(MessageSourceProperties.class)
public class SpringWhaleMessageSourceConfig {

    private static final String MESSAGES_PATTERN = "classpath*:messages-*.properties";

    /**
     * Matches locale-suffixed files like {@code messages-admin_ja_JP.properties}.
     */
    private static final String LOCALE_SUFFIX_REGEX = ".*_[a-z]{2,3}(_[A-Z]{1,3})?\\.properties";

    @Bean
    public MessageSource messageSource(MessageSourceProperties properties, ResourceLoader resourceLoader)
            throws IOException {

        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();

        // 1. User basenames first (highest priority)
        Set<String> basenames = new LinkedHashSet<>();
        List<String> userBasenames = properties.getBasename();
        if (userBasenames != null && !userBasenames.isEmpty()) {
            for (String bn : userBasenames) {
                String trimmed = bn.trim();
                if (!trimmed.isEmpty()) {
                    basenames.add(trimmed);
                }
            }
        }

        // 2. Auto-discover framework module message files (e.g. messages-admin, messages-rbac)
        Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
                .getResources(MESSAGES_PATTERN);
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null && !filename.matches(LOCALE_SUFFIX_REGEX)) {
                // "messages-admin.properties" → "classpath:messages-admin"
                basenames.add("classpath:" + filename.replace(".properties", ""));
            }
        }

        source.setBasenames(basenames.toArray(String[]::new));
        source.setDefaultEncoding(properties.getEncoding().name());
        source.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
        source.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());

        return source;
    }
}
