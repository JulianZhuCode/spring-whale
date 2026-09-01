package io.github.springwhale.test.utils;

import io.github.springwhale.framework.core.utils.EdgeTtsEngine;
import io.github.springwhale.framework.core.model.TtsRequest;
import io.github.springwhale.framework.core.model.TtsResult;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EdgeTtsEngine.
 * Requires edge-tts to be installed on the system PATH.
 */
class EdgeTtsUtilTest {

    @TempDir
    Path tempDir;

    private EdgeTtsEngine engine;

    @BeforeEach
    void setUp() {
        engine = new EdgeTtsEngine("edge-tts", 30, 2);
    }

    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
    }

    @Test
    @DisplayName("Should generate MP3 file successfully with default voice")
    void testTtsToMp3Success() {
        String outputPath = tempDir.resolve("output.mp3").toString();

        TtsResult result = engine.ttsToMp3(
                "Hello world, this is a test",
                "zh-CN-XiaoxiaoNeural",
                outputPath
        );

        assertTrue(result.success(), "ttsToMp3 should return true for valid input");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist");
        assertTrue(outputFile.length() > 0, "Output file should not be empty");
    }

    @Test
    @DisplayName("Should auto-create output directory when it does not exist")
    void testTtsToMp3WithNewDirectory() {
        Path newDir = tempDir.resolve("nested").resolve("output");
        String outputPath = newDir.resolve("speech.mp3").toString();

        TtsResult result = engine.ttsToMp3(
                "Directory creation test",
                "zh-CN-XiaoxiaoNeural",
                outputPath
        );

        assertTrue(result.success(), "ttsToMp3 should create directories and succeed");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist in newly created directory");
    }

    @Test
    @DisplayName("Should return false when voice is invalid")
    void testTtsToMp3WithInvalidVoice() {
        String outputPath = tempDir.resolve("invalid-voice.mp3").toString();

        TtsResult result = engine.ttsToMp3(
                "Test with invalid voice",
                "zh-CN-NonExistentVoice",
                outputPath
        );

        assertFalse(result.success(), "ttsToMp3 should return false for invalid voice");
    }

    @Test
    @DisplayName("Should return false when text is empty")
    void testTtsToMp3WithEmptyText() {
        String outputPath = tempDir.resolve("empty.mp3").toString();

        TtsResult result = engine.ttsToMp3(
                "",
                "zh-CN-XiaoxiaoNeural",
                outputPath
        );

        assertFalse(result.success(), "ttsToMp3 should return false for empty text");
    }

    @Test
    @DisplayName("Should generate audio with different voice")
    void testTtsToMp3WithDifferentVoice() {
        String outputPath = tempDir.resolve("en-us-voice.mp3").toString();

        TtsResult result = engine.ttsToMp3(
                "This is a test with a different voice",
                "en-US-AriaNeural",
                outputPath
        );

        assertTrue(result.success(), "ttsToMp3 should succeed with en-US voice");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist");
        assertTrue(outputFile.length() > 0, "Output file should not be empty");
    }

    @Test
    @DisplayName("Should generate audio with configurable timeout")
    void testTtsToMp3WithTimeout() {
        String outputPath = tempDir.resolve("timeout.mp3").toString();

        TtsResult result = engine.ttsToMp3(
                "Testing configurable timeout",
                "zh-CN-XiaoxiaoNeural",
                outputPath,
                60
        );

        assertTrue(result.success(), "ttsToMp3 with custom timeout should succeed");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist");
    }

    @Test
    @DisplayName("Should track active task count")
    void testActiveTaskCount() {
        assertEquals(0, engine.getActiveTaskCount(), "Initial active task count should be 0");

        String outputPath = tempDir.resolve("active-count.mp3").toString();
        engine.ttsToMp3("Active count test", "zh-CN-XiaoxiaoNeural", outputPath);

        assertTrue(engine.getActiveTaskCount() >= 0, "Active task count should be non-negative after completion");
    }

    @Test
    @DisplayName("Batch generation should produce results for all requests")
    void testBatchGeneration() {
        String baseDir = tempDir.resolve("batch").toString();

        TtsRequest req1 = new TtsRequest("1", "First test", "zh-CN-XiaoxiaoNeural", baseDir + "/1.mp3");
        TtsRequest req2 = new TtsRequest("2", "Second test", "zh-CN-XiaoxiaoNeural", baseDir + "/2.mp3");
        TtsRequest req3 = new TtsRequest("3", "", "zh-CN-XiaoxiaoNeural", baseDir + "/3.mp3");

        var results = engine.ttsToMp3Batch(java.util.List.of(req1, req2, req3));

        assertEquals(3, results.size(), "Should return result for each request");
        assertTrue(results.get(0).success(), "First request should succeed");
        assertTrue(results.get(1).success(), "Second request should succeed");
        assertFalse(results.get(2).success(), "Third request with empty text should fail");
    }
}