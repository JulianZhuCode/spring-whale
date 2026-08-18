package io.github.springwhale.framework.event;

/**
 * Exponential backoff retry strategy: delay doubles after each retry attempt,
 * capped at {@code maxInterval}.
 * <p>Formula: {@code min(baseInterval * 2^(retryCount-1), maxInterval)}.</p>
 * <p>Not a Spring bean — registered internally by {@link RetryStrategyRegistry}.</p>
 */
class ExponentialRetryStrategy implements RetryStrategy {

    @Override
    public long calculateDelay(int baseInterval, int maxInterval, int retryCount) {
        long delay = (long) baseInterval * (1L << (retryCount - 1));
        return Math.min(delay, maxInterval);
    }

}