package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.PublishOption;
import io.github.springwhale.framework.event.kafka.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherPartitionKeyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventProperties eventProperties = new EventProperties();

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private KafkaEventPublisher publisher;

    static class OrderEvent {
        private String orderId;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(kafkaTemplate);
        publisher = new KafkaEventPublisher(eventProperties, objectMapper,
                Collections.emptyList(), kafkaTemplate);
    }

    @SuppressWarnings("unchecked")
    private static CompletableFuture<SendResult<String, String>> successFuture() {
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.complete(null);
        return future;
    }

    @Test
    @DisplayName("Should use partitionKey as Kafka message key when provided")
    void testPartitionKeyUsedAsKafkaKey() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(successFuture());

        OrderEvent event = new OrderEvent();
        event.setOrderId("ORDER-001");

        PublishOption option = PublishOption.builder()
                .topic("order-topic")
                .businessName("order.created")
                .partitionKey("ORDER-001")
                .build();

        publisher.publish(event, option);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), anyString());
        assertEquals("ORDER-001", keyCaptor.getValue());
    }

    @Test
    @DisplayName("Should use message ID as Kafka key when partitionKey is null")
    void testNullPartitionKeyFallsBackToMessageId() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(successFuture());

        OrderEvent event = new OrderEvent();
        event.setOrderId("ORDER-002");

        PublishOption option = PublishOption.builder()
                .topic("order-topic")
                .businessName("order.created")
                .partitionKey(null)
                .build();

        publisher.publish(event, option);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), anyString());
        String key = keyCaptor.getValue();
        assertNotNull(key);
        assertTrue(key.length() > 0);
    }

    @Test
    @DisplayName("Should use message ID as Kafka key when partitionKey is not set")
    void testNoPartitionKeyFallsBackToMessageId() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(successFuture());

        OrderEvent event = new OrderEvent();
        event.setOrderId("ORDER-003");

        publisher.publish(event);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), anyString());
        String key = keyCaptor.getValue();
        assertNotNull(key);
        assertTrue(key.length() > 0);
    }

    @Test
    @DisplayName("Should use same partitionKey for all events in batch with same key")
    void testBatchPublishWithPartitionKey() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(successFuture());

        OrderEvent event1 = new OrderEvent();
        event1.setOrderId("ORDER-004");
        OrderEvent event2 = new OrderEvent();
        event2.setOrderId("ORDER-005");

        PublishOption option = PublishOption.builder()
                .partitionKey("SHARED-KEY")
                .build();

        publisher.publishBatch(java.util.List.of(event1, event2), option);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(2)).send(anyString(), keyCaptor.capture(), anyString());
        for (String key : keyCaptor.getAllValues()) {
            assertEquals("SHARED-KEY", key);
        }
    }

    @Test
    @DisplayName("Should publish with partitionKey in PublishOption without topic/businessName")
    void testPartitionKeyOnlyOption() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(successFuture());

        OrderEvent event = new OrderEvent();
        event.setOrderId("ORDER-006");

        PublishOption option = PublishOption.builder()
                .partitionKey("ORDER-006")
                .build();

        publisher.publish(event, option);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), anyString());
        assertEquals("ORDER-006", keyCaptor.getValue());
    }
}