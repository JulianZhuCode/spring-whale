package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.recovery.kafka.KafkaEventConsumeFailedListener;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import io.github.springwhale.framework.event.recovery.EventConsumeTerminalHandler;
import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import io.github.springwhale.framework.event.recovery.util.EventFailedRecordIdGenerator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.Acknowledgment;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaEventConsumeFailedListenerTest {

    private EventConsumeFailedRecordDao recordDao;
    private EventProperties eventProperties;
    private ObjectMapper objectMapper;
    private RetryStrategyRegistry retryStrategyRegistry;
    private KafkaEventConsumeFailedListener listener;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("kafka-failed-test")
                .addScript("classpath:schema.sql")
                .build();
        recordDao = new EventConsumeFailedRecordDao(new JdbcTemplate(dataSource));
        eventProperties = new EventProperties();
        objectMapper = new ObjectMapper();
        retryStrategyRegistry = new RetryStrategyRegistry(Collections.emptyMap());

        listener = new KafkaEventConsumeFailedListener(
                recordDao, eventProperties, objectMapper, retryStrategyRegistry,
                Collections.emptyList(), Collections.emptyList());
    }

    @Test
    @DisplayName("Should create retry record for failed message with retry enabled")
    void testCreateRetryRecord() throws Exception {
        EventMessage failMessage = new EventMessage();
        failMessage.setId("msg-001");
        failMessage.setSource("test-service");
        failMessage.setBusinessName("order.created");
        failMessage.setTopic("test-topic");
        failMessage.setData("{\"orderId\":\"ORDER-001\"}");
        failMessage.setMessageType(MessageType.FAIL);
        failMessage.setRetryEnabled(true);
        failMessage.setRetryCount(0);
        failMessage.setFailListener("orderListener");
        failMessage.setErrorStack("java.lang.RuntimeException: test error");

        String rawPayload = objectMapper.writeValueAsString(failMessage);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);
        Acknowledgment ack = mock(Acknowledgment.class);

        listener.listenerFailed(record, ack);

        String expectedId = EventFailedRecordIdGenerator.generate("msg-001", "orderListener");
        Optional<EventConsumeFailedRecord> saved = recordDao.findById(expectedId);
        assertTrue(saved.isPresent());
        assertEquals(EventConsumeStatus.PENDING_RETRY, saved.get().getStatus());
        assertEquals(1, saved.get().getRetryCount());
        assertNotNull(saved.get().getNextRetryTime());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("Should create discard record when retry disabled")
    void testCreateDiscardRecord() throws Exception {
        EventMessage failMessage = new EventMessage();
        failMessage.setId("msg-002");
        failMessage.setSource("test-service");
        failMessage.setBusinessName("order.created");
        failMessage.setTopic("test-topic");
        failMessage.setData("{\"orderId\":\"ORDER-002\"}");
        failMessage.setMessageType(MessageType.FAIL);
        failMessage.setRetryEnabled(false);
        failMessage.setRetryCount(0);
        failMessage.setFailListener("orderListener");

        String rawPayload = objectMapper.writeValueAsString(failMessage);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);
        Acknowledgment ack = mock(Acknowledgment.class);

        listener.listenerFailed(record, ack);

        String expectedId = EventFailedRecordIdGenerator.generate("msg-002", "orderListener");
        Optional<EventConsumeFailedRecord> saved = recordDao.findById(expectedId);
        assertTrue(saved.isPresent());
        assertEquals(EventConsumeStatus.DISCARDED, saved.get().getStatus());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("Should update retry record on retry success")
    void testUpdateRetrySuccess() throws Exception {
        String recordId = EventFailedRecordIdGenerator.generate("msg-003", "orderListener");

        EventConsumeFailedRecord existing = new EventConsumeFailedRecord();
        existing.setId(recordId);
        existing.setMessageId("msg-003");
        existing.setSource("test-service");
        existing.setBusinessName("order.created");
        existing.setListenerName("orderListener");
        existing.setTopic("test-topic");
        existing.setRawMessage("{}");
        existing.setStatus(EventConsumeStatus.PENDING_RETRY);
        existing.setRetryCount(1);
        existing.setNextRetryTime(LocalDateTime.now().plusMinutes(5));
        recordDao.save(existing);

        EventMessage failMessage = new EventMessage();
        failMessage.setId("msg-003");
        failMessage.setSource("test-service");
        failMessage.setBusinessName("order.created");
        failMessage.setTopic("test-topic");
        failMessage.setData("{}");
        failMessage.setMessageType(MessageType.FAIL);
        failMessage.setRetryEnabled(true);
        failMessage.setRetryCount(1);
        failMessage.setRetrySuccess(true);
        failMessage.setFailListener("orderListener");

        String rawPayload = objectMapper.writeValueAsString(failMessage);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);
        Acknowledgment ack = mock(Acknowledgment.class);

        listener.listenerFailed(record, ack);

        Optional<EventConsumeFailedRecord> updated = recordDao.findById(recordId);
        assertTrue(updated.isPresent());
        assertEquals(EventConsumeStatus.REPLAY_SUCCESS, updated.get().getStatus());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("Should set FINAL_FAILED when retries exhausted")
    void testRetriesExhausted() throws Exception {
        String recordId = EventFailedRecordIdGenerator.generate("msg-004", "orderListener");

        EventConsumeFailedRecord existing = new EventConsumeFailedRecord();
        existing.setId(recordId);
        existing.setMessageId("msg-004");
        existing.setSource("test-service");
        existing.setBusinessName("order.created");
        existing.setListenerName("orderListener");
        existing.setTopic("test-topic");
        existing.setRawMessage("{}");
        existing.setStatus(EventConsumeStatus.PENDING_RETRY);
        existing.setRetryCount(2);
        existing.setNextRetryTime(LocalDateTime.now().plusMinutes(5));
        recordDao.save(existing);

        EventMessage failMessage = new EventMessage();
        failMessage.setId("msg-004");
        failMessage.setSource("test-service");
        failMessage.setBusinessName("order.created");
        failMessage.setTopic("test-topic");
        failMessage.setData("{}");
        failMessage.setMessageType(MessageType.FAIL);
        failMessage.setRetryEnabled(true);
        failMessage.setRetryCount(eventProperties.getMaxRetries());
        failMessage.setRetrySuccess(false);
        failMessage.setFailListener("orderListener");

        String rawPayload = objectMapper.writeValueAsString(failMessage);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);
        Acknowledgment ack = mock(Acknowledgment.class);

        listener.listenerFailed(record, ack);

        Optional<EventConsumeFailedRecord> updated = recordDao.findById(recordId);
        assertTrue(updated.isPresent());
        assertEquals(EventConsumeStatus.FINAL_FAILED, updated.get().getStatus());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("Should ack and skip non-FAIL message type")
    void testSkipNonFailMessage() throws Exception {
        EventMessage eventMessage = new EventMessage();
        eventMessage.setId("msg-005");
        eventMessage.setSource("test-service");
        eventMessage.setBusinessName("order.created");
        eventMessage.setTopic("test-topic");
        eventMessage.setData("{}");
        eventMessage.setMessageType(MessageType.EVENT);

        String rawPayload = objectMapper.writeValueAsString(eventMessage);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);
        Acknowledgment ack = mock(Acknowledgment.class);

        listener.listenerFailed(record, ack);

        verify(ack).acknowledge();
        String expectedId = EventFailedRecordIdGenerator.generate("msg-005", "orderListener");
        assertFalse(recordDao.findById(expectedId).isPresent());
    }

    @Test
    @DisplayName("Should not ack when deserialization fails")
    void testNotAckOnDeserializationFailure() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", "invalid-json");
        Acknowledgment ack = mock(Acknowledgment.class);

        listener.listenerFailed(record, ack);

        verify(ack, never()).acknowledge();
    }

    @Test
    @DisplayName("Should invoke terminal handlers on FINAL_FAILED")
    void testTerminalHandlersOnFinalFailed() throws Exception {
        EventConsumeTerminalHandler handler = mock(EventConsumeTerminalHandler.class);

        KafkaEventConsumeFailedListener listenerWithHandler = new KafkaEventConsumeFailedListener(
                recordDao, eventProperties, objectMapper, retryStrategyRegistry,
                Collections.emptyList(), List.of(handler));

        String recordId = EventFailedRecordIdGenerator.generate("msg-006", "orderListener");

        EventConsumeFailedRecord existing = new EventConsumeFailedRecord();
        existing.setId(recordId);
        existing.setMessageId("msg-006");
        existing.setSource("test-service");
        existing.setBusinessName("order.created");
        existing.setListenerName("orderListener");
        existing.setTopic("test-topic");
        existing.setRawMessage("{}");
        existing.setStatus(EventConsumeStatus.PENDING_RETRY);
        existing.setRetryCount(2);
        existing.setNextRetryTime(LocalDateTime.now().plusMinutes(5));
        recordDao.save(existing);

        EventMessage failMessage = new EventMessage();
        failMessage.setId("msg-006");
        failMessage.setSource("test-service");
        failMessage.setBusinessName("order.created");
        failMessage.setTopic("test-topic");
        failMessage.setData("{}");
        failMessage.setMessageType(MessageType.FAIL);
        failMessage.setRetryEnabled(true);
        failMessage.setRetryCount(eventProperties.getMaxRetries());
        failMessage.setRetrySuccess(false);
        failMessage.setFailListener("orderListener");

        String rawPayload = objectMapper.writeValueAsString(failMessage);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("test-topic", 0, 0, "key", rawPayload);
        Acknowledgment ack = mock(Acknowledgment.class);

        listenerWithHandler.listenerFailed(record, ack);

        verify(handler).onFinalFailed(any(EventConsumeFailedRecord.class));
    }
}