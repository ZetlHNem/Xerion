package com.xerion.common.config;

public final class ConfigManager {
    private RuntimeConfig currentConfig = RuntimeConfig.defaults();

    public void load() {
        // Replace with JSON/TOML persistence at the loader layer or via a platform-neutral file service.
        currentConfig = RuntimeConfig.defaults();
    }

    public void save() {
        // Intentionally empty in the skeleton.
    }

    public RuntimeConfig currentConfig() {
        return currentConfig;
    }
}
