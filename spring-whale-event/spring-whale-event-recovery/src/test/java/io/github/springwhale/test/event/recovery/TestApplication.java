package io.github.springwhale.test.event.recovery;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration(excludeName = {
        "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
        "io.github.springwhale.framework.event.autoconfigure.KafkaEventConfiguration",
        "io.github.springwhale.framework.event.autoconfigure.RabbitEventConfiguration",
        "io.github.springwhale.framework.event.recovery.autoconfigure.KafkaEventServerConfiguration",
        "io.github.springwhale.framework.event.recovery.autoconfigure.RabbitEventServerConfiguration"
})
@ComponentScan(basePackages = "io.github.springwhale",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "io\\.github\\.springwhale\\..*\\.autoconfigure\\..*"
        ))
public class TestApplication {
}