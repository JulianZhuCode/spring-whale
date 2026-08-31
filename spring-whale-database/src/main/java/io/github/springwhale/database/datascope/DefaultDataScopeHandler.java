package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Default no-op implementation of {@link DataScopeHandler}.
 *
 * <p>Returns an empty department ID list and logs a warning.
 * Override this by registering a custom {@link DataScopeHandler} bean.</p>
 */
@Slf4j
public class DefaultDataScopeHandler implements DataScopeHandler {

    @Override
    public List<Object> resolveDeptIds(DataScopeType scopeType, String module) {
        log.warn("DefaultDataScopeHandler.resolveDeptIds() returns empty list. " +
                        "Please implement a custom DataScopeHandler to provide department IDs for data scope type: {}, module: {}",
                scopeType, module);
        return new ArrayList<>();
    }
}