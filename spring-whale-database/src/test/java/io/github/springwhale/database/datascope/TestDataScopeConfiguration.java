package io.github.springwhale.database.datascope;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@TestConfiguration
@EnableAspectJAutoProxy
public class TestDataScopeConfiguration {

    @Bean
    public DataScopeHandler dataScopeHandler() {
        return new TestDataScopeHandler();
    }
}