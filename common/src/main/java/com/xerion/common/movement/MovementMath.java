package com.xerion.common.movement;

public final class MovementMath {
    private MovementMath() {
    }

    public static MoveInput normalizedMoveInput(float forward, float strafe) {
        float length = (float) Math.sqrt(forward * forward + strafe * strafe);
        if (length <= 1.0E-5F) {
            return new MoveInput(0.0F, 0.0F);
        }
        return new MoveInput(forward / length, strafe / length);
    }

    public static double[] directionalMotion(float yaw, float forward, float strafe, double speed) {
        double radians = Math.toRadians(yaw + 90.0F);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double motionX = (forward * cos + strafe * sin) * speed;
        double motionZ = (forward * sin - strafe * cos) * speed;
        return new double[]{motionX, motionZ};
    }

    public record MoveInput(float forward, float strafe) {
        public boolean moving() {
            return Math.abs(forward) > 1.0E-5F || Math.abs(strafe) > 1.0E-5F;
        }
    }
}