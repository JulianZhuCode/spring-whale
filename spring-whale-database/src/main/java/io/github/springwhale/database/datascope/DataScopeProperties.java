package io.github.springwhale.database.datascope;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.whale.database.datascope")
public class DataScopeProperties {

    private boolean enabled = true;

    private boolean transmitEnabled = true;

    private String scopeTypeHeader = "X-DataScope-Type";

    private String moduleHeader = "X-DataScope-Module";
}