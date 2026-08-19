package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.PublishOption;
import io.github.springwhale.framework.event.kafka.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestEventConfiguration.class)
class EventPublisherBoundaryTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventProperties eventProperties;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaEventPublisher publisher;

    static class TestEvent {
        private String data;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(eventProperties, objectMapper,
                Collections.emptyList(), kafkaTemplate);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when publishing null event")
    void testPublishNullEvent() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> publisher.publish((Object) null));
        assertEquals("event must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when publishing null EventMessage")
    void testPublishNullEventMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> publisher.publish((EventMessage) null));
        assertEquals("message must not be null", ex.getMessage());
    }

    @Test
    @DisplayName("Should use default topic and class name as business name when no annotation")
    void testPublishWithDefaults() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        TestEvent event = new TestEvent();
        event.setData("test");

        assertDoesNotThrow(() -> publisher.publish(event));
    }

    @Test
    @DisplayName("Should handle PublishOption with only topic set")
    void testPublishOptionOnlyTopic() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        TestEvent event = new TestEvent();
        event.setData("test");

        PublishOption option = PublishOption.builder()
                .topic("override-topic")
                .businessName(null)
                .build();

        assertDoesNotThrow(() -> publisher.publish(event, option));
    }

    @Test
    @DisplayName("Should handle PublishOption with only businessName set")
    void testPublishOptionOnlyBusinessName() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        TestEvent event = new TestEvent();
        event.setData("test");

        PublishOption option = PublishOption.builder()
                .topic(null)
                .businessName("override.business")
                .build();

        assertDoesNotThrow(() -> publisher.publish(event, option));
    }

    @Test
    @DisplayName("Should handle EventMessage with minimal fields")
    void testPublishMinimalEventMessage() throws Exception {
        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);
        future.complete(null);

        EventMessage message = new EventMessage();
        message.setSource("test");
        message.setBusinessName("test");
        message.setTopic("test");
        message.setData("{}");

        assertDoesNotThrow(() -> publisher.publish(message));
    }
}