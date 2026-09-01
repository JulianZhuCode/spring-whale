package io.github.springwhale.framework.core.model;

import org.jspecify.annotations.Nullable;

/**
 * A TTS generation result.
 *
 * @param id           unique identifier matching the original request
 * @param success      whether the audio was generated successfully
 * @param outputPath   the file path of the generated audio
 * @param errorMessage error details on failure, or {@code null} on success
 */
public record TtsResult(String id, boolean success, String outputPath, @Nullable String errorMessage) {
}