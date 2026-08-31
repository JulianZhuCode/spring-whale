package io.github.springwhale.test.event;

import io.github.springwhale.framework.event.BatchPublishException;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(TestEventConfiguration.class)
class EventPublisherBatchTest {

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

    @SuppressWarnings("unchecked")
    private static CompletableFuture<SendResult<String, String>> failedFuture() {
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka send failed"));
        return future;
    }

    @Test
    @DisplayName("Should publish all events in batch successfully")
    void testPublishBatchAllSuccess() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(successFuture());

        TestEvent event1 = new TestEvent();
        event1.setData("data1");
        TestEvent event2 = new TestEvent();
        event2.setData("data2");
        TestEvent event3 = new TestEvent();
        event3.setData("data3");

        assertDoesNotThrow(() -> publisher.publishBatch(List.of(event1, event2, event3)));
    }

    @Test
    @DisplayName("Should throw BatchPublishException when some events fail")
    void testPublishBatchPartialFailure() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(successFuture())
                .thenReturn(failedFuture())
                .thenReturn(successFuture());

        TestEvent event1 = new TestEvent();
        event1.setData("data1");
        TestEvent event2 = new TestEvent();
        event2.setData("data2");
        TestEvent event3 = new TestEvent();
        event3.setData("data3");

        BatchPublishException ex = assertThrows(BatchPublishException.class,
                () -> publisher.publishBatch(List.of(event1, event2, event3)));

        assertEquals(1, ex.getFailures().size());
        assertEquals(1, ex.getFailures().get(0).getIndex());
        assertEquals(event2, ex.getFailures().get(0).getEvent());
        assertTrue(ex.getFailures().get(0).getCause().getMessage().contains("Failed to publish event"));
    }

    @Test
    @DisplayName("Should collect all failures when multiple events fail")
    void testPublishBatchAllFail() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failedFuture());

        TestEvent event1 = new TestEvent();
        event1.setData("data1");
        TestEvent event2 = new TestEvent();
        event2.setData("data2");

        BatchPublishException ex = assertThrows(BatchPublishException.class,
                () -> publisher.publishBatch(List.of(event1, event2)));

        assertEquals(2, ex.getFailures().size());
        assertEquals(0, ex.getFailures().get(0).getIndex());
        assertEquals(1, ex.getFailures().get(1).getIndex());
    }

    @Test
    @DisplayName("Should throw BatchPublishException with PublishOption on partial failure")
    void testPublishBatchWithOptionPartialFailure() throws Exception {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(successFuture())
                .thenReturn(failedFuture());

        TestEvent event1 = new TestEvent();
        event1.setData("data1");
        TestEvent event2 = new TestEvent();
        event2.setData("data2");

        PublishOption option = PublishOption.builder()
                .topic("batch-topic")
                .businessName("batch.biz")
                .build();

        BatchPublishException ex = assertThrows(BatchPublishException.class,
                () -> publisher.publishBatch(List.of(event1, event2), option));

        assertEquals(1, ex.getFailures().size());
        assertEquals(1, ex.getFailures().get(0).getIndex());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when batch is null")
    void testPublishBatchNull() {
        assertThrows(IllegalArgumentException.class, () -> publisher.publishBatch(null));
        assertThrows(IllegalArgumentException.class, () -> publisher.publishBatch(null, PublishOption.builder().build()));
    }

    @Test
    @DisplayName("Should handle empty batch without error")
    void testPublishBatchEmpty() {
        assertDoesNotThrow(() -> publisher.publishBatch(Collections.emptyList()));
    }
}