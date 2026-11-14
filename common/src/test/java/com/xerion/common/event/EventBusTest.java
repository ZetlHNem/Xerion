package com.xerion.common.event;

import com.xerion.common.event.events.TickEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {
    @Test
    void dispatchesExactEventType() {
        EventBus eventBus = new EventBus();
        AtomicInteger calls = new AtomicInteger();

        eventBus.subscribe(TickEvent.RuntimeStart.class, ignored -> calls.incrementAndGet());
        eventBus.post(new TickEvent.RuntimeStart());
        eventBus.post(new TickEvent.RuntimeEnd());

        assertEquals(1, calls.get());
    }
}
