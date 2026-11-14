package com.xerion.common.module;

import com.xerion.common.event.EventBus;
import com.xerion.common.state.RuntimeState;

public abstract class Module {
    private final String id;
    private final String displayName;
    private final ModuleCategory category;
    private boolean enabled;

    protected Module(String id, String displayName, ModuleCategory category) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
    }

    public final void enable(EventBus eventBus, RuntimeState runtimeState) {
        if (enabled) {
            return;
        }
        enabled = true;
        onEnable(eventBus, runtimeState);
    }

    public final void disable(EventBus eventBus, RuntimeState runtimeState) {
        if (!enabled) {
            return;
        }
        onDisable(eventBus, runtimeState);
        enabled = false;
    }

    protected void onEnable(EventBus eventBus, RuntimeState runtimeState) {
    }

    protected void onDisable(EventBus eventBus, RuntimeState runtimeState) {
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public ModuleCategory category() {
        return category;
    }

    public boolean enabled() {
        return enabled;
    }
}
