package io.github.springwhale.platform.rbac.dto.request;

import io.github.springwhale.database.datascope.DataScopeType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Role create/update request
 */
@Data
public class RoleRequest {

    private String code;

    @NotBlank(message = "Role name must not be empty")
    private String name;

    private String description;
    private Integer status;
    private Integer sort;
    private Long groupId;
    private DataScopeType dataScope;
}