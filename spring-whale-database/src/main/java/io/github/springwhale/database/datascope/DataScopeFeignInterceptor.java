package io.github.springwhale.database.datascope;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DataScopeFeignInterceptor implements RequestInterceptor {

    private final DataScopeProperties properties;

    @Override
    public void apply(RequestTemplate template) {
        if (!properties.isTransmitEnabled()) {
            return;
        }

        DataScopeResult scope = DataScopeContext.getScope();
        if (scope == null || scope.getScopeType() == null) {
            return;
        }

        template.header(properties.getScopeTypeHeader(), scope.getScopeType().name());

        if (scope.getModule() != null && !scope.getModule().isEmpty()) {
            template.header(properties.getModuleHeader(), scope.getModule());
        }

        log.debug("Data scope transmitted via feign: type={}, module={}",
                scope.getScopeType(), scope.getModule());
    }
}