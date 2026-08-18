package io.github.springwhale.framework.event;

/**
 * SPI interface for retry backoff strategy.
 * <p>Built-in strategies ({@code fixed}, {@code exponential}) are registered
 * internally by {@link RetryStrategyRegistry}. Custom strategies are registered
 * as Spring beans with a unique name:</p>
 * <pre>{@code
 * @Component("jitter")
 * public class JitterRetryStrategy implements RetryStrategy {
 *     public long calculateDelay(int baseInterval, int maxInterval, int retryCount) {
 *         // custom logic
 *     }
 * }
 * }</pre>
 * <p>The strategy name configured via {@code spring.whale.event.retry-strategy}
 * is used to look up from {@link RetryStrategyRegistry}.</p>
 */
public interface RetryStrategy {

    /**
     * Calculate the delay in seconds before the next retry.
     *
     * @param baseInterval base retry interval in seconds
     * @param maxInterval  maximum retry interval in seconds (cap)
     * @param retryCount   current retry count (1-based: 1 = first retry)
     * @return delay in seconds
     */
    long calculateDelay(int baseInterval, int maxInterval, int retryCount);

}