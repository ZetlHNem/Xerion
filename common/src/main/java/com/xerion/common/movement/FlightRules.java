package com.xerion.common.movement;

public final class FlightRules {
    public static final double BPS_TO_DELTA = 1.0D / 20.0D;

    private FlightRules() {
    }

    public static double horizontalSpeed(double configuredSpeed) {
        return Math.max(0.0D, configuredSpeed) * BPS_TO_DELTA;
    }

    public static double verticalSpeed(double configuredSpeed) {
        return Math.max(0.0D, configuredSpeed) * BPS_TO_DELTA;
    }

    public static double boostedHorizontalSpeed(double configuredSpeed, boolean boostMode) {
        double speed = horizontalSpeed(configuredSpeed);
        return boostMode ? speed * 1.25D : speed;
    }

    public static double boostedVerticalSpeed(double configuredSpeed, boolean boostMode) {
        double speed = verticalSpeed(configuredSpeed);
        return boostMode ? speed * 1.15D : speed;
    }

    public static double glideMotion(String mode, double glide, double factor, double fallback) {
        return "Glide".equals(mode) ? -factor * clamp(glide, 0.0D, 1.0D) : fallback;
    }

    public static double elytraGlideMotion(double glide) {
        return -0.04D * clamp(glide, 0.0D, 1.0D);
    }

    public static double entitySpeed(double configuredSpeed) {
        return Math.max(0.0D, configuredSpeed) * BPS_TO_DELTA;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}