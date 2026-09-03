package io.github.springwhale.platform.task.support;

import io.github.springwhale.platform.task.handler.TaskHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Deterministic handler for engine/service tests.
 * <p>
 * Behavior is driven by task {@code params}:
 * <ul>
 *     <li>{@code count} - number of items to enumerate (default 5, keys {@code item-0..n-1})</li>
 *     <li>{@code flakyKeys} - keys that fail on the first attempt and succeed afterwards</li>
 *     <li>{@code errorKeys} - keys that always throw, simulating an unrecoverable item error</li>
 * </ul>
 * Lifecycle hook invocations are counted for assertions.
 */
public class TestTaskHandler implements TaskHandler {

    public static final String TASK_TYPE = "TEST_TASK";

    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    private final List<String> flakyKeys = Collections.synchronizedList(new ArrayList<>());
    private final List<String> errorKeys = Collections.synchronizedList(new ArrayList<>());
    private final List<String> processedKeys = Collections.synchronizedList(new ArrayList<>());

    public final AtomicInteger beforeStartCount = new AtomicInteger();
    public final AtomicInteger afterCompleteCount = new AtomicInteger();
    public final AtomicInteger onCancelCount = new AtomicInteger();

    @Override
    public String getTaskType() {
        return TASK_TYPE;
    }

    @Override
    public List<String> enumerateItems(Map<String, Object> params) {
        int count = intParam(params, "count", 5);
        flakyKeys.clear();
        flakyKeys.addAll(stringListParam(params, "flakyKeys"));
        errorKeys.clear();
        errorKeys.addAll(stringListParam(params, "errorKeys"));

        List<String> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add("item-" + i);
        }
        return keys;
    }

    @Override
    public boolean processItem(String itemKey, Map<String, Object> params) throws Exception {
        attempts.merge(itemKey, 1, Integer::sum);
        processedKeys.add(itemKey);

        if (errorKeys.contains(itemKey)) {
            throw new IllegalStateException("simulated processing error for " + itemKey);
        }
        // Flaky items fail on the first attempt, succeed from the second attempt on.
        if (flakyKeys.contains(itemKey) && attempts.get(itemKey) < 2) {
            return false;
        }
        return true;
    }

    @Override
    public void processBatch(List<String> itemKeys, Map<String, Object> params,
                             TaskHandler.BatchProgressCallback callback) throws Exception {
        if (Boolean.TRUE.equals(params == null ? null : params.get("fatalBatch"))) {
            throw new IllegalStateException("simulated fatal batch failure");
        }
        TaskHandler.super.processBatch(itemKeys, params, callback);
    }

    @Override
    public void beforeStart(Map<String, Object> params) {
        beforeStartCount.incrementAndGet();
    }

    @Override
    public void afterComplete(Map<String, Object> params) {
        afterCompleteCount.incrementAndGet();
    }

    @Override
    public void onCancel(Map<String, Object> params) {
        onCancelCount.incrementAndGet();
    }

    public int attemptsOf(String itemKey) {
        return attempts.getOrDefault(itemKey, 0);
    }

    public int processedCount() {
        return processedKeys.size();
    }

    public void reset() {
        attempts.clear();
        flakyKeys.clear();
        errorKeys.clear();
        processedKeys.clear();
        beforeStartCount.set(0);
        afterCompleteCount.set(0);
        onCancelCount.set(0);
    }

    private static int intParam(Map<String, Object> params, String name, int defaultValue) {
        Object value = params == null ? null : params.get(name);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringListParam(Map<String, Object> params, String name) {
        Object value = params == null ? null : params.get(name);
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }
}
