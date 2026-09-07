package io.github.springwhale.framework.event;

import lombok.Builder;

/**
 * Optional overrides for event publishing.
 * <p>Non-null fields take precedence over annotation values and property defaults.
 * Null fields fall back to the normal resolution logic.</p>
 *
 * <ul>
 *   <li>{@code topic} — override the target topic (null = use default)</li>
 *   <li>{@code businessName} — override the business name (null = derive from annotation or class name)</li>
 *   <li>{@code partitionKey} — partition key for ordered message delivery (Kafka key / RabbitMQ routing key);
 *       null means no explicit ordering. Local mode ignores this value.</li>
 * </ul>
 */
@Builder
public record PublishOption(String topic, String businessName, String partitionKey) {

}
