package io.github.springwhale.framework.core.exception;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BusinessException extends RuntimeException {

    /**
     * Error code, e.g., USER_NOT_FOUND
     */
    private final String errorCode;

    /**
     * User-friendly error message (supports i18n fallback)
     */
    private final String errorMessage;

    /**
     * Internationalization message key for retrieving multi-language messages from MessageSource
     */
    private final String messageCode;

    /**
     * Business module/category identifier, e.g., user-module, order-module
     */
    private final String module;

    /**
     * Extended data for passing additional error context information
     */
    private final Object data;

    public static BusinessException create(String errorCode, String errorMessage) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }

    public static BusinessException create(String errorCode, String errorMessage, Object data) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .data(data)
                .build();
    }

    public static BusinessException createWithModule(String errorCode, String module, String errorMessage) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .module(module)
                .errorMessage(errorMessage)
                .build();
    }

    public static BusinessException createWithModule(String errorCode, String module, String errorMessage, Object data) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .module(module)
                .errorMessage(errorMessage)
                .data(data)
                .build();
    }

    public static BusinessException createWithI18n(String errorCode, String messageCode) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .build();
    }

    public static BusinessException createWithI18n(String errorCode, String messageCode, String errorMessage) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .errorMessage(errorMessage)
                .build();
    }

    public static BusinessException createWithI18n(String errorCode, String messageCode, Object data) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .data(data)
                .build();
    }

    public static BusinessException createWithI18n(String errorCode, String messageCode, String errorMessage, Object data) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .errorMessage(errorMessage)
                .data(data)
                .build();
    }

    public static BusinessException createI18nWithModule(String errorCode, String messageCode, String module) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .module(module)
                .build();
    }

    public static BusinessException createI18nWithModule(String errorCode, String messageCode, String module, Object data) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .module(module)
                .data(data)
                .build();
    }

    public static BusinessException createI18nWithModule(String errorCode, String messageCode, String module, String errorMessage) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .module(module)
                .errorMessage(errorMessage)
                .build();
    }

    public static BusinessException createI18nWithModule(String errorCode, String messageCode, String module, String errorMessage, Object data) {
        return BusinessException.builder()
                .errorCode(errorCode)
                .messageCode(messageCode)
                .module(module)
                .errorMessage(errorMessage)
                .data(data)
                .build();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
