package io.github.springwhale.database.datascope;

import java.util.List;

/**
 * DTO for the data scope department IDs resolution response.
 */
public record DataScopeResolveResponse(List<Object> deptIds) {
}