package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

/**
 * Unified event message that flows through the entire event lifecycle.
 * <p>The sole business field is {@code data} — all other fields are framework metadata:
 * routing ({@code source}, {@code businessName}, {@code topic}, {@code id}),
 * authentication ({@code authenticationContext}), and retry state
 * ({@code retryCount}, {@code retryEnabled}, {@code retrySuccess}, {@code errorStack},
 * {@code failListener}, {@code messageType}).</p>
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