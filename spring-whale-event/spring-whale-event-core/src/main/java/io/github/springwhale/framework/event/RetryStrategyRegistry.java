package io.github.springwhale.framework.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link RetryStrategy} implementations.
 * <p>Built-in strategies ({@code fixed}, {@code exponential}) are pre-registered
 * as plain instances (not Spring beans). Custom strategies can be registered as
 * Spring beans with a unique name (e.g. {@code @Component("jitter")}) and will
 * override built-in ones with the same name.</p>
 */
public class RetryStrategyRegistry {

    private final Map<String, RetryStrategy> strategies = new ConcurrentHashMap<>();

    public RetryStrategyRegistry(Map<String, RetryStrategy> customStrategies) {
        strategies.put("fixed", new FixedRetryStrategy());
        strategies.put("exponential", new ExponentialRetryStrategy());
        if (customStrategies != null) {
            strategies.putAll(customStrategies);
        }
    }

    /**
     * Get the strategy by name.
     *
     * @param name the strategy name (e.g. {@code fixed}, {@code exponential}, {@code jitter})
     * @return the matching {@link RetryStrategy}, never null
     * @throws IllegalArgumentException if no strategy is registered with the given name
     */
    public RetryStrategy get(String name) {
        RetryStrategy strategy = strategies.get(name);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unknown retry strategy '" + name + "'. Available: " + strategies.keySet());
        }
        return strategy;
    }

}