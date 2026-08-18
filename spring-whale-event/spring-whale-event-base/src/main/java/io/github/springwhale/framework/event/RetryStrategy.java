package io.github.springwhale.framework.event;

/**
 * Retry backoff strategy for failed event messages.
 * <p>Each strategy encapsulates its own delay calculation algorithm.
 * New strategies can be added without changing any caller code.</p>
 */
public enum RetryStrategy {

    /**
     * Fixed interval: every retry waits the same amount of time.
     */
    FIXED {
        @Override
        public long calculateDelay(int baseInterval, int maxInterval, int retryCount) {
            return baseInterval;
        }
    },

    /**
     * Exponential backoff: delay doubles after each retry attempt.
     * <p>Formula: {@code min(baseInterval * 2^(retryCount-1), maxInterval)}.</p>
     */
    EXPONENTIAL {
        @Override
        public long calculateDelay(int baseInterval, int maxInterval, int retryCount) {
            long delay = (long) baseInterval * (1L << (retryCount - 1));
            return Math.min(delay, maxInterval);
        }
    };

    /**
     * Calculate the delay in seconds before the next retry.
     *
     * @param baseInterval base retry interval in seconds
     * @param maxInterval  maximum retry interval in seconds (cap)
     * @param retryCount   current retry count (1-based: 1 = first retry)
     * @return delay in seconds
     */
    public abstract long calculateDelay(int baseInterval, int maxInterval, int retryCount);

}