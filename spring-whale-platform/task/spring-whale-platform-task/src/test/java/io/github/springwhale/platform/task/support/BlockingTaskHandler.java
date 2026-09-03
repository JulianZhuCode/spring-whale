package io.github.springwhale.platform.task.support;

import io.github.springwhale.platform.task.handler.TaskHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler whose items block until a test-controlled gate is opened.
 * <p>
 * Used to create a deterministic window in which the task is RUNNING, so that
 * pause / cancel behavior can be exercised without timing races. Interrupts
 * (caused by future cancellation) are deliberately ignored — the gate is the
 * single control point, which keeps tests deterministic.
 */
public class BlockingTaskHandler implements TaskHandler {

    public static final String TASK_TYPE = "BLOCK_TASK";

    private final AtomicBoolean gateOpen = new AtomicBoolean(false);
    private final AtomicInteger enteredCount = new AtomicInteger();
    private final AtomicInteger processedCount = new AtomicInteger();
    private final AtomicInteger onCancelCount = new AtomicInteger();

    @Override
    public String getTaskType() {
        return TASK_TYPE;
    }

    @Override
    public List<String> enumerateItems(Map<String, Object> params) {
        Object countValue = params == null ? null : params.get("count");
        int count = countValue instanceof Number number ? number.intValue() : 3;

        List<String> keys = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            keys.add("block-" + i);
        }
        return keys;
    }

    @Override
    public boolean processItem(String itemKey, Map<String, Object> params) {
        enteredCount.incrementAndGet();
        while (!gateOpen.get()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Pause/cancel interrupts the worker thread; the gate remains the
                // single control point so tests stay deterministic.
            }
        }
        processedCount.incrementAndGet();
        return true;
    }

    @Override
    public void onCancel(Map<String, Object> params) {
        onCancelCount.incrementAndGet();
    }

    /**
     * Releases all blocked items.
     */
    public void openGate() {
        gateOpen.set(true);
    }

    /**
     * Closes the gate and resets counters for the next test.
     */
    public void reset() {
        gateOpen.set(false);
        enteredCount.set(0);
        processedCount.set(0);
        onCancelCount.set(0);
    }

    public int enteredCount() {
        return enteredCount.get();
    }

    public int processedCount() {
        return processedCount.get();
    }

    public int onCancelCount() {
        return onCancelCount.get();
    }
}
