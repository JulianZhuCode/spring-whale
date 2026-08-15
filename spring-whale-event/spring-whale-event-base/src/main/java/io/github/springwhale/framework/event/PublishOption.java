package io.github.springwhale.framework.event;

import lombok.Builder;

@Builder
public record PublishOption(String topic, String businessName) {

}
