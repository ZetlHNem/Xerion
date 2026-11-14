package com.xerion.common.event.events;

import com.xerion.common.event.Event;

public sealed interface TickEvent extends Event permits TickEvent.RuntimeStart, TickEvent.RuntimeEnd {
    record RuntimeStart() implements TickEvent {
    }

    record RuntimeEnd() implements TickEvent {
    }
}
