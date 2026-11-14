package com.xerion.common.config;

public final class BypassSettings {
    public static final String PROFILE_ADAPTIVE = "Adaptive";
    public static final String PROFILE_STRICT = "Strict";

    private static final float ROTATION_RANDOMIZATION_MIN = 0.0f;
    private static final float ROTATION_RANDOMIZATION_MAX = 1.0f;
    private static final float ATTACK_JITTER_MIN = 0.0f;
    private static final float ATTACK_JITTER_MAX = 3.0f;
    private static final float COMMAND_WINDOW_MIN = 0.0f;
    private static final float COMMAND_WINDOW_MAX = 250.0f;
    private static final float RECOVERY_WINDOW_MIN = 150.0f;
    private static final float RECOVERY_WINDOW_MAX = 1000.0f;

    private static boolean enabled;
    private static String profile = PROFILE_ADAPTIVE;
    private static boolean rotationQuantization = true;
    private static boolean rotationRandomization = true;
    private static float rotationRandomizationAmount = 0.25f;
    private static boolean attackTiming = true;
    private static float attackJitterTicks = 1.0f;
    private static boolean actionDeduplication = true;
    private static float actionDeduplicationWindow = 75.0f;
    private static boolean packetDeduplication = true;
    private static boolean correctionRecovery = true;
    private static float correctionRecoveryWindow = 500.0f;

    private BypassSettings() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static String profile() {
        return profile;
    }

    public static void setProfile(String value) {
        profile = PROFILE_STRICT.equals(value) ? PROFILE_STRICT : PROFILE_ADAPTIVE;
    }

    public static boolean rotationQuantization() {
        return rotationQuantization;
    }

    public static void setRotationQuantization(boolean value) {
        rotationQuantization = value;
    }

    public static boolean rotationRandomization() {
        return rotationRandomization;
    }

    public static void setRotationRandomization(boolean value) {
        rotationRandomization = value;
    }

    public static float rotationRandomizationAmount() {
        return rotationRandomizationAmount;
    }

    public static float rotationRandomizationAmountSlider() {
        return inverse(rotationRandomizationAmount, ROTATION_RANDOMIZATION_MIN, ROTATION_RANDOMIZATION_MAX);
    }

    public static void setRotationRandomizationAmountSlider(float value) {
        rotationRandomizationAmount = range(value, ROTATION_RANDOMIZATION_MIN, ROTATION_RANDOMIZATION_MAX);
    }

    public static boolean attackTiming() {
        return attackTiming;
    }

    public static void setAttackTiming(boolean value) {
        attackTiming = value;
    }

    public static int attackJitterTicks() {
        return Math.round(attackJitterTicks);
    }

    public static float attackJitterTicksSlider() {
        return inverse(attackJitterTicks, ATTACK_JITTER_MIN, ATTACK_JITTER_MAX);
    }

    public static void setAttackJitterTicksSlider(float value) {
        attackJitterTicks = range(value, ATTACK_JITTER_MIN, ATTACK_JITTER_MAX);
    }

    public static boolean actionDeduplication() {
        return actionDeduplication;
    }

    public static void setActionDeduplication(boolean value) {
        actionDeduplication = value;
    }

    public static int actionDeduplicationWindowMs() {
        return Math.round(actionDeduplicationWindow);
    }

    public static float actionDeduplicationWindowSlider() {
        return inverse(actionDeduplicationWindow, COMMAND_WINDOW_MIN, COMMAND_WINDOW_MAX);
    }

    public static void setActionDeduplicationWindowSlider(float value) {
        actionDeduplicationWindow = range(value, COMMAND_WINDOW_MIN, COMMAND_WINDOW_MAX);
    }

    public static boolean packetDeduplication() {
        return packetDeduplication;
    }

    public static void setPacketDeduplication(boolean value) {
        packetDeduplication = value;
    }

    public static boolean correctionRecovery() {
        return correctionRecovery;
    }

    public static void setCorrectionRecovery(boolean value) {
        correctionRecovery = value;
    }

    public static int correctionRecoveryWindowMs() {
        return Math.round(correctionRecoveryWindow);
    }

    public static float correctionRecoveryWindowSlider() {
        return inverse(correctionRecoveryWindow, RECOVERY_WINDOW_MIN, RECOVERY_WINDOW_MAX);
    }

    public static void setCorrectionRecoveryWindowSlider(float value) {
        correctionRecoveryWindow = range(value, RECOVERY_WINDOW_MIN, RECOVERY_WINDOW_MAX);
    }

    private static float range(float slider, float min, float max) {
        return min + (max - min) * clamp(slider);
    }

    private static float inverse(float value, float min, float max) {
        return clamp((value - min) / (max - min));
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
