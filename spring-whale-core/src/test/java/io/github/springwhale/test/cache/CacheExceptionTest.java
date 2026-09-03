package io.github.springwhale.test.cache;

import io.github.springwhale.framework.core.exception.CacheException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CacheException")
class CacheExceptionTest {

    @Test
    @DisplayName("Should create with message only")
    void testMessageOnly() {
        CacheException ex = new CacheException("Cache read failed");

        assertEquals("Cache read failed", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("Should create with message and cause")
    void testMessageAndCause() {
        RuntimeException cause = new RuntimeException("IO error");
        CacheException ex = new CacheException("Cache read failed", cause);

        assertEquals("Cache read failed", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    @DisplayName("Should create with cause only")
    void testCauseOnly() {
        RuntimeException cause = new RuntimeException("IO error");
        CacheException ex = new CacheException(cause);

        assertEquals("java.lang.RuntimeException: IO error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}