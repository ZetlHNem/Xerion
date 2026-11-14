package com.xerion.common.platform;

public record PlatformRuntimeInfo(
        Loader loader,
        String modId,
        String modName,
        String version
) implements PlatformRuntime {
    public PlatformRuntimeInfo {
        loader = loader == null ? Loader.UNKNOWN : loader;
        modId = normalize(modId, "xerion");
        modName = normalize(modName, "Xerion");
        version = normalize(version, "dev");
    }

    private static String normalize(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}