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
     * Whether to fall back to default description if i18n key is missing
     */
    private boolean fallbackToDefaultDesc = false;

    /**
     * DateTime format for serialization, default is "yyyy-MM-dd HH:mm:ss"
     * if value is "timestamp", it will serialize as timestamp (milliseconds since epoch)
     */
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * LocalDate format for serialization, default is "yyyy-MM-dd"
     */
    private String dateFormat = "yyyy-MM-dd";

    /**
     * LocalTime format for serialization, default is "HH:mm:ss"
     */
    private String timeFormat = "HH:mm:ss";

}
