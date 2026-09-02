package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Default no-op implementation of {@link DataScopeHandler}.
 *
 * <p>Returns {@code null} (no permission) and logs a warning.
 * Override this by registering a custom {@link DataScopeHandler} bean.</p>
 */
@Slf4j
public class DefaultDataScopeHandler implements DataScopeHandler {

    @Override
    public List<Object> resolveDeptIds(DataScopeType scopeType, String module) {
        log.warn("DefaultDataScopeHandler.resolveDeptIds() returns null (no permission). " +
                        "Please implement a custom DataScopeHandler to provide department IDs for data scope type: {}, module: {}",
                scopeType, module);
        return null;
    }
}