package com.xerion.common.module;

import com.xerion.common.config.RuntimeConfig;
import com.xerion.common.event.EventBus;
import com.xerion.common.state.RuntimeState;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private final Map<String, Module> modules = new LinkedHashMap<>();
    private final EventBus eventBus;
    private final RuntimeState runtimeState;

    public ModuleManager(EventBus eventBus, RuntimeState runtimeState) {
        this.eventBus = eventBus;
        this.runtimeState = runtimeState;
    }

    public void register(Module module) {
        if (modules.putIfAbsent(module.id(), module) != null) {
            throw new IllegalArgumentException("Duplicate module id: " + module.id());
        }
    }

    public void enable(String moduleId) {
        module(moduleId).ifPresent(module -> module.enable(eventBus, runtimeState));
    }

    public void disable(String moduleId) {
        module(moduleId).ifPresent(module -> module.disable(eventBus, runtimeState));
    }

    public void toggle(String moduleId) {
        module(moduleId).ifPresent(module -> {
            if (module.enabled()) {
                module.disable(eventBus, runtimeState);
            } else {
                module.enable(eventBus, runtimeState);
            }
        });
    }

    public void enableAllConfigured(RuntimeConfig config) {
        for (String enabledModuleId : config.enabledModules()) {
            enable(enabledModuleId);
        }
    }

    public Optional<Module> module(String moduleId) {
        return Optional.ofNullable(modules.get(moduleId));
    }

    public Collection<Module> modules() {
        return modules.values();
    }
}
