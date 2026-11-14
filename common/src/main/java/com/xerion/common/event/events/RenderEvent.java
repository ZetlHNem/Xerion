package com.xerion.common.event.events;

import com.xerion.common.event.Event;
import com.xerion.common.render.RenderContext;

public sealed interface RenderEvent extends Event permits RenderEvent.Hud, RenderEvent.World {
    RenderContext context();

    record Hud(RenderContext context) implements RenderEvent {
    }

    record World(RenderContext context) implements RenderEvent {
    }
}
