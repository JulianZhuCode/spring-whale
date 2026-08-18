package io.github.springwhale.framework.event;

/**
 * Fixed-interval retry strategy: every retry waits the same amount of time.
 * <p>Not a Spring bean — registered internally by {@link RetryStrategyRegistry}.</p>
 */
class FixedRetryStrategy implements RetryStrategy {

    @Override
    public long calculateDelay(int baseInterval, int maxInterval, int retryCount) {
        return baseInterval;
    }

}