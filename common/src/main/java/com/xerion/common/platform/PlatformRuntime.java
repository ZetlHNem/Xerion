package com.xerion.common.platform;

public interface PlatformRuntime {
    Loader loader();

    String modId();

    String modName();

    String version();

    enum Loader {
        FORGE,
        UNKNOWN
    }
}
