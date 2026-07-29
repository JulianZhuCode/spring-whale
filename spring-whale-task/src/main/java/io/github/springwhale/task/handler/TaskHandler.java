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
}
