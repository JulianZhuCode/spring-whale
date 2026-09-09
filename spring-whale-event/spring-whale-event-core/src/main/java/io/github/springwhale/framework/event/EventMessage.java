package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.context.AuthenticationContext;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.UUID;

/**
 * Unified event message that flows through the entire event lifecycle.
 * <p>The sole business field is {@code data} — all other fields are framework metadata:
 * routing ({@code source}, {@code businessName}, {@code topic}, {@code id}),
 * authentication ({@code authenticationContext}), tracing ({@code traceId}),
 * and retry state ({@code retryCount}, {@code retryEnabled}, {@code errorStack},
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
    private String traceId;
    private MessageType messageType = MessageType.EVENT;
    private Integer retryCount;
    private Boolean retryEnabled;
    private String errorStack;
    private String failListener;
    private Integer version;

    /**
     * Create a shallow copy of this message.
     * <p>Used when a single message is dispatched to multiple listeners in parallel:
     * each listener works on its own copy so failure-path mutations
     * (failListener, errorStack, messageType) cannot race or leak across listeners.</p>
     * <p>The fields are immutable values (String/Integer/Boolean) or a read-only shared
     * reference (authenticationContext), so a shallow copy is safe.</p>
     */
    public EventMessage copy() {
        EventMessage copy = new EventMessage();
        BeanUtils.copyProperties(this, copy);
        return copy;
    }

}