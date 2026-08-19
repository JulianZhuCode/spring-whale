package io.github.springwhale.test.event.recovery;

import io.github.springwhale.framework.event.recovery.EventRetryTask;
import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(TestRecoveryConfiguration.class)
class SpringWhaleEventServerConfigurationTest {

    @Autowired
    private EventConsumeFailedRecordDao eventConsumeFailedRecordDao;

    @Autowired
    private EventRetryTask eventRetryTask;

    @Test
    @DisplayName("Should auto-configure EventConsumeFailedRecordDao")
    void testEventConsumeFailedRecordDao() {
        assertNotNull(eventConsumeFailedRecordDao);
    }

    @Test
    @DisplayName("Should auto-configure EventRetryTask")
    void testEventRetryTask() {
        assertNotNull(eventRetryTask);
    }
}