package io.github.springwhale.framework.core.utils;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Utility for generating speech audio via the edge-tts command-line tool.
 * <p>
 * Requires edge-tts (https://github.com/rany2/edge-tts) to be installed and available on the system PATH.
 * Supports edge-tts v7.x+ with the {@code --write-media} parameter.
 * <p>
 * Supports concurrent TTS generation via a bounded thread pool. Each edge-tts invocation spawns
 * an independent OS process connecting to Microsoft's online TTS service, which naturally supports
 * concurrent usage.
 * <p>
 * Configurable via {@code edge-tts.*} application properties / environment variables:
 * <ul>
 *   <li>{@code edge-tts.command} - path/name of the edge-tts executable (default: {@code edge-tts})</li>
 *   <li>{@code edge-tts.timeout-seconds} - per-request timeout in seconds (default: 30)</li>
 *   <li>{@code edge-tts.concurrency} - max concurrent TTS tasks (default: CPU cores / 2, min 2)</li>
 * </ul>
 */
@Slf4j
@Component
public class EdgeTtsUtil {

    private final String command;
    private final int timeoutSeconds;
    private final int concurrency;
    private final ExecutorService executor;
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    /**
     * A TTS generation request.
     */
    public record TtsRequest(String id, String text, String voice, String outputPath) {
    }

    /**
     * A TTS generation result.
     */
    public record TtsResult(String id, boolean success, String outputPath, String errorMessage) {
    }

    public EdgeTtsUtil(
            @Value("${edge-tts.command:edge-tts}") String command,
            @Value("${edge-tts.timeout-seconds:30}") int timeoutSeconds,
            @Value("${edge-tts.concurrency:0}") int concurrency) {
        this.command = command;
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 30;
        this.concurrency = concurrency > 0 ? concurrency : Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        this.executor = Executors.newFixedThreadPool(this.concurrency, r -> {
            Thread t = new Thread(r, "edge-tts-worker");
            t.setDaemon(true);
            return t;
        });
        log.info("EdgeTtsUtil initialized: command={}, timeout={}s, concurrency={}",
                this.command, this.timeoutSeconds, this.concurrency);
    }

    /**
     * Convert text to an MP3 audio file using edge-tts (synchronous, backward-compatible).
     *
     * @param text       the text content to convert to speech
     * @param voice      the voice name, e.g. {@code zh-CN-XiaoxiaoNeural}
     * @param outputPath the target file path for the generated MP3
     * @return {@code true} if the audio file was generated successfully
     */
    public boolean ttsToMp3(String text, String voice, String outputPath) {
        TtsResult result = ttsToMp3Internal(new TtsRequest(outputPath, text, voice, outputPath), timeoutSeconds);
        return result.success();
    }

    /**
     * Convert text to an MP3 audio file with a configurable timeout.
     *
     * @param text            the text content to convert to speech
     * @param voice           the voice name
     * @param outputPath      the target file path
     * @param timeoutSeconds  max time in seconds before the process is killed
     * @return {@code true} if successful
     */
    public boolean ttsToMp3(String text, String voice, String outputPath, int timeoutSeconds) {
        TtsResult result = ttsToMp3Internal(new TtsRequest(outputPath, text, voice, outputPath), timeoutSeconds);
        return result.success();
    }

    /**
     * Async TTS generation. Returns a future that completes with the success flag.
     *
     * @param text       the text content
     * @param voice      the voice name
     * @param outputPath the target file path
     * @return CompletableFuture that resolves to {@code true} on success
     */
    public CompletableFuture<Boolean> ttsToMp3Async(String text, String voice, String outputPath) {
        return ttsToMp3Async(text, voice, outputPath, timeoutSeconds);
    }

    /**
     * Async TTS generation with configurable timeout.
     */
    public CompletableFuture<Boolean> ttsToMp3Async(String text, String voice, String outputPath, int timeoutSeconds) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        executor.submit(() -> {
            activeTasks.incrementAndGet();
            try {
                TtsResult result = ttsToMp3Internal(new TtsRequest(outputPath, text, voice, outputPath), timeoutSeconds);
                future.complete(result.success());
            } catch (Exception e) {
                log.error("Async TTS failed for outputPath={}", outputPath, e);
                future.complete(false);
            } finally {
                activeTasks.decrementAndGet();
            }
        });
        return future;
    }

    /**
     * Batch TTS generation with configurable parallelism.
     * Each request is processed concurrently up to the given concurrency limit.
     *
     * @param requests list of TTS requests with unique IDs
     * @return list of results in the same order as requests
     */
    public List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests) {
        return ttsToMp3Batch(requests, timeoutSeconds);
    }

    /**
     * Batch TTS generation with configurable timeout.
     */
    public List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests, int timeoutSeconds) {
        return ttsToMp3Batch(requests, timeoutSeconds, null);
    }

    /**
     * Batch TTS generation with per-item callback for real-time progress reporting.
     *
     * @param requests       list of TTS requests with unique IDs
     * @param itemCallback   callback invoked immediately after each request completes
     * @return list of results in the same order as requests
     */
    public List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests,
                                         Consumer<TtsResult> itemCallback) {
        return ttsToMp3Batch(requests, timeoutSeconds, itemCallback);
    }

    /**
     * Batch TTS generation with configurable timeout and per-item callback.
     */
    public List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests, int timeoutSeconds,
                                         Consumer<TtsResult> itemCallback) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        log.info("Starting batch TTS generation: {} requests, timeout={}s, concurrency={}",
                requests.size(), timeoutSeconds, concurrency);
        long startTime = System.currentTimeMillis();

        ConcurrentHashMap<String, TtsResult> resultMap = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>(requests.size());

        for (TtsRequest req : requests) {
            CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                activeTasks.incrementAndGet();
                try {
                    TtsResult result = ttsToMp3Internal(req, timeoutSeconds);
                    resultMap.put(req.id(), result);
                    if (itemCallback != null) {
                        try {
                            itemCallback.accept(result);
                        } catch (Exception cbEx) {
                            log.warn("Per-item callback failed for id={}", req.id(), cbEx);
                        }
                    }
                } catch (Exception e) {
                    log.error("Batch TTS failed for id={}", req.id(), e);
                    TtsResult errResult = new TtsResult(req.id(), false, req.outputPath(), e.getMessage());
                    resultMap.put(req.id(), errResult);
                    if (itemCallback != null) {
                        try {
                            itemCallback.accept(errResult);
                        } catch (Exception cbEx) {
                            log.warn("Per-item callback failed for id={}", req.id(), cbEx);
                        }
                    }
                } finally {
                    activeTasks.decrementAndGet();
                }
            }, executor);
            futures.add(f);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long elapsed = System.currentTimeMillis() - startTime;
        int successCount = (int) resultMap.values().stream().filter(TtsResult::success).count();
        log.info("Batch TTS generation completed: {} success, {} failed, {}ms elapsed",
                successCount, requests.size() - successCount, elapsed);

        List<TtsResult> results = new ArrayList<>(requests.size());
        for (TtsRequest req : requests) {
            TtsResult r = resultMap.get(req.id());
            results.add(r != null ? r : new TtsResult(req.id(), false, req.outputPath(), "Unknown error"));
        }
        return results;
    }

    /**
     * Returns the number of currently active TTS tasks.
     */
    public int getActiveTaskCount() {
        return activeTasks.get();
    }

    /**
     * Shutdown the thread pool on application context destroy.
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private TtsResult ttsToMp3Internal(TtsRequest req, int timeoutSeconds) {
        if (req.text() == null || req.text().isBlank()) {
            return new TtsResult(req.id(), false, req.outputPath(), "Text is empty");
        }

        List<String> cmd = List.of(
                command,
                "--text", req.text(),
                "--voice", req.voice(),
                "--write-media", req.outputPath()
        );

        File outputFile = new File(req.outputPath());
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        Process process;
        try {
            process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        } catch (IOException e) {
            log.error("Failed to start edge-tts process for id={}", req.id(), e);
            return new TtsResult(req.id(), false, req.outputPath(), "Failed to start process: " + e.getMessage());
        }

        StringBuilder output = new StringBuilder();
        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } catch (IOException ignored) {
            }
        }, "edge-tts-output-reader-" + req.id());
        readerThread.setDaemon(true);
        readerThread.start();

        try {
            boolean exited = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!exited) {
                process.destroyForcibly();
                readerThread.interrupt();
                return new TtsResult(req.id(), false, req.outputPath(),
                        "Process timed out after " + timeoutSeconds + "s");
            }

            readerThread.join(2000);

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("edge-tts failed for id={}, exitCode={}, output: {}", req.id(), exitCode, output);
                return new TtsResult(req.id(), false, req.outputPath(),
                        "edge-tts exitCode=" + exitCode + ", " + output.toString().trim());
            }

            boolean exists = outputFile.exists();
            if (!exists) {
                log.error("edge-tts succeeded but file not found for id={}, path: {}", req.id(), req.outputPath());
                return new TtsResult(req.id(), false, req.outputPath(), "Output file not created");
            }

            log.debug("edge-tts succeeded for id={}, path: {}", req.id(), req.outputPath());
            return new TtsResult(req.id(), true, req.outputPath(), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            readerThread.interrupt();
            return new TtsResult(req.id(), false, req.outputPath(), "Interrupted");
        }
    }
}
