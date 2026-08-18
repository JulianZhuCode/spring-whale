package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

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
