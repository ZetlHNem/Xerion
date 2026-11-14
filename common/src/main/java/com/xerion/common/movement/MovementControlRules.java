package com.xerion.common.movement;

public final class MovementControlRules {
    public static final float MOVE_INPUT_EPSILON = 1.0E-5F;
    public static final double MIN_HORIZONTAL_SPEED = 1.0E-4D;
    public static final double GROUND_SPEED_TOLERANCE = 0.01D;

    private MovementControlRules() {
    }

    public static boolean hasMoveInput(float forward, float strafe) {
        return Math.abs(forward) > MOVE_INPUT_EPSILON || Math.abs(strafe) > MOVE_INPUT_EPSILON;
    }

    public static double horizontalSpeed(double motionX, double motionZ) {
        return Math.sqrt(motionX * motionX + motionZ * motionZ);
    }

    public static HorizontalMotion bhopTurnMotion(float yaw, float forward, float strafe,
                                                  double velocityX, double velocityZ,
                                                  boolean keepSprintEnabled,
                                                  double keepSprintAirTargetSpeed) {
        MovementMath.MoveInput input = MovementMath.normalizedMoveInput(forward, strafe);
        if (!input.moving()) {
            return null;
        }

        double speed = horizontalSpeed(velocityX, velocityZ);
        if (speed < MIN_HORIZONTAL_SPEED) {
            return null;
        }
        if (keepSprintEnabled) {
            speed = Math.max(speed, keepSprintAirTargetSpeed);
        }
        return directionalHorizontalMotion(yaw, input.forward(), input.strafe(), speed);
    }

    public static AirState nextAirState(boolean wasOnGround, int airTicks) {
        return new AirState(false, wasOnGround ? 1 : airTicks + 1);
    }

    public static HorizontalMotion directionalHorizontalMotion(float yaw, float forward, float strafe, double speed) {
        MovementMath.MoveInput input = MovementMath.normalizedMoveInput(forward, strafe);
        if (!input.moving()) {
            return null;
        }
        double[] motion = MovementMath.directionalMotion(yaw, input.forward(), input.strafe(), speed);
        return new HorizontalMotion(motion[0], motion[1]);
    }

    public static boolean shouldAdjustGroundSpeed(double currentSpeed, double targetSpeed) {
        return Math.abs(currentSpeed - targetSpeed) > GROUND_SPEED_TOLERANCE;
    }

    public static HorizontalMotion scaleHorizontalMotion(double velocityX, double velocityZ,
                                                         double currentSpeed, double targetSpeed) {
        if (currentSpeed < MIN_HORIZONTAL_SPEED) {
            return null;
        }
        double scale = targetSpeed / currentSpeed;
        return new HorizontalMotion(velocityX * scale, velocityZ * scale);
    }

    public record HorizontalMotion(double x, double z) {
    }

    public record AirState(boolean wasOnGround, int airTicks) {
    }
}
