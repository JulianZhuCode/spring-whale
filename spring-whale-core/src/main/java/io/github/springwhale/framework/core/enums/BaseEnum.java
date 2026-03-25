package io.github.springwhale.framework.core.enums;

import io.github.springwhale.framework.core.json.SpringWhaleJacksonComponent;
import tools.jackson.databind.annotation.JsonDeserialize;


@JsonDeserialize(using = SpringWhaleJacksonComponent.BaseEnumDeserializer.class)
public interface BaseEnum {

    String getId();

    String getDesc();
}
