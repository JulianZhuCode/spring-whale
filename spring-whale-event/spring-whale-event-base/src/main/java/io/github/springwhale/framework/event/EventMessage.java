package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

/**
 * Unified event message that flows through the entire event lifecycle.
 * <p>Core fields ({@code source}, {@code data}, {@code businessName}, {@code topic})
 * represent the business event payload. The retry-related fields
 * ({@code retryCount}, {@code retryEnabled}, {@code retrySuccess}, {@code errorStack},
 * {@code failListener}) are internal framework metadata used for the retry mechanism
 * and are transparent to business listeners.</p>
 */
@Data
public class EventMessage {

    private String id = UUID.randomUUID().toString();
    @NotBlank(message = "source is not null")
    private String source;
    @NotBlank(message = "data is not null")
    private String data;
    @NotBlank(message = "businessName is not null")
    private String businessName;
    @NotBlank(message = "topic is not null")
    private String topic;
    private AuthenticationContext authenticationContext;
    private MessageType messageType = MessageType.EVENT;
    private Integer retryCount;
    private Boolean retryEnabled;
    private Boolean retrySuccess;
    private String errorStack;
    private String failListener;

}