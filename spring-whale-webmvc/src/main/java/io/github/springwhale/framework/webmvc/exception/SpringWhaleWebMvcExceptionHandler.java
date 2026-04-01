package io.github.springwhale.framework.webmvc.exception;


import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.framework.core.model.ApiResult;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

@Slf4j
@RestControllerAdvice
public class SpringWhaleWebMvcExceptionHandler {

    private final MessageSource messageSource;
    private final SpringWhaleWebMvcExceptionProperties properties;

    public SpringWhaleWebMvcExceptionHandler(MessageSource messageSource, SpringWhaleWebMvcExceptionProperties properties) {
        this.messageSource = messageSource;
        this.properties = properties;
    }

    @ExceptionHandler(value = Exception.class)
    public ApiResult<Boolean> handleException(Exception e) {
        log.error("unknown exception occurred", e);
        String msg = properties.getMessage500();
        return ApiResult.error("500", getI18nMessage(msg, properties.getCode500()));
    }

    @ExceptionHandler(value = BusinessException.class)
    public ApiResult<?> handleBusinessException(BusinessException e) {
        log.warn("business exception occurred, code={}, message={}", e.getErrorCode(), e.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("business exception occurred, stack trace:", e);
        }
        String code = StringUtils.hasText(e.getModule()) ? e.getModule() + "_" + e.getErrorCode() : e.getErrorCode();
        return ApiResult.error(code, getI18nMessage(e.getErrorMessage(), e.getErrorCode()), e.getData());
    }

    @ExceptionHandler(value = {IllegalArgumentException.class,
            ValidationException.class,
            MethodArgumentNotValidException.class,
            BindException.class})
    public ApiResult<Boolean> handleIllegalArgumentException(Throwable e) {
        log.warn("illegal argument exception occurred, message={}", e.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("illegal argument exception occurred, stack trace:", e);
        }
        return ApiResult.error("400", getI18nMessage(properties.getMessage400(), properties.getCode400()));
    }

    @ExceptionHandler(value = {HttpRequestMethodNotSupportedException.class})
    public ApiResult<Boolean> handleHttpRequestMethodNotSupportedException(Throwable e) {
        log.warn("http request method not supported exception occurred, message={}", e.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("http request method not supported exception occurred, stack trace:", e);
        }
        return ApiResult.error("405", getI18nMessage(properties.getMessage405(), properties.getCode405()));
    }

    @ExceptionHandler(value = DuplicateKeyException.class)
    public ApiResult<Boolean> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("duplicate key exception occurred, message={}", e.getMessage());
        if (log.isDebugEnabled()) {
            log.debug("duplicate key exception occurred, stack trace:", e);
        }
        return ApiResult.error("409", getI18nMessage(properties.getMessage409(), properties.getCode409()));
    }

    private String getI18nMessage(String msg, String errorCode) {
        if (properties.isEnableI18n()) {
            try {
                return messageSource.getMessage(errorCode, null, LocaleContextHolder.getLocale());
            } catch (NoSuchMessageException ex) {
                log.warn("No message found under code: {}", errorCode);
            }
        }
        return msg != null ? msg : "";
    }
}
