package io.github.springwhale.test.exception;

import io.github.springwhale.framework.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessException
 */
class BusinessExceptionTest {

    @Test
    @DisplayName("Should create exception with basic fields")
    void testBasicCreate() {
        BusinessException ex = BusinessException.create("USER_NOT_FOUND", "User not found");

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("User not found", ex.getErrorMessage());
        assertNull(ex.getMessageCode());
        assertNull(ex.getModule());
        assertNull(ex.getData());
    }

    @Test
    @DisplayName("Should create exception with extended data")
    void testCreateWithData() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 123L);
        data.put("username", "test");

        BusinessException ex = BusinessException.create(
                "USER_NOT_FOUND",
                "User not found",
                data
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("User not found", ex.getErrorMessage());
        assertEquals(data, ex.getData());
        assertEquals(123L, ((Map<?, ?>) ex.getData()).get("userId"));
    }

    @Test
    @DisplayName("Should create exception with module information")
    void testCreateWithModule() {
        BusinessException ex = BusinessException.createWithModule(
                "USER_NOT_FOUND",
                "user-module",
                "User not found"
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("user-module", ex.getModule());
        assertEquals("User not found", ex.getErrorMessage());
        assertNull(ex.getMessageCode());
        assertNull(ex.getData());
    }

    @Test
    @DisplayName("Should create exception with module and data")
    void testCreateWithModuleAndData() {
        Map<String, Object> data = Map.of("orderId", "ORD-001");

        BusinessException ex = BusinessException.createWithModule(
                "ORDER_NOT_FOUND",
                "order-module",
                "Order not found",
                data
        );

        assertEquals("ORDER_NOT_FOUND", ex.getErrorCode());
        assertEquals("order-module", ex.getModule());
        assertEquals("Order not found", ex.getErrorMessage());
        assertEquals(data, ex.getData());
    }

    @Test
    @DisplayName("Should create exception with i18n key only")
    void testCreateWithI18nKeyOnly() {
        BusinessException ex = BusinessException.createWithI18n(
                "USER_NOT_FOUND",
                "error.user.notfound"
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertNull(ex.getErrorMessage());
        assertNull(ex.getModule());
        assertNull(ex.getData());
    }

    @Test
    @DisplayName("Should create exception with i18n and fallback message")
    void testCreateWithI18nAndFallbackMessage() {
        BusinessException ex = BusinessException.createWithI18n(
                "USER_NOT_FOUND",
                "error.user.notfound",
                "User not found"
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertEquals("User not found", ex.getErrorMessage());
        assertNull(ex.getModule());
        assertNull(ex.getData());
    }

    @Test
    @DisplayName("Should create exception with i18n and data")
    void testCreateWithI18nAndData() {
        Map<String, Object> data = Map.of("userId", 456L);

        BusinessException ex = BusinessException.createWithI18n(
                "USER_NOT_FOUND",
                "error.user.notfound",
                data
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertEquals(data, ex.getData());
        assertNull(ex.getErrorMessage());
    }

    @Test
    @DisplayName("Should create exception with full i18n parameters")
    void testCreateWithI18nFullParameters() {
        Map<String, Object> data = Map.of("userId", 789L);

        BusinessException ex = BusinessException.createWithI18n(
                "USER_NOT_FOUND",
                "error.user.notfound",
                "User not found",
                data
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertEquals("User not found", ex.getErrorMessage());
        assertEquals(data, ex.getData());
    }

    @Test
    @DisplayName("Should create exception with i18n and module")
    void testCreateI18nWithModule() {
        BusinessException ex = BusinessException.createI18nWithModule(
                "USER_NOT_FOUND",
                "error.user.notfound",
                "user-module"
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertEquals("user-module", ex.getModule());
        assertNull(ex.getErrorMessage());
        assertNull(ex.getData());
    }

    @Test
    @DisplayName("Should create exception with i18n, module and data")
    void testCreateI18nWithModuleAndData() {
        Map<String, Object> data = Map.of("userId", 999L);

        BusinessException ex = BusinessException.createI18nWithModule(
                "USER_NOT_FOUND",
                "error.user.notfound",
                "user-module",
                data
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertEquals("user-module", ex.getModule());
        assertEquals(data, ex.getData());
        assertNull(ex.getErrorMessage());
    }

    @Test
    @DisplayName("Should create exception with i18n, module and fallback message")
    void testCreateI18nWithModuleAndMessage() {
        BusinessException ex = BusinessException.createI18nWithModule(
                "USER_NOT_FOUND",
                "error.user.notfound",
                "user-module",
                "User not found"
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertEquals("user-module", ex.getModule());
        assertEquals("User not found", ex.getErrorMessage());
        assertNull(ex.getData());
    }

    @Test
    @DisplayName("Should create exception with all parameters")
    void testCreateI18nWithModuleFullParameters() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 12345L);
        data.put("attemptedEmail", "test@example.com");

        BusinessException ex = BusinessException.createI18nWithModule(
                "USER_NOT_FOUND",
                "error.user.notfound",
                "user-module",
                "User not found",
                data
        );

        assertEquals("USER_NOT_FOUND", ex.getErrorCode());
        assertEquals("error.user.notfound", ex.getMessageCode());
        assertEquals("user-module", ex.getModule());
        assertEquals("User not found", ex.getErrorMessage());
        assertEquals(data, ex.getData());
        assertEquals(12345L, ((Map<?, ?>) ex.getData()).get("userId"));
        assertEquals("test@example.com", ((Map<?, ?>) ex.getData()).get("attemptedEmail"));
    }

    @Test
    @DisplayName("Should support builder pattern")
    void testBuilderPattern() {
        Map<String, Object> data = Map.of("key", "value");

        BusinessException ex = BusinessException.builder()
                .errorCode("CUSTOM_ERROR")
                .errorMessage("Custom error message")
                .messageCode("error.custom")
                .module("custom-module")
                .data(data)
                .build();

        assertEquals("CUSTOM_ERROR", ex.getErrorCode());
        assertEquals("Custom error message", ex.getErrorMessage());
        assertEquals("error.custom", ex.getMessageCode());
        assertEquals("custom-module", ex.getModule());
        assertEquals(data, ex.getData());
    }

    @Test
    @DisplayName("Should not fill stack trace")
    void testFillInStackTrace() {
        BusinessException ex = BusinessException.create("TEST", "Test error");
        Throwable result = ex.fillInStackTrace();

        assertSame(ex, result, "fillInStackTrace should return the exception itself");
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void testIsRuntimeException() {
        BusinessException ex = BusinessException.create("TEST", "Test");

        assertInstanceOf(RuntimeException.class, ex);
        assertInstanceOf(Exception.class, ex);
        assertInstanceOf(Throwable.class, ex);
    }

    @Test
    @DisplayName("Should have correct toString output")
    void testToString() {
        BusinessException ex = BusinessException.create("TEST_CODE", "Test message");
        String toString = ex.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("TEST_CODE"));
        assertTrue(toString.contains("Test message"));
    }

    @Test
    @DisplayName("Should create exceptions with different error codes")
    void testDifferentErrorCodes() {
        BusinessException ex1 = BusinessException.create("ERROR_1", "Message 1");
        BusinessException ex2 = BusinessException.create("ERROR_2", "Message 2");

        assertEquals("ERROR_1", ex1.getErrorCode());
        assertEquals("ERROR_2", ex2.getErrorCode());
        assertNotEquals(ex1, ex2);
    }

    @Test
    @DisplayName("Should handle null data gracefully")
    void testNullData() {
        BusinessException ex = BusinessException.create("TEST", "Test", null);

        assertNull(ex.getData());
    }

    @Test
    @DisplayName("Should handle empty string messages")
    void testEmptyStringMessages() {
        BusinessException ex = BusinessException.create("", "");

        assertEquals("", ex.getErrorCode());
        assertEquals("", ex.getErrorMessage());
    }
}
