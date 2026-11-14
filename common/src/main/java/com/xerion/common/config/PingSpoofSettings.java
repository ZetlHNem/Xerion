package com.xerion.common.config;

public final class PingSpoofSettings {
    private static final float DELAY_MIN = 0.0f;
    private static final float DELAY_MAX = 25_000.0f;

    private static boolean enabled;
    private static float delayMs = 500.0f;

    private PingSpoofSettings() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static float delayMs() {
        return delayMs;
    }

    public static float delaySlider() {
        return inverse(delayMs);
    }

    public static void setDelaySlider(float value) {
        delayMs = range(value);
    }

    private static float range(float value) {
        return Math.max(DELAY_MIN, Math.min(DELAY_MAX, value));
    }

    private static float inverse(float value) {
        return (range(value) - DELAY_MIN) / (DELAY_MAX - DELAY_MIN);
    }
}
