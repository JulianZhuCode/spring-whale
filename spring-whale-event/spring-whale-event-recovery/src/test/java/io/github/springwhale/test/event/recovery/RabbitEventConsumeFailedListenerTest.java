package io.github.springwhale.test.event.recovery;

import com.rabbitmq.client.Channel;
import io.github.springwhale.framework.event.EventMessage;
import io.github.springwhale.framework.event.EventProperties;
import io.github.springwhale.framework.event.MessageType;
import io.github.springwhale.framework.event.RetryStrategyRegistry;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import io.github.springwhale.framework.event.recovery.rabbit.RabbitEventConsumeFailedListener;
import io.github.springwhale.framework.event.recovery.util.EventFailedRecordIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RabbitEventConsumeFailedListenerTest {

    private EventConsumeFailedRecordDao recordDao;
    private EventProperties eventProperties;
    private ObjectMapper objectMapper;
    private RetryStrategyRegistry retryStrategyRegistry;
    private RabbitEventConsumeFailedListener listener;
    private Channel channel;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("rabbit-failed-test")
                .addScript("classpath:schema.sql")
                .build();
        recordDao = new EventConsumeFailedRecordDao(dataSource);
        eventProperties = new EventProperties();
        objectMapper = new ObjectMapper();
        retryStrategyRegistry = new RetryStrategyRegistry(Collections.emptyMap());
        channel = mock(Channel.class);

        listener = new RabbitEventConsumeFailedListener(
                recordDao, eventProperties, objectMapper, retryStrategyRegistry,
                Collections.emptyList(), Collections.emptyList());
    }

    @Test
    @DisplayName("Should create retry record for failed message and ack")
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

        listener.listenerFailed(rawPayload, channel, 1L);

        String expectedId = EventFailedRecordIdGenerator.generate("msg-001", "orderListener");
        Optional<EventConsumeFailedRecord> saved = recordDao.findById(expectedId);
        assertTrue(saved.isPresent());
        assertEquals(EventConsumeStatus.PENDING_RETRY, saved.get().getStatus());
        assertEquals(1, saved.get().getRetryCount());
        assertNotNull(saved.get().getNextRetryTime());
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("Should create discard record when retry disabled and ack")
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

        listener.listenerFailed(rawPayload, channel, 1L);

        String expectedId = EventFailedRecordIdGenerator.generate("msg-002", "orderListener");
        Optional<EventConsumeFailedRecord> saved = recordDao.findById(expectedId);
        assertTrue(saved.isPresent());
        assertEquals(EventConsumeStatus.DISCARDED, saved.get().getStatus());
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("Should update retry record on retry success and ack")
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

        listener.listenerFailed(rawPayload, channel, 1L);

        Optional<EventConsumeFailedRecord> updated = recordDao.findById(recordId);
        assertTrue(updated.isPresent());
        assertEquals(EventConsumeStatus.REPLAY_SUCCESS, updated.get().getStatus());
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("Should set FINAL_FAILED when retries exhausted and ack")
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

        listener.listenerFailed(rawPayload, channel, 1L);

        Optional<EventConsumeFailedRecord> updated = recordDao.findById(recordId);
        assertTrue(updated.isPresent());
        assertEquals(EventConsumeStatus.FINAL_FAILED, updated.get().getStatus());
        verify(channel).basicAck(1L, false);
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

        listener.listenerFailed(rawPayload, channel, 1L);

        String expectedId = EventFailedRecordIdGenerator.generate("msg-005", "orderListener");
        Optional<EventConsumeFailedRecord> saved = recordDao.findById(expectedId);
        assertTrue(saved.isEmpty());
        verify(channel).basicAck(1L, false);
    }

    @Test
    @DisplayName("Should not ack when processing fails")
    void testNotAckOnFailure() throws Exception {
        String rawPayload = "invalid-json";

        listener.listenerFailed(rawPayload, channel, 1L);

        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should not ack when database save fails")
    void testNotAckOnDatabaseFailure() throws Exception {
        EventMessage failMessage = new EventMessage();
        failMessage.setId("msg-007");
        failMessage.setSource("test-service");
        failMessage.setBusinessName("order.created");
        failMessage.setTopic("test-topic");
        failMessage.setData("{}");
        failMessage.setMessageType(MessageType.FAIL);
        failMessage.setRetryEnabled(true);
        failMessage.setRetryCount(0);
        failMessage.setFailListener("orderListener");

        String rawPayload = objectMapper.writeValueAsString(failMessage);

        doThrow(new IOException("Channel error")).when(channel).basicAck(anyLong(), anyBoolean());

        listener.listenerFailed(rawPayload, channel, 1L);

        verify(channel).basicAck(1L, false);
    }
}