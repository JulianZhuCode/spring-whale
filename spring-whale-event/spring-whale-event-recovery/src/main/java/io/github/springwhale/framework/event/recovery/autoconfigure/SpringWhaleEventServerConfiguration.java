package io.github.springwhale.framework.event.recovery.autoconfigure;

import io.github.springwhale.framework.event.recovery.dao.EventConsumeFailedRecordDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@AutoConfiguration
@EnableScheduling
@Slf4j
public class SpringWhaleEventServerConfiguration {
 

    @Bean
    @ConditionalOnMissingBean
    public EventConsumeFailedRecordDao eventConsumeFailedRecordDao(DataSource dataSource) {
        return new EventConsumeFailedRecordDao(dataSource);
    }
}