package com.xerion.common.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    public <T extends Event> Subscription subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> unsubscribe(eventType, listener);
    }

    public <T extends Event> void post(T event) {
        List<Consumer<?>> exactListeners = listeners.get(event.getClass());
        if (exactListeners == null) {
            return;
        }

        for (Consumer<?> listener : exactListeners) {
            @SuppressWarnings("unchecked")
            Consumer<T> typedListener = (Consumer<T>) listener;
            typedListener.accept(event);
        }
    }

    private <T extends Event> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer<?>> typedListeners = listeners.get(eventType);
        if (typedListeners != null) {
            typedListeners.remove(listener);
        }
    }
}
