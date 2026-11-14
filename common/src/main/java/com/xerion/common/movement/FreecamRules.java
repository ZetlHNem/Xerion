package com.xerion.common.movement;

public final class FreecamRules {
    private FreecamRules() {
    }

    public static double frameSeconds(long nowNanos, long lastFrameNanos) {
        if (lastFrameNanos == 0L) {
            return 0.0D;
        }
        double frameSeconds = (nowNanos - lastFrameNanos) / 1_000_000_000.0D;
        return Math.max(0.0D, Math.min(0.1D, frameSeconds));
    }

    public static double movementSpeed(double configuredSpeed, double frameSeconds, boolean sprinting) {
        double speed = configuredSpeed * 5.0D * frameSeconds;
        return sprinting ? speed * 2.0D : speed;
    }

    public static float nextYaw(float currentYaw, double yawDelta) {
        return currentYaw + (float) yawDelta * 0.15F;
    }

    public static float nextPitch(float currentPitch, double pitchDelta) {
        return clamp(currentPitch + (float) pitchDelta * 0.15F, -90.0F, 90.0F);
    }

    public static Motion movement(float yaw, double forward, double strafe, double vertical, double speed) {
        if (forward == 0.0D && strafe == 0.0D && vertical == 0.0D) {
            return new Motion(0.0D, 0.0D, 0.0D);
        }
        double radians = Math.toRadians(yaw);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double dx = (strafe * cos - forward * sin) * speed;
        double dz = (forward * cos + strafe * sin) * speed;
        double dy = vertical * speed;
        double length = Math.sqrt(forward * forward + strafe * strafe + vertical * vertical);
        if (length > 1.0D) {
            dx /= length;
            dy /= length;
            dz /= length;
        }
        return new Motion(dx, dy, dz);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Motion(double x, double y, double z) {
        public boolean moving() {
            return x != 0.0D || y != 0.0D || z != 0.0D;
        }
    }
}
