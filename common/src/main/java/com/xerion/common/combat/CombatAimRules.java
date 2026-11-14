package com.xerion.common.combat;

public final class CombatAimRules {
    private static final float RAD_TO_DEG = 180.0F / (float) Math.PI;

    private CombatAimRules() {
    }

    public static Rotation rotationBetween(double fromX, double fromY, double fromZ,
                                           double toX, double toY, double toZ) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dz = toZ - fromZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * RAD_TO_DEG) - 90.0F;
        float pitch = (float) -(Math.atan2(dy, horizontal) * RAD_TO_DEG);
        return new Rotation(yaw, pitch);
    }

    public static Rotation ballisticRotation(double fromX, double fromY, double fromZ,
                                             double toX, double toY, double toZ,
                                             float speed, float gravity) {
        return ballisticRotation(fromX, fromY, fromZ, toX, toY, toZ, speed, gravity, -55.0F, 55.0F);
    }

    public static Rotation ballisticRotation(double fromX, double fromY, double fromZ,
                                             double toX, double toY, double toZ,
                                             float speed, float gravity,
                                             float minPitch, float maxPitch) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;
        double dy = toY - fromY;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 0.001D) {
            return null;
        }

        double speedSq = speed * speed;
        double discriminant = speedSq * speedSq - gravity * (gravity * horizontal * horizontal + 2.0D * dy * speedSq);
        if (discriminant < 0.0D) {
            return rotationBetween(fromX, fromY, fromZ, toX, toY, toZ);
        }

        double root = Math.sqrt(discriminant);
        double tan = (speedSq - root) / (gravity * horizontal);
        float yaw = (float) (Math.atan2(dz, dx) * RAD_TO_DEG) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan(tan));
        if (pitch < minPitch || pitch > maxPitch) {
            return rotationBetween(fromX, fromY, fromZ, toX, toY, toZ);
        }
        return new Rotation(yaw, pitch);
    }


    public static double projectilePredictionTicks(double horizontalDistance, float speed,
                                                   boolean hasPitch, float pitch) {
        double predictedTicks = horizontalDistance / Math.max(0.1D, speed);
        if (hasPitch) {
            double pitchRadians = Math.toRadians(pitch);
            double horizontalSpeed = Math.max(0.1D, speed * Math.cos(pitchRadians));
            predictedTicks = horizontalDistance / horizontalSpeed;
        }
        return predictedTicks;
    }

    public static double predictionScale(double predictionPercent) {
        return predictionPercent / 100.0D;
    }
    public static double horizontalDistance(double fromX, double fromZ, double toX, double toZ) {
        double dx = toX - fromX;
        double dz = toZ - fromZ;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static float aimCircleDistance(float currentYaw, float currentPitch, float targetYaw, float targetPitch) {
        float yaw = wrapDegrees(targetYaw - currentYaw);
        float pitch = targetPitch - currentPitch;
        return (float) Math.sqrt(yaw * yaw + pitch * pitch);
    }

    public static float yawDelta(float currentYaw, float targetYaw) {
        return Math.abs(wrapDegrees(targetYaw - currentYaw));
    }

    public static boolean insideFov(float yawDelta, float fov) {
        if (fov >= 359.0F) {
            return true;
        }
        return yawDelta <= fov * 0.5F;
    }

    public static float rotationSpeed(float smoothness) {
        float clampedSmoothness = clamp(smoothness, 0.0F, 100.0F);
        return Math.max(5.0F, 15.5F - clampedSmoothness * 0.085F);
    }

    public static float rotateYaw(float current, float target, float step) {
        return current + clamp(wrapDegrees(target - current), -step, step);
    }

    public static float approach(float current, float target, float step) {
        return current + clamp(target - current, -step, step);
    }

    public static float smoothYawStep(float current, float target, float alpha, float maxStep) {
        float delta = wrapDegrees(target - current) * clamp(alpha, 0.0F, 1.0F);
        return current + clamp(delta, -maxStep, maxStep);
    }

    public static float smoothStep(float current, float target, float alpha, float maxStep) {
        float delta = (target - current) * clamp(alpha, 0.0F, 1.0F);
        return current + clamp(delta, -maxStep, maxStep);
    }

    public static float strengthTargetYaw(float current, float target, float strengthSetting) {
        float strength = clamp(strengthSetting, 1.0F, 100.0F) / 100.0F;
        return current + wrapDegrees(target - current) * strength;
    }

    public static float strengthTargetPitch(float current, float target, float strengthSetting) {
        float strength = clamp(strengthSetting, 1.0F, 100.0F) / 100.0F;
        return current + (target - current) * strength;
    }

    public static float renderLerpAlpha(float smoothness, float dt) {
        if (dt <= 0.0F) {
            return 0.0F;
        }
        float clampedSmoothness = clamp(smoothness, 0.0F, 100.0F);
        float response = 28.0F - clampedSmoothness * 0.16F;
        return clamp(1.0F - (float) Math.exp(-response * dt), 0.0F, 1.0F);
    }

    public static float aimAssistLerpAlpha(float smooth, float dt) {
        if (dt <= 0.0F) {
            return 0.0F;
        }
        float clampedSmooth = clamp(smooth, 0.0F, 100.0F);
        if (clampedSmooth <= 0.5F) {
            return 1.0F;
        }
        float smoothFactor = clampedSmooth / 100.0F;
        float response = lerp(smoothFactor, 64.0F, 3.4F);
        return clamp(1.0F - (float) Math.exp(-response * dt), 0.0F, 1.0F);
    }

    public static float aimAssistMaxStep(float smooth, float dt) {
        float clampedSmooth = clamp(smooth, 0.0F, 100.0F);
        if (clampedSmooth <= 0.5F) {
            return Float.MAX_VALUE;
        }
        float smoothFactor = clampedSmooth / 100.0F;
        float degreesPerSecond = lerp(smoothFactor, 2400.0F, 90.0F);
        return Math.max(0.15F, degreesPerSecond * dt);
    }

    public static float projectileAimLerpAlpha(float smooth, float dt) {
        if (dt <= 0.0F) {
            return 0.0F;
        }
        float clampedSmooth = clamp(smooth, 0.0F, 100.0F);
        if (clampedSmooth <= 0.5F) {
            return 1.0F;
        }
        float smoothFactor = clampedSmooth / 100.0F;
        float response = lerp(smoothFactor, 72.0F, 4.2F);
        return clamp(1.0F - (float) Math.exp(-response * dt), 0.0F, 1.0F);
    }

    public static float projectileAimMaxStep(float smooth, float dt) {
        float clampedSmooth = clamp(smooth, 0.0F, 100.0F);
        if (clampedSmooth <= 0.5F) {
            return Float.MAX_VALUE;
        }
        float smoothFactor = clampedSmooth / 100.0F;
        float degreesPerSecond = lerp(smoothFactor, 2800.0F, 120.0F);
        return Math.max(0.15F, degreesPerSecond * dt);
    }
    public static float pearlAimLerpAlpha(float smooth, float dt) {
        return timedLerpAlpha(smooth, dt, 70.0F, 4.0F);
    }

    public static float pearlAimMaxStep(float smooth, float dt) {
        return timedMaxStep(smooth, dt, 2600.0F, 110.0F);
    }

    public static float timedLerpAlpha(float smooth, float dt, float fastResponse, float slowResponse) {
        if (dt <= 0.0F) {
            return 0.0F;
        }
        float clampedSmooth = clamp(smooth, 0.0F, 100.0F);
        if (clampedSmooth <= 0.5F) {
            return 1.0F;
        }
        float smoothFactor = clampedSmooth / 100.0F;
        float response = lerp(smoothFactor, fastResponse, slowResponse);
        return clamp(1.0F - (float) Math.exp(-response * dt), 0.0F, 1.0F);
    }

    public static float timedMaxStep(float smooth, float dt, float fastDegreesPerSecond, float slowDegreesPerSecond) {
        float clampedSmooth = clamp(smooth, 0.0F, 100.0F);
        if (clampedSmooth <= 0.5F) {
            return Float.MAX_VALUE;
        }
        float smoothFactor = clampedSmooth / 100.0F;
        float degreesPerSecond = lerp(smoothFactor, fastDegreesPerSecond, slowDegreesPerSecond);
        return Math.max(0.15F, degreesPerSecond * dt);
    }

    public static float lerpYaw(float current, float target, float alpha) {
        return current + wrapDegrees(target - current) * clamp(alpha, 0.0F, 1.0F);
    }

    public static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public record Rotation(float yaw, float pitch) {
    }
}