package io.github.springwhale.framework.core.utils;

import io.github.springwhale.framework.core.model.TtsRequest;
import io.github.springwhale.framework.core.model.TtsResult;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Internal engine for edge-tts speech synthesis.
 * <p>
 * Managed as a Spring bean to handle executor lifecycle and concurrency control.
 * Each edge-tts invocation spawns an independent OS process connecting to Microsoft's
 * online TTS service. Virtual threads are used for async execution, with a semaphore
 * bounding the number of concurrent OS processes.
 * </p>
 */
@Slf4j
public class EdgeTtsEngine {

    private static final int DEFAULT_TIMEOUT = 30;
    private static final int READER_JOIN_TIMEOUT_MS = 2000;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 10;

    private final String command;
    private final int timeoutSeconds;
    private final int concurrency;
    private final ExecutorService executor;
    private final Semaphore semaphore;
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    public EdgeTtsEngine(String command, int timeoutSeconds, int concurrency) {
        this.command = command;
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT;
        this.concurrency = concurrency > 0 ? concurrency : Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.semaphore = new Semaphore(this.concurrency);
        log.info("EdgeTtsEngine initialized: command={}, timeout={}s, concurrency={}",
                this.command, this.timeoutSeconds, this.concurrency);
    }

    // ──────────────────────────── Sync API ────────────────────────────

    public TtsResult ttsToMp3(String text, String voice, String outputPath) {
        return ttsToMp3(text, voice, outputPath, timeoutSeconds);
    }

    public TtsResult ttsToMp3(String text, String voice, String outputPath, int timeoutSeconds) {
        return withTracking(() -> ttsToMp3Internal(request(text, voice, outputPath), timeoutSeconds));
    }

    // ──────────────────────────── Async API ───────────────────────────

    public CompletableFuture<TtsResult> ttsToMp3Async(String text, String voice, String outputPath) {
        return ttsToMp3Async(text, voice, outputPath, timeoutSeconds);
    }

    public CompletableFuture<TtsResult> ttsToMp3Async(String text, String voice, String outputPath, int timeoutSeconds) {
        return CompletableFuture.supplyAsync(
                () -> withTracking(() -> ttsToMp3Internal(request(text, voice, outputPath), timeoutSeconds)),
                executor
        );
    }

    // ──────────────────────────── Batch API ───────────────────────────

    public List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests) {
        return ttsToMp3Batch(requests, timeoutSeconds);
    }

    public List<TtsResult> ttsToMp3Batch(List<TtsRequest> requests, int timeoutSeconds) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        log.info("Starting batch TTS generation: {} requests, timeout={}s, concurrency={}",
                requests.size(), timeoutSeconds, concurrency);
        long startTime = System.currentTimeMillis();

        var futureMap = new LinkedHashMap<String, CompletableFuture<TtsResult>>();
        for (TtsRequest req : requests) {
            futureMap.put(req.id(), CompletableFuture.supplyAsync(
                    () -> withTracking(() -> ttsToMp3Internal(req, timeoutSeconds)),
                    executor));
        }

        CompletableFuture.allOf(futureMap.values().toArray(CompletableFuture[]::new)).join();

        long elapsed = System.currentTimeMillis() - startTime;
        List<TtsResult> results = new ArrayList<>(requests.size());
        for (TtsRequest req : requests) {
            TtsResult result = futureMap.get(req.id()).getNow(
                    new TtsResult(req.id(), false, req.outputPath(), "Unknown error"));
            results.add(result);
        }

        long successCount = results.stream().filter(TtsResult::success).count();
        log.info("Batch TTS completed: {} success, {} failed, {}ms elapsed",
                successCount, requests.size() - successCount, elapsed);
        return results;
    }

    // ──────────────────────────── Monitoring ──────────────────────────

    public int getActiveTaskCount() {
        return activeTasks.get();
    }

    // ──────────────────────────── Lifecycle ───────────────────────────

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ──────────────────────────── Internal ────────────────────────────

    private static TtsRequest request(String text, String voice, String outputPath) {
        return new TtsRequest(outputPath, text, voice, outputPath);
    }

    private TtsResult withTracking(Supplier<TtsResult> task) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TtsResult(null, false, null, "Interrupted while waiting for concurrency permit");
        }
        activeTasks.incrementAndGet();
        try {
            return task.get();
        } finally {
            activeTasks.decrementAndGet();
            semaphore.release();
        }
    }

    private TtsResult ttsToMp3Internal(TtsRequest req, int timeoutSeconds) {
        if (req.text() == null || req.text().isBlank()) {
            return new TtsResult(req.id(), false, req.outputPath(), "Text is empty");
        }

        File outputFile = new File(req.outputPath());
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        return runProcess(req, outputFile, timeoutSeconds);
    }

    private TtsResult runProcess(TtsRequest req, File outputFile, int timeoutSeconds) {
        Process process;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    command, "--text", req.text(), "--voice", req.voice(), "--write-media", req.outputPath());
            pb.redirectErrorStream(true);
            process = pb.start();
        } catch (IOException e) {
            log.error("Failed to start edge-tts process for id={}", req.id(), e);
            return new TtsResult(req.id(), false, req.outputPath(), "Failed to start process: " + e.getMessage());
        }

        StringBuilder output = new StringBuilder();
        Thread readerThread = startOutputReader(process, output, req.id());
        try {
            return waitForCompletion(req, process, outputFile, output, readerThread, timeoutSeconds);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
            readerThread.interrupt();
        }
    }

    private Thread startOutputReader(Process process, StringBuilder output, String id) {
        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            } catch (IOException ignored) {
            }
        }, "edge-tts-output-reader-" + id);
        readerThread.setDaemon(true);
        readerThread.start();
        return readerThread;
    }

    private TtsResult waitForCompletion(TtsRequest req, Process process, File outputFile,
                                        StringBuilder output, Thread readerThread, int timeoutSeconds) {
        try {
            boolean exited = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!exited) {
                return new TtsResult(req.id(), false, req.outputPath(),
                        "Process timed out after " + timeoutSeconds + "s");
            }

            readerThread.join(READER_JOIN_TIMEOUT_MS);

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("edge-tts failed for id={}, exitCode={}, output: {}", req.id(), exitCode, output);
                return new TtsResult(req.id(), false, req.outputPath(),
                        "edge-tts exitCode=" + exitCode + ", " + output.toString().trim());
            }

            if (!outputFile.exists()) {
                log.error("edge-tts succeeded but file not found for id={}, path: {}", req.id(), req.outputPath());
                return new TtsResult(req.id(), false, req.outputPath(), "Output file not created");
            }

            log.debug("edge-tts succeeded for id={}, path: {}", req.id(), req.outputPath());
            return new TtsResult(req.id(), true, req.outputPath(), null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TtsResult(req.id(), false, req.outputPath(), "Interrupted");
        }
    }
}