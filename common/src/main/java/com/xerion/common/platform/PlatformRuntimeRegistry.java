package com.xerion.common.platform;

public final class PlatformRuntimeRegistry {
    private static PlatformRuntime current = new PlatformRuntimeInfo(
            PlatformRuntime.Loader.UNKNOWN,
            "xerion",
            "Xerion",
            "dev"
    );

    private PlatformRuntimeRegistry() {
    }

    public static synchronized void register(PlatformRuntime runtime) {
        current = runtime == null ? current : runtime;
    }

    public static synchronized PlatformRuntime current() {
        return current;
    }
}