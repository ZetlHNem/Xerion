package com.xerion.common.movement;

public final class MovementAbilityRules {
    private MovementAbilityRules() {
    }

    public static double spiderSpeed(double configuredSpeed, String mode) {
        double speed = clamp(configuredSpeed, 0.05D, 0.6D);
        return "Legit".equals(mode) ? speed * 0.55D : speed;
    }

    public static double highJumpMotion(double currentMotionY, double power) {
        return Math.max(currentMotionY, 0.42D * clamp(power, 1.0D, 4.0D));
    }

    public static double longJumpPower(double power) {
        return clamp(power, 0.5D, 4.0D);
    }

    public static boolean shouldSpoofNoFall(float fallDistance, double motionY) {
        return fallDistance >= 2.5F && motionY < -0.05D;
    }

    public static boolean shouldTriggerAntiVoid(boolean voidBelow, float fallDistance,
                                                float configuredFallDistance, double motionY,
                                                int cooldownTicks) {
        return voidBelow
                && fallDistance >= configuredFallDistance
                && motionY < -0.02D
                && cooldownTicks <= 0;
    }

    public static boolean antiVoidSetback(String mode, boolean hasSafePosition) {
        return "Setback".equals(mode) && hasSafePosition;
    }

    public static float stepHeight(float configuredHeight) {
        return (float) clamp(configuredHeight, 0.6D, 2.5D);
    }

    public static boolean stepHeightChanged(float currentHeight, float targetHeight) {
        return Math.abs(currentHeight - targetHeight) > 1.0E-4F;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
