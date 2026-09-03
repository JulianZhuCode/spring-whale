package io.github.springwhale.framework.core.exception;

import java.io.Serial;

public class CacheException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheException(Throwable cause) {
        super(cause);
    }
}