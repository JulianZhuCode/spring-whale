package io.github.springwhale.framework.event;

import io.github.springwhale.framework.core.utils.SpringContextUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventMessage {

    @NotBlank(message = "data is not null")
    private String data;
    @NotBlank(message = "businessName is not null")
    private String businessName;

    private String getSource() {
        return SpringContextUtils.getApplicationName();
    }
}
