package com.xerion.common.combat;

public final class TargetStrafeRules {
    public static final double BASE_BPS = 3.06D;
    public static final double BPS_TO_DELTA = 1.0D / 20.0D;
    public static final double MIN_INPUT = 1.0E-5D;

    private TargetStrafeRules() {
    }

    public static boolean moving(float forward, float strafe) {
        return Math.abs(forward) > MIN_INPUT || Math.abs(strafe) > MIN_INPUT;
    }

    public static int manualDirection(String setting, String leftSetting, String rightSetting, int fallbackDirection) {
        if (leftSetting.equals(setting)) {
            return 1;
        }
        if (rightSetting.equals(setting)) {
            return -1;
        }
        return fallbackDirection;
    }

    public static int autoDirection(float strafeInput, int currentDirection) {
        if (strafeInput > MIN_INPUT) {
            return 1;
        }
        if (strafeInput < -MIN_INPUT) {
            return -1;
        }
        return currentDirection;
    }

    public static Motion motion(double playerX, double playerZ, double targetX, double targetZ,
                                int direction, double radius, double speedMultiplier) {
        double radialX = playerX - targetX;
        double radialZ = playerZ - targetZ;
        double distance = Math.sqrt(radialX * radialX + radialZ * radialZ);
        if (distance < 0.001D) {
            return Motion.NONE;
        }

        radialX /= distance;
        radialZ /= distance;

        double tangentX = -radialZ * direction;
        double tangentZ = radialX * direction;
        double correction = clamp((Math.max(0.5D, radius) - distance) * 0.85D, -0.95D, 0.95D);
        double moveX = tangentX + radialX * correction;
        double moveZ = tangentZ + radialZ * correction;
        double moveLen = Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (moveLen < 0.001D) {
            return Motion.NONE;
        }

        double speed = BASE_BPS * clamp(speedMultiplier, 0.0D, 2.0D) * BPS_TO_DELTA;
        if (speed <= MIN_INPUT) {
            return Motion.NONE;
        }

        return new Motion(moveX / moveLen * speed, moveZ / moveLen * speed, true);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Motion(double x, double z, boolean active) {
        public static final Motion NONE = new Motion(0.0D, 0.0D, false);
    }
}