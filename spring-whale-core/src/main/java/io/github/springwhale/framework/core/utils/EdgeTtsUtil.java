package io.github.springwhale.framework.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Utility for generating speech audio via the edge-tts command-line tool.
 * <p>
 * Requires edge-tts (https://github.com/rany2/edge-tts) to be installed and available on the system PATH.
 * Supports edge-tts v7.x+ with the {@code --write-media} parameter.
 */
@Slf4j
public class EdgeTtsUtil {

    /**
     * Convert text to an MP3 audio file using edge-tts.
     *
     * @param text       the text content to convert to speech
     * @param voice      the voice name, e.g. {@code zh-CN-XiaoxiaoNeural}
     * @param outputPath the target file path for the generated MP3
     * @return {@code true} if the audio file was generated successfully
     */
    public static boolean ttsToMp3(String text, String voice, String outputPath) {
        List<String> cmd = List.of(
                "edge-tts",
                "--text", text,
                "--voice", voice,
                "--write-media", outputPath
        );

        // Ensure the output directory exists
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process process;
        try {
            process = pb.start();

            // Consume process output to prevent buffer deadlock
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("edge-tts failed with exitCode={}, output: {}", exitCode, output);
                return false;
            }

            boolean exists = outputFile.exists();
            if (!exists) {
                log.error("edge-tts succeeded but output file does not exist: {}, output: {}", outputPath, output);
            }
            return exists;
        } catch (IOException | InterruptedException e) {
            log.error("Failed to invoke edge-tts for speech synthesis", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
}