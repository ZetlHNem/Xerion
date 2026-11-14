package com.xerion.common;

import com.xerion.common.config.ConfigManager;
import com.xerion.common.event.EventBus;
import com.xerion.common.event.events.RenderEvent;
import com.xerion.common.module.ModuleManager;
import com.xerion.common.platform.PlatformRuntime;
import com.xerion.common.platform.PlatformRuntimeRegistry;
import com.xerion.common.render.HudRenderer;
import com.xerion.common.state.RuntimeState;

public final class XerionCore {
    private final EventBus eventBus;
    private final ModuleManager moduleManager;
    private final RuntimeState runtimeState;
    private final ConfigManager configManager;
    private final HudRenderer hudRenderer;
    private boolean initialized;

    public XerionCore() {
        this.eventBus = new EventBus();
        this.runtimeState = new RuntimeState();
        this.configManager = new ConfigManager();
        this.moduleManager = new ModuleManager(eventBus, runtimeState);
        this.hudRenderer = new HudRenderer(moduleManager);
    }

    public void initialize() {
        if (initialized) {
            return;
        }

        configManager.load();
        moduleManager.enableAllConfigured(configManager.currentConfig());
        eventBus.subscribe(RenderEvent.Hud.class, event -> hudRenderer.render(event.context()));

        initialized = true;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public ModuleManager moduleManager() {
        return moduleManager;
    }

    public RuntimeState runtimeState() {
        return runtimeState;
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public PlatformRuntime platformRuntime() {
        return PlatformRuntimeRegistry.current();
    }
}
