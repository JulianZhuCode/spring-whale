package io.github.springwhale.framework.core.json;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JSON serialization features
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.json")
public class SpringWhaleJsonConfig {

    /**
     * Whether to use internationalization for enum descriptions
     */
    private boolean useI18n = false;

    /**
     * Whether to fallback to default description if i18n key is missing
     */
    private boolean fallbackToDefaultDesc = false;

    /**
     * DateTime format for serialization, default is "yyyy-MM-dd HH:mm:ss"
     */
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * Date format for serialization, default is "yyyy-MM-dd"
     */
    private String dateFormat = "yyyy-MM-dd";

}
