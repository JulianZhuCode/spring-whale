package io.github.springwhale.framework.webmvc;

import io.github.springwhale.framework.webmvc.advice.SpringWhaleWebMvcResponseBodyAdvice;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionHandler;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        SpringWhaleWebMvcExceptionHandler.class,
        SpringWhaleWebMvcExceptionProperties.class,
        SpringWhaleWebMvcResponseBodyAdvice.class,
})
@Slf4j
public class SpringWhaleWebMvcConfiguration {
    static {
        log.info("SpringWhaleWebMvcConfiguration loaded");
    }
}
