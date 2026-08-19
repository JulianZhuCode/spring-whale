package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.EventPublisher;
import io.github.springwhale.framework.event.recovery.EventRetryTask;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(TestRecoveryConfiguration.class)
class EventRetryTaskTest {

    @Autowired
    private EventConsumeFailedRecordDao recordDao;

    @Autowired
    private EventRetryTask eventRetryTask;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM event_consume_failed_record");
        }
        reset(eventPublisher);
    }

    @Test
    @DisplayName("Should retry pending records and transition status")
    void testRetryPendingRecords() throws Exception {
        EventConsumeFailedRecord record = new EventConsumeFailedRecord();
        record.setId("RETRY-001");
        record.setMessageId("msg-retry-001");
        record.setSource("test");
        record.setBusinessName("test.business");
        record.setListenerName("testListener");
        record.setTopic("test-topic");
        record.setRawMessage("{}");
        record.setStatus(EventConsumeStatus.PENDING_RETRY);
        record.setRetryCount(1);
        record.setNextRetryTime(LocalDateTime.now().minusMinutes(1));
        recordDao.save(record);

        eventRetryTask.retry();

        verify(eventPublisher, times(1)).publish(any());

        Optional<EventConsumeFailedRecord> updated = recordDao.findById("RETRY-001");
        assertTrue(updated.isPresent());
        assertEquals(EventConsumeStatus.RETRYING, updated.get().getStatus());
    }

    @Test
    @DisplayName("Should not retry when no pending records")
    void testRetryNoPendingRecords() {
        eventRetryTask.retry();

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Cleanup should remove terminal records older than retention days")
    void testCleanupTerminalRecords() throws Exception {
        EventConsumeFailedRecord record = new EventConsumeFailedRecord();
        record.setId("CLEANUP-001");
        record.setMessageId("msg-cleanup-001");
        record.setSource("test");
        record.setBusinessName("test.business");
        record.setListenerName("testListener");
        record.setTopic("test-topic");
        record.setRawMessage("{}");
        record.setStatus(EventConsumeStatus.DISCARDED);
        record.setRetryCount(0);
        recordDao.save(record);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE event_consume_failed_record SET create_time = ? WHERE id = ?")) {
            ps.setObject(1, LocalDateTime.now().minusDays(60));
            ps.setString(2, "CLEANUP-001");
            ps.executeUpdate();
        }

        eventRetryTask.cleanup();

        Optional<EventConsumeFailedRecord> deleted = recordDao.findById("CLEANUP-001");
        assertFalse(deleted.isPresent());
    }

    @Test
    @DisplayName("Cleanup should skip records within retention period")
    void testCleanupSkipRecentRecords() {
        EventConsumeFailedRecord record = new EventConsumeFailedRecord();
        record.setId("CLEANUP-002");
        record.setMessageId("msg-cleanup-002");
        record.setSource("test");
        record.setBusinessName("test.business");
        record.setListenerName("testListener");
        record.setTopic("test-topic");
        record.setRawMessage("{}");
        record.setStatus(EventConsumeStatus.DISCARDED);
        record.setRetryCount(0);
        recordDao.save(record);

        eventRetryTask.cleanup();

        Optional<EventConsumeFailedRecord> stillExists = recordDao.findById("CLEANUP-002");
        assertTrue(stillExists.isPresent());
    }

    @Test
    @DisplayName("Shutdown should gracefully stop executor")
    void testShutdown() {
        assertDoesNotThrow(() -> eventRetryTask.shutdown());
    }
}