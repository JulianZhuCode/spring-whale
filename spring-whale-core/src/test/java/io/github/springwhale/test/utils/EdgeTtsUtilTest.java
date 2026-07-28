package io.github.springwhale.test.utils;

import io.github.springwhale.framework.core.utils.EdgeTtsUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EdgeTtsUtil.
 * Requires edge-tts to be installed on the system PATH.
 */
class EdgeTtsUtilTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should generate MP3 file successfully with default voice")
    void testTtsToMp3Success() {
        String outputPath = tempDir.resolve("output.mp3").toString();

        boolean result = EdgeTtsUtil.ttsToMp3(
                "Hello world, this is a test",
                "zh-CN-XiaoxiaoNeural",
                outputPath
        );

        assertTrue(result, "ttsToMp3 should return true for valid input");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist");
        assertTrue(outputFile.length() > 0, "Output file should not be empty");
    }

    @Test
    @DisplayName("Should auto-create output directory when it does not exist")
    void testTtsToMp3WithNewDirectory() {
        Path newDir = tempDir.resolve("nested").resolve("output");
        String outputPath = newDir.resolve("speech.mp3").toString();

        boolean result = EdgeTtsUtil.ttsToMp3(
                "Directory creation test",
                "zh-CN-XiaoxiaoNeural",
                outputPath
        );

        assertTrue(result, "ttsToMp3 should create directories and succeed");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist in newly created directory");
    }

    @Test
    @DisplayName("Should return false when voice is invalid")
    void testTtsToMp3WithInvalidVoice() {
        String outputPath = tempDir.resolve("invalid-voice.mp3").toString();

        boolean result = EdgeTtsUtil.ttsToMp3(
                "Test with invalid voice",
                "zh-CN-NonExistentVoice",
                outputPath
        );

        assertFalse(result, "ttsToMp3 should return false for invalid voice");
    }

    @Test
    @DisplayName("Should handle empty text gracefully by generating silent audio")
    void testTtsToMp3WithEmptyText() {
        String outputPath = tempDir.resolve("empty.mp3").toString();

        boolean result = EdgeTtsUtil.ttsToMp3(
                "",
                "zh-CN-XiaoxiaoNeural",
                outputPath
        );

        // edge-tts handles empty text by generating a silent audio file
        assertTrue(result, "ttsToMp3 should handle empty text gracefully");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist for empty text input");
    }

    @Test
    @DisplayName("Should generate audio with different voice")
    void testTtsToMp3WithDifferentVoice() {
        String outputPath = tempDir.resolve("en-us-voice.mp3").toString();

        boolean result = EdgeTtsUtil.ttsToMp3(
                "This is a test with a different voice",
                "en-US-AriaNeural",
                outputPath
        );

        assertTrue(result, "ttsToMp3 should succeed with en-US voice");
        File outputFile = new File(outputPath);
        assertTrue(outputFile.exists(), "Output file should exist");
        assertTrue(outputFile.length() > 0, "Output file should not be empty");
    }
}