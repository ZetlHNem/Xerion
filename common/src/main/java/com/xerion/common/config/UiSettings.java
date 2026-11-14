package com.xerion.common.config;

public final class UiSettings {
    private static boolean fullscreenBlurEnabled = true;
    private static String theme = "Light";

    private UiSettings() {
    }

    public static boolean fullscreenBlurEnabled() {
        return fullscreenBlurEnabled;
    }

    public static void setFullscreenBlurEnabled(boolean enabled) {
        fullscreenBlurEnabled = enabled;
    }

    public static String theme() {
        return theme;
    }

    public static void setTheme(String value) {
        if ("Light".equals(value) || "Dark".equals(value) || "Auto".equals(value)) {
            theme = value;
        }
    }

    public static boolean darkTheme() {
        return !"Light".equals(theme);
    }
}
