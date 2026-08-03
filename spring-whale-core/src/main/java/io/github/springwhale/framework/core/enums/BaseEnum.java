package io.github.springwhale.framework.core.enums;

import io.github.springwhale.framework.core.json.BaseEnumJacksonComponent;
import tools.jackson.databind.annotation.JsonDeserialize;


@JsonDeserialize(using = BaseEnumJacksonComponent.BaseEnumDeserializer.class)
public interface BaseEnum {

    String getId();

    String getDesc();
}