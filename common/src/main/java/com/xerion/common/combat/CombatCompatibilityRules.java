package com.xerion.common.combat;

import com.xerion.common.config.BypassSettings;

public final class CombatCompatibilityRules {
    private CombatCompatibilityRules() {
    }

    public static Rotation filterRotation(float currentYaw, float currentPitch,
                                          float targetYaw, float targetPitch,
                                          double sensitivity, float randomization,
                                          double yawSample, double pitchSample,
                                          boolean quantize) {
        float amount = Math.max(0.0f, randomization);
        float randomizedYaw = targetYaw + sampleOffset(yawSample, amount);
        float randomizedPitch = clamp(targetPitch + sampleOffset(pitchSample, amount), -90.0f, 90.0f);
        if (!quantize) {
            return new Rotation(randomizedYaw, randomizedPitch);
        }

        double step = rotationStep(sensitivity);
        float yawDelta = CombatAimRules.wrapDegrees(randomizedYaw - currentYaw);
        float pitchDelta = randomizedPitch - currentPitch;
        float yaw = currentYaw + quantize(yawDelta, step);
        float pitch = clamp(currentPitch + quantize(pitchDelta, step), -90.0f, 90.0f);
        return new Rotation(yaw, pitch);
    }

    public static double rotationStep(double sensitivity) {
        double normalized = clamp(sensitivity, 0.0, 1.0);
        double base = normalized * 0.6D + 0.2D;
        return base * base * base * 8.0D * 0.15D;
    }

    public static int attackDelayTicks(float cps, String profile, int jitterTicks, double sample) {
        int base = CombatActionRules.attackDelayTicks(cps);
        int jitter = Math.max(0, jitterTicks);
        if (jitter == 0) {
            return base;
        }
        int randomized = (int) Math.floor(clamp(sample, 0.0D, 0.999999D) * (jitter + 1));
        if (BypassSettings.PROFILE_STRICT.equals(profile)) {
            randomized = Math.max(1, randomized);
        }
        return base + randomized;
    }

    public static boolean duplicateAction(long nowNanos, long previousNanos, int windowMs) {
        if (previousNanos <= 0L || windowMs <= 0) {
            return false;
        }
        long elapsed = nowNanos - previousNanos;
        return elapsed >= 0L && elapsed < windowMs * 1_000_000L;
    }

    private static float sampleOffset(double sample, float amount) {
        return (float) ((clamp(sample, 0.0D, 1.0D) * 2.0D - 1.0D) * amount);
    }

    private static float quantize(float value, double step) {
        if (step <= 1.0E-8D) {
            return value;
        }
        return (float) (Math.round(value / step) * step);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Rotation(float yaw, float pitch) {
    }
}
