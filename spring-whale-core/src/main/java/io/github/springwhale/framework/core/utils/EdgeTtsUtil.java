package io.github.springwhale.framework.core.utils;

import io.github.springwhale.framework.core.model.TtsRequest;
import io.github.springwhale.framework.core.model.TtsResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Static utility facade for edge-tts speech synthesis.
 * <p>
 * Delegates to the {@link EdgeTtsEngine} Spring-managed bean internally.
 * Use this class for simple, one-off TTS calls without needing to inject a bean.
 * </p>
 *
 * <pre>{@code
 * // Simple synchronous call
 * EdgeTtsUtil.ttsToMp3("Hello world", "zh-CN-XiaoxiaoNeural", "/tmp/output.mp3");
 *
 * // Async
 * EdgeTtsUtil.ttsToMp3Async("Hello", "en-US-AriaNeural", "/tmp/out.mp3")
 *     .thenAccept(result -> System.out.println(result.success()));
 *
 * // Batch
 * var results = EdgeTtsUtil.ttsToMp3Batch(List.of(
 *     new TtsRequest("1", "First", "zh-CN-XiaoxiaoNeural", "/tmp/1.mp3"),
 *     new TtsRequest("2", "Second", "zh-CN-XiaoxiaoNeural", "/tmp/2.mp3")
 * ));
 * }</pre>
 */
public final class EdgeTtsUtil {

    private EdgeTtsUtil() {
    }

    private static EdgeTtsEngine engine() {
        return SpringContextUtils.getBean(EdgeTtsEngine.class);
    }

    // ──────────────────────────── Sync ────────────────────────────

    public static TtsResult ttsToMp3(String text, String voice, String outputPath) {
        return engine().ttsToMp3(text, voice, outputPath);
    }

    public static TtsResult ttsToMp3(String text, String voice, String outputPath, int timeoutSeconds) {
        return engine().ttsToMp3(text, voice, outputPath, timeoutSeconds);
    }

    // ──────────────────────────── Async ────────────────────────────

    public static CompletableFuture<TtsResult> ttsToMp3Async(String text, String voice, String outputPath) {
        return engine().ttsToMp3Async(text, voice, outputPath);
    }

    public static CompletableFuture<TtsResult> ttsToMp3Async(String text, String voice, String outputPath, int timeoutSeconds) {
        return engine().ttsToMp3Async(text, voice, outputPath, timeoutSeconds);
    }

    // ──────────────────────────── Batch ────────────────────────────

    public static List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests) {
        return engine().ttsToMp3Batch(requests);
    }

    public static List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests, int timeoutSeconds) {
        return engine().ttsToMp3Batch(requests, timeoutSeconds);
    }

    // ──────────────────────────── Monitoring ──────────────────────────

    public static int getActiveTaskCount() {
        return engine().getActiveTaskCount();
    }
}