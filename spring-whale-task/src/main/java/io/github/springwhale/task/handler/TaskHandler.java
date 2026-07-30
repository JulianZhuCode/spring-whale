package io.github.springwhale.task.handler;

import java.util.List;
import java.util.Map;

/**
 * Handler interface for batch task processing.
 * <p>
 * Applications implement this interface to provide concrete task logic.
 * Each implementation is registered by {@code taskType} and discovered automatically.
 * </p>
 *
 * <pre>{@code
 * @Component
 * public class WordAudioTaskHandler implements TaskHandler {
 *     @Override
 *     public String getTaskType() { return "WORD_AUDIO"; }
 *
 *     @Override
 *     public List<String> enumerateItems(Map<String, Object> params) {
 *         return wordRepository.findAll().stream()
 *                 .map(w -> "word:" + w.getId())
 *                 .toList();
 *     }
 *
 *     @Override
 *     public boolean processItem(String itemKey, Map<String, Object> params) {
 *         int wordId = Integer.parseInt(itemKey.split(":")[1]);
 *         wordService.regenerateAudio(wordId);
 *         return true;
 *     }
 * }
 * }</pre>
 */
public interface TaskHandler {

    /**
     * Returns the unique task type identifier (e.g., "WORD_AUDIO", "EXAMPLE_AUDIO").
     */
    String getTaskType();

    /**
     * Enumerates all item keys that need to be processed.
     * Each key uniquely identifies an item within the task.
     *
     * @param params task parameters passed at creation time
     * @return list of item keys to process
     */
    List<String> enumerateItems(Map<String, Object> params);

    /**
     * Processes a single item.
     *
     * @param itemKey the item key to process
     * @param params  task parameters passed at creation time
     * @return true if the item was processed successfully, false otherwise
     * @throws Exception if processing fails with an unrecoverable error
     */
    boolean processItem(String itemKey, Map<String, Object> params) throws Exception;

    /**
     * Processes multiple items in batch.
     * <p>
     * Default implementation delegates to {@link #processItem(String, Map)} for each item.
     * Handlers that can benefit from batch processing (e.g., audio generation with thread pools)
     * should override this method to implement more efficient processing.
     * </p>
     *
     * @param itemKeys the list of item keys to process
     * @param params   task parameters passed at creation time
     * @param callback callback to report per-item results for progress tracking
     * @throws Exception if processing fails with an unrecoverable error
     */
    default void processBatch(List<String> itemKeys, Map<String, Object> params,
                              BatchProgressCallback callback) throws Exception {
        for (String key : itemKeys) {
            boolean success;
            try {
                success = processItem(key, params);
            } catch (Exception e) {
                success = false;
            }
            callback.onItemResult(key, success);
        }
    }

    /**
     * Optional callback invoked before the task starts processing.
     */
    default void beforeStart(Map<String, Object> params) {
    }

    /**
     * Optional callback invoked after the task completes (all items processed).
     */
    default void afterComplete(Map<String, Object> params) {
    }

    /**
     * Optional callback invoked when the task is cancelled.
     */
    default void onCancel(Map<String, Object> params) {
    }

    /**
     * Callback for reporting per-item processing results during batch execution.
     */
    @FunctionalInterface
    interface BatchProgressCallback {
        /**
         * Called when an item has been processed.
         *
         * @param itemKey the item key
         * @param success true if the item was processed successfully
         */
        void onItemResult(String itemKey, boolean success);

        /**
         * Checks if the task has been cancelled/paused and processing should stop.
         * Handlers should check this between chunks to enable responsive cancellation.
         *
         * @return true if processing should be aborted
         */
        default boolean isCancelled() {
            return false;
        }

        /**
         * Forces flushing of accumulated progress to the database immediately.
         * Handlers should call this after completing each chunk to ensure
         * progress is persisted for page refreshes.
         */
        default void flush() {
        }
    }
}
