package com.xerion.common.config;

import java.util.Set;

public record RuntimeConfig(Set<String> enabledModules) {
    public static RuntimeConfig defaults() {
        return new RuntimeConfig(Set.of());
    }
}
