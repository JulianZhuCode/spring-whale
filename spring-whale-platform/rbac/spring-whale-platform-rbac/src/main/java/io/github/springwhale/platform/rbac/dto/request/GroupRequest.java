package io.github.springwhale.platform.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Department create/update request
 */
@Data
public class GroupRequest {

    private Long parentId;

    private String code;

    @NotBlank(message = "Department name must not be empty")
    private String name;

    private String description;
    private String leader;
    private String phone;
    private String email;
    private Integer sort;
    private Integer status;
}