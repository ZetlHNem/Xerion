package com.xerion.common.runtime;

public final class MiscRuntimeRules {
    private MiscRuntimeRules() {
    }

    public static long reconnectDelayMillis(float delaySeconds) {
        return Math.max(1L, Math.round(delaySeconds)) * 1000L;
    }

    public static boolean reconnectDelayElapsed(long nowMillis, long disconnectSeenMillis, long delayMillis) {
        return nowMillis - disconnectSeenMillis >= delayMillis;
    }

    public static int blinkPulseTicks(int configuredTicks) {
        return Math.max(2, configuredTicks);
    }

    public static boolean blinkPulseMode(String mode) {
        return "Pulse".equals(mode);
    }

    public static boolean blinkCancelOnHealthDrop(boolean cancelOnHurt, boolean hasLastHealth,
                                                  float currentHealth, float lastHealth) {
        return cancelOnHurt && hasLastHealth && currentHealth < lastHealth;
    }

    public static boolean blinkPulseReady(int pulseTicks, int configuredTicks) {
        return pulseTicks >= blinkPulseTicks(configuredTicks);
    }

    public static int blinkMaxPackets(int configuredMaxPackets) {
        return Math.max(10, configuredMaxPackets);
    }
}
