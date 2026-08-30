package io.github.springwhale.database.datascope;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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