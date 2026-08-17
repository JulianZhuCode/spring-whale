package io.github.springwhale.framework.event;

import lombok.Getter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Abstract MQ event message consumer.
 * <p>Merge listeners from spring container and manual registration, build runtime routing tables.
 * addListener / removeListener supports runtime modification, each call triggers full routing‑table rebuild.</p>
 */
public abstract class EventMessageConsumer implements InitializingBean {

    /**
     * Manually registered listeners, support runtime concurrent put/remove.
     */
    private final Map<String, AbstractEventListener<?>> customRegisterMap = new ConcurrentHashMap<>();
    @Autowired
    protected ObjectMapper jsonMapper;
    @Autowired
    protected EventProperties eventProperties;
    /**
     * Listener instance -> registered name. Unmodifiable view.
     * <p>Key is object reference, do NOT serialize this map.</p>
     */
    @Getter
    private volatile Map<AbstractEventListener<?>, String> listenerInstanceToNameMap = Collections.emptyMap();

    /**
     * Registered name -> listener instance. Unmodifiable view.
     */
    @Getter
    private volatile Map<String, AbstractEventListener<?>> listenerNameToInstanceMap = Collections.emptyMap();

    /**
     * Listeners auto‑injected by spring container, key is spring bean name.
     * <p>Will be null if no matching beans found, do not use directly.</p>
     */
    @Autowired(required = false)
    private Map<String, AbstractEventListener<?>> springListenerBeanMap;

    /**
     * Routing table: key = businessName, value = list of matched listeners.
     * volatile guarantees visibility across MQ consumer threads.
     */
    @Getter
    private volatile Map<String, List<AbstractEventListener<?>>> listenerGroup = Collections.emptyMap();

    /**
     * Check whether no listener registered.
     *
     * @return true if no listener
     */
    public boolean listenerIsEmpty() {
        return CollectionUtils.isEmpty(this.listenerGroup);
    }

    /**
     * Register listener manually, trigger full routing‑table rebuild.
     *
     * @param name     registered name
     * @param listener target listener instance
     */
    public void addListener(String name, AbstractEventListener<?> listener) {
        customRegisterMap.put(name, listener);
        rebuildRouteTable();
    }

    /**
     * Remove manually registered listener, trigger full routing‑table rebuild.
     *
     * @param name registered name
     */
    public void removeListener(String name) {
        customRegisterMap.remove(name);
        rebuildRouteTable();
    }

    /**
     * Force rebuild all listener routing tables. Avoid frequent runtime call.
     */
    public void refreshListeners() {
        rebuildRouteTable();
    }

    @Override
    public void afterPropertiesSet() {
        rebuildRouteTable();
    }

    /**
     * Rebuild all routing tables from combined listener sources.
     * All volatile references will be replaced with new unmodifiable view.
     */
    private void rebuildRouteTable() {
        Map<String, AbstractEventListener<?>> allListenersMap = new HashMap<>(
                Optional.ofNullable(springListenerBeanMap).orElse(Collections.emptyMap())
        );
        allListenersMap.putAll(customRegisterMap);

        if (CollectionUtils.isEmpty(allListenersMap)) {
            this.listenerGroup = Collections.emptyMap();
            this.listenerNameToInstanceMap = Collections.emptyMap();
            this.listenerInstanceToNameMap = Collections.emptyMap();
            return;
        }

        // build businessName group routing
        Collection<AbstractEventListener<?>> allListeners = allListenersMap.values();
        Map<String, List<AbstractEventListener<?>>> groupMap = allListeners.stream()
                .collect(Collectors.groupingBy(AbstractEventListener::getBusinessName));

        // build bidirectional mapping: name <-> listener instance
        Map<String, AbstractEventListener<?>> tempNameToInstance = new HashMap<>(allListenersMap.size());
        Map<AbstractEventListener<?>, String> tempInstanceToName = new HashMap<>(allListenersMap.size());

        for (Map.Entry<String, AbstractEventListener<?>> entry : allListenersMap.entrySet()) {
            String name = entry.getKey();
            AbstractEventListener<?> listener = entry.getValue();

            // detect conflict: one instance bind multiple name
            if (tempInstanceToName.containsKey(listener)) {
                throw new IllegalStateException(
                        "Listener instance already bound to name[" + tempInstanceToName.get(listener)
                                + "], cannot rebind to name[" + name + "]"
                );
            }
            tempNameToInstance.put(name, listener);
            tempInstanceToName.put(listener, name);
        }

        // assign unmodifiable view, volatile reference replace
        this.listenerGroup = Collections.unmodifiableMap(groupMap);
        this.listenerNameToInstanceMap = Collections.unmodifiableMap(tempNameToInstance);
        this.listenerInstanceToNameMap = Collections.unmodifiableMap(tempInstanceToName);
    }
}