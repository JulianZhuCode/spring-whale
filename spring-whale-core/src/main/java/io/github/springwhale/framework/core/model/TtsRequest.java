package io.github.springwhale.framework.core.model;

/**
 * A TTS generation request.
 *
 * @param id         unique identifier for tracking this request
 * @param text       the text content to convert to speech
 * @param voice      the voice name, e.g. {@code zh-CN-XiaoxiaoNeural}
 * @param outputPath the target file path for the generated audio
 */
public record TtsRequest(String id, String text, String voice, String outputPath) {
}