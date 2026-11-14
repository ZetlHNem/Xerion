package com.xerion.common.config;

public final class NoRotateSettings {
    private static boolean enabled;
    private static boolean ignoreTeleports = true;

    private NoRotateSettings() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean ignoreTeleports() {
        return ignoreTeleports;
    }

    public static void setIgnoreTeleports(boolean value) {
        ignoreTeleports = value;
    }
}
