package io.github.springwhale.rbac;

import io.github.springwhale.framework.webmvc.advice.SpringWhaleWebMvcResponseBodyAdvice;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionHandler;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ComponentScan
@Slf4j
public class SpringWhaleRBACConfiguration {
    static {
        log.info("SpringWhaleRBACConfiguration loaded");
    }
}
