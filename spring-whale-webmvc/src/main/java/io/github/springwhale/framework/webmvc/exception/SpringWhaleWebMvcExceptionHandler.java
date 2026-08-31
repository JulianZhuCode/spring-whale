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
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler that intercepts exceptions thrown by controllers
 * and returns a unified {@link ApiResult} response.
 *
 * <h3>Exception mapping</h3>
 * <table>
 *   <tr><th>Exception</th><th>HTTP status</th><th>Error code</th></tr>
 *   <tr><td>{@link BusinessException}</td><td>200</td><td>module_errorCode</td></tr>
 *   <tr><td>Validation / Bind / IllegalArgument</td><td>200</td><td>400</td></tr>
 *   <tr><td>{@link HttpRequestMethodNotSupportedException}</td><td>200</td><td>405</td></tr>
 *   <tr><td>{@link NoResourceFoundException}</td><td>200</td><td>404</td></tr>
 *   <tr><td>All other exceptions</td><td>200</td><td>500</td></tr>
 * </table>
 *
 * <h3>i18n support</h3>
 * When {@code spring.whale.web-mvc.exception.enable-i18n=true},
 * error messages are resolved via {@link MessageSource} using the error code
 * as the message key, falling back to the configured default message.
 */
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

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ApiResult<Boolean> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("no resource found: {}", e.getMessage());
        return ApiResult.error("404", getI18nMessage(properties.getMessage404(), properties.getCode404()));
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