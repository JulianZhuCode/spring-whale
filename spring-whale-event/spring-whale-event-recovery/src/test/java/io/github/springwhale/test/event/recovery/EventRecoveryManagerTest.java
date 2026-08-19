package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.recovery.EventRecoveryManager;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import io.github.springwhale.framework.event.recovery.enums.EventConsumeStatus;
import io.github.springwhale.framework.event.recovery.model.EventConsumeFailedRecord;
import io.github.springwhale.framework.event.recovery.model.ResetRetryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestRecoveryConfiguration.class)
class EventRecoveryManagerTest {

    @Autowired
    private EventConsumeFailedRecordDao recordDao;

    @Autowired
    private EventRecoveryManager recoveryManager;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM event_consume_failed_record");
        }
    }

    private EventConsumeFailedRecord createRecord(String id, String messageId,
                                                   EventConsumeStatus status) {
        EventConsumeFailedRecord record = new EventConsumeFailedRecord();
        record.setId(id);
        record.setMessageId(messageId);
        record.setSource("test");
        record.setBusinessName("test.business");
        record.setListenerName("testListener");
        record.setTopic("test-topic");
        record.setRawMessage("{}");
        record.setStatus(status);
        record.setRetryCount(status == EventConsumeStatus.FINAL_FAILED ? 3 : 0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        recordDao.save(record);
        return record;
    }

    @Test
    @DisplayName("Should reset retry by ids")
    void testResetRetryByIds() {
        createRecord("REC-001", "msg-001", EventConsumeStatus.FINAL_FAILED);
        createRecord("REC-002", "msg-002", EventConsumeStatus.FINAL_FAILED);

        ResetRetryRequest request = new ResetRetryRequest();
        request.setIds(List.of("REC-001", "REC-002"));

        int affected = recoveryManager.resetRetry(request);
        assertEquals(2, affected);

        Optional<EventConsumeFailedRecord> r1 = recordDao.findById("REC-001");
        assertTrue(r1.isPresent());
        assertEquals(EventConsumeStatus.PENDING_RETRY, r1.get().getStatus());
    }

    @Test
    @DisplayName("Should reset retry by terminal status")
    void testResetRetryByStatus() {
        createRecord("REC-003", "msg-003", EventConsumeStatus.FINAL_FAILED);
        createRecord("REC-004", "msg-004", EventConsumeStatus.DISCARDED);

        ResetRetryRequest request = new ResetRetryRequest();
        request.setStatus(EventConsumeStatus.FINAL_FAILED);

        int affected = recoveryManager.resetRetry(request);
        assertEquals(1, affected);
    }

    @Test
    @DisplayName("Should reset retry count when resetRetryCount is true")
    void testResetRetryCountTrue() {
        createRecord("REC-005", "msg-005", EventConsumeStatus.FINAL_FAILED);

        ResetRetryRequest request = new ResetRetryRequest();
        request.setIds(List.of("REC-005"));
        request.setResetRetryCount(true);

        recoveryManager.resetRetry(request);

        Optional<EventConsumeFailedRecord> record = recordDao.findById("REC-005");
        assertTrue(record.isPresent());
        assertEquals(0, record.get().getRetryCount());
    }

    @Test
    @DisplayName("Should keep retry count when resetRetryCount is false")
    void testResetRetryCountFalse() {
        createRecord("REC-006", "msg-006", EventConsumeStatus.FINAL_FAILED);

        ResetRetryRequest request = new ResetRetryRequest();
        request.setIds(List.of("REC-006"));
        request.setResetRetryCount(false);

        recoveryManager.resetRetry(request);

        Optional<EventConsumeFailedRecord> record = recordDao.findById("REC-006");
        assertTrue(record.isPresent());
        assertEquals(EventConsumeStatus.PENDING_RETRY, record.get().getStatus());
        assertEquals(3, record.get().getRetryCount());
    }

    @Test
    @DisplayName("Should throw when no filter criteria provided")
    void testNoFilterCriteria() {
        ResetRetryRequest request = new ResetRetryRequest();

        assertThrows(IllegalArgumentException.class,
                () -> recoveryManager.resetRetry(request));
    }

    @Test
    @DisplayName("Should throw when status is not terminal")
    void testNonTerminalStatus() {
        ResetRetryRequest request = new ResetRetryRequest();
        request.setStatus(EventConsumeStatus.PENDING_RETRY);

        assertThrows(IllegalArgumentException.class,
                () -> recoveryManager.resetRetry(request));
    }
}