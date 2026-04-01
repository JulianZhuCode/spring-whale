package io.github.springwhale.framework.webmvc.exception;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.whale.web-mvc.exception")
public class SpringWhaleWebMvcExceptionProperties {

    private boolean enableI18n = false;

    private String message500 = "Server abnormal, please try again later!";

    private String code500 = "http.error.500";

    private String message400 = "Invalid request parameters!";

    private String code400 = "http.error.400";

    private String message405 = "Method not allowed!";

    private String code405 = "http.error.405";

    private String message409 = "Duplicate records!";

    private String code409 = "http.error.409";
}
