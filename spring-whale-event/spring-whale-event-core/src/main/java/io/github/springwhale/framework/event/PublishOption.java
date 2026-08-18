package io.github.springwhale.framework.event;

import lombok.Builder;

/**
 * Optional overrides for event publishing.
 * <p>Non-null fields take precedence over annotation values and property defaults.
 * Null fields fall back to the normal resolution logic.</p>
 *
 * @param topic        override the target topic (null = use default)
 * @param businessName override the business name (null = derive from annotation or class name)
 */
@Builder
public record PublishOption(String topic, String businessName) {

}