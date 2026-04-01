package io.github.springwhale.test.exception;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.springwhale.framework.core.model.ApiResult;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionHandler;
import io.github.springwhale.framework.webmvc.exception.SpringWhaleWebMvcExceptionProperties;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SpringWhaleWebMvcExceptionHandler
 */
@SpringBootTest
class SpringWhaleWebMvcExceptionHandlerTest {

    private SpringWhaleWebMvcExceptionHandler mockHandler;
    private MessageSource mockMessageSource;

    @BeforeEach
    void setUp() {
        // Create mock components for controlled testing
        mockMessageSource = mock(MessageSource.class);
        SpringWhaleWebMvcExceptionProperties testProperties = new SpringWhaleWebMvcExceptionProperties();
        mockHandler = new SpringWhaleWebMvcExceptionHandler(mockMessageSource, testProperties);
    }

    @Test
    @DisplayName("Should handle generic Exception with 500 error")
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");

        ApiResult<Boolean> result = mockHandler.handleException(ex);

        assertNotNull(result);
        assertEquals("500", result.getCode());
        assertFalse(result.getData());
        assertEquals("Server abnormal, please try again later!", result.getMessage());
    }

    @Test
    @DisplayName("Should handle BusinessException with correct error code and message")
    void testHandleBusinessException() {
        BusinessException ex = BusinessException.create("USER_NOT_FOUND", "User not found");

        ApiResult<?> result = mockHandler.handleBusinessException(ex);

        assertNotNull(result);
        assertEquals("USER_NOT_FOUND", result.getCode());
        assertEquals("User not found", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("Should handle BusinessException with module prefix")
    void testHandleBusinessExceptionWithModule() {
        BusinessException ex = BusinessException.createWithModule(
                "USER_NOT_FOUND",
                "user-module",
                "User not found"
        );

        ApiResult<?> result = mockHandler.handleBusinessException(ex);

        assertNotNull(result);
        assertEquals("user-module_USER_NOT_FOUND", result.getCode());
        assertEquals("User not found", result.getMessage());
    }

    @Test
    @DisplayName("Should handle BusinessException with extended data")
    void testHandleBusinessExceptionWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 123L);
        data.put("email", "test@example.com");
        BusinessException ex = BusinessException.create("USER_NOT_FOUND", "User not found", data);

        ApiResult<?> result = mockHandler.handleBusinessException(ex);

        assertNotNull(result);
        assertEquals("USER_NOT_FOUND", result.getCode());
        assertEquals("User not found", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(123L, ((Map<?, ?>) result.getData()).get("userId"));
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 error")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ApiResult<Boolean> result = mockHandler.handleIllegalArgumentException(ex);

        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertFalse(result.getData());
        assertEquals("Invalid request parameters!", result.getMessage());
    }

    @Test
    @DisplayName("Should handle ValidationException with 400 error")
    void testHandleValidationException() {
        ValidationException ex = new ValidationException("Validation failed");

        ApiResult<Boolean> result = mockHandler.handleIllegalArgumentException(ex);

        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertFalse(result.getData());
        assertEquals("Invalid request parameters!", result.getMessage());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with 400 error")
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getMessage()).thenReturn("Validation failed");

        ApiResult<Boolean> result = mockHandler.handleIllegalArgumentException(ex);

        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertFalse(result.getData());
        assertEquals("Invalid request parameters!", result.getMessage());
    }

    @Test
    @DisplayName("Should handle BindException with 400 error")
    void testHandleBindException() {
        TestObject target = new TestObject("test");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
        // Add a validation error to make it a real bind exception
        bindingResult.rejectValue("name", "required", "Name is required");
        BindException ex = new BindException(bindingResult);

        ApiResult<Boolean> result = mockHandler.handleIllegalArgumentException(ex);

        assertNotNull(result);
        assertEquals("400", result.getCode());
        assertFalse(result.getData());
        assertEquals("Invalid request parameters!", result.getMessage());
    }

    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException with 405 error")
    void testHandleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");

        ApiResult<Boolean> result = mockHandler.handleHttpRequestMethodNotSupportedException(ex);

        assertNotNull(result);
        assertEquals("405", result.getCode());
        assertFalse(result.getData());
        assertEquals("Method not allowed!", result.getMessage());
    }

    @Test
    @DisplayName("Should handle DuplicateKeyException with 409 error")
    void testHandleDuplicateKeyException() {
        // Create DuplicateKeyException using mock since constructor is protected
        DuplicateKeyException ex = mock(DuplicateKeyException.class);
        when(ex.getMessage()).thenReturn("Duplicate key found");

        ApiResult<Boolean> result = mockHandler.handleDuplicateKeyException(ex);

        assertNotNull(result);
        assertEquals("409", result.getCode());
        assertFalse(result.getData());
        assertEquals("Duplicate records!", result.getMessage());
    }

    @Test
    @DisplayName("Should use i18n message when enabled and message exists")
    void testI18nMessageWhenEnabled() {
        // Setup mock to return i18n message
        when(mockMessageSource.getMessage(eq("http.error.500"), any(), eq(LocaleContextHolder.getLocale())))
                .thenReturn("服务器异常，请稍后重试！");

        // Enable i18n in properties
        SpringWhaleWebMvcExceptionProperties testProperties = new SpringWhaleWebMvcExceptionProperties();
        testProperties.setEnableI18n(true);
        SpringWhaleWebMvcExceptionHandler handlerWithI18n = new SpringWhaleWebMvcExceptionHandler(mockMessageSource, testProperties);

        Exception ex = new Exception("Server error");
        ApiResult<Boolean> result = handlerWithI18n.handleException(ex);

        assertNotNull(result);
        assertEquals("500", result.getCode());
        assertEquals("服务器异常，请稍后重试！", result.getMessage());
    }

    @Test
    @DisplayName("Should fallback to default message when i18n message not found")
    void testI18nFallbackWhenMessageNotFound() {
        // Setup mock to throw NoSuchMessageException
        when(mockMessageSource.getMessage(eq("http.error.500"), any(), eq(LocaleContextHolder.getLocale())))
                .thenThrow(new NoSuchMessageException("http.error.500"));

        // Enable i18n in properties
        SpringWhaleWebMvcExceptionProperties testProperties = new SpringWhaleWebMvcExceptionProperties();
        testProperties.setEnableI18n(true);
        SpringWhaleWebMvcExceptionHandler handlerWithI18n = new SpringWhaleWebMvcExceptionHandler(mockMessageSource, testProperties);

        Exception ex = new Exception("Server error");
        ApiResult<Boolean> result = handlerWithI18n.handleException(ex);

        assertNotNull(result);
        assertEquals("500", result.getCode());
        assertEquals("Server abnormal, please try again later!", result.getMessage());
    }

    @Test
    @DisplayName("Should use empty string when message is null and i18n disabled")
    void testEmptyMessageWhenNullAndI18nDisabled() {
        // Create properties with null message
        SpringWhaleWebMvcExceptionProperties testProperties = new SpringWhaleWebMvcExceptionProperties();
        testProperties.setMessage500(null);
        SpringWhaleWebMvcExceptionHandler handler = new SpringWhaleWebMvcExceptionHandler(mockMessageSource, testProperties);

        Exception ex = new Exception("Server error");
        ApiResult<Boolean> result = handler.handleException(ex);

        assertNotNull(result);
        assertEquals("500", result.getCode());
        assertEquals("", result.getMessage());
    }

    @Test
    @DisplayName("Should handle different locales correctly")
    void testDifferentLocales() {
        Locale originalLocale = LocaleContextHolder.getLocale();
        try {
            // Set locale to Japanese
            LocaleContextHolder.setLocale(Locale.JAPAN);

            // Setup mock to return Japanese message
            when(mockMessageSource.getMessage(eq("http.error.500"), any(), eq(Locale.JAPAN)))
                    .thenReturn("サーバーエラーが発生しました");

            SpringWhaleWebMvcExceptionProperties testProperties = new SpringWhaleWebMvcExceptionProperties();
            testProperties.setEnableI18n(true);
            SpringWhaleWebMvcExceptionHandler handlerWithI18n = new SpringWhaleWebMvcExceptionHandler(mockMessageSource, testProperties);

            Exception ex = new Exception("Server error");
            ApiResult<Boolean> result = handlerWithI18n.handleException(ex);

            assertNotNull(result);
            assertEquals("500", result.getCode());
            assertEquals("サーバーエラーが発生しました", result.getMessage());
        } finally {
            // Restore original locale
            LocaleContextHolder.setLocale(originalLocale);
        }
    }

    @Test
    @DisplayName("Should handle BusinessException with i18n message code")
    void testBusinessExceptionWithI18nCode() {
        // Setup mock to return i18n message using error code as key
        when(mockMessageSource.getMessage(eq("USER_NOT_FOUND"), any(), eq(LocaleContextHolder.getLocale())))
                .thenReturn("用户不存在");

        SpringWhaleWebMvcExceptionProperties testProperties = new SpringWhaleWebMvcExceptionProperties();
        testProperties.setEnableI18n(true);
        SpringWhaleWebMvcExceptionHandler handlerWithI18n = new SpringWhaleWebMvcExceptionHandler(mockMessageSource, testProperties);

        BusinessException ex = BusinessException.createWithI18n(
                "USER_NOT_FOUND",
                "error.user.notfound",
                "User not found"
        );

        ApiResult<?> result = handlerWithI18n.handleBusinessException(ex);

        assertNotNull(result);
        assertEquals("USER_NOT_FOUND", result.getCode());
        assertEquals("用户不存在", result.getMessage());
    }

    @Test
    @DisplayName("Should preserve exception hierarchy for BusinessException")
    void testBusinessExceptionHierarchy() {
        BusinessException ex = BusinessException.create("TEST", "Test error");

        assertInstanceOf(RuntimeException.class, ex);
        assertInstanceOf(Exception.class, ex);
        assertInstanceOf(BusinessException.class, ex);
    }

    record TestObject(String name) {
    }
}
