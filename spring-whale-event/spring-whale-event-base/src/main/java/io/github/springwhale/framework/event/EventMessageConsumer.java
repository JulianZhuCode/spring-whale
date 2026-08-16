package io.github.springwhale.framework.event;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class EventMessageConsumer implements InitializingBean {

    private final Map<String, AbstractEventListener<?>> customRegisterMap = new ConcurrentHashMap<>();
    @Autowired(required = false)
    private Map<String, AbstractEventListener<?>> springListenerBeanMap;
    @Getter
    private volatile Map<String, List<AbstractEventListener<?>>> listenerGroup = Collections.emptyMap();


    public void addListener(String name, AbstractEventListener<?> listener) {
        customRegisterMap.put(name, listener);
        rebuildRouteTable();
    }

    public void removeListener(String name) {
        customRegisterMap.remove(name);
        rebuildRouteTable();
    }

    public void refreshListeners() {
        rebuildRouteTable();
    }

    @Override
    public void afterPropertiesSet() {
        rebuildRouteTable();
    }

    private void rebuildRouteTable() {
        Map<String, AbstractEventListener<?>> allListenersMap = new HashMap<>(
                Optional.ofNullable(springListenerBeanMap).orElse(Collections.emptyMap())
        );
        allListenersMap.putAll(customRegisterMap);

        if (CollectionUtils.isEmpty(allListenersMap)) {
            this.listenerGroup = Collections.emptyMap();
            return;
        }

        Collection<AbstractEventListener<?>> allListeners = allListenersMap.values();

        Map<String, List<AbstractEventListener<?>>> groupMap = allListeners.stream()
                .collect(Collectors.groupingBy(AbstractEventListener::getBusinessName));

        this.listenerGroup = Collections.unmodifiableMap(groupMap);
    }
}
