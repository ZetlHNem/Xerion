package com.xerion.common.movement;

public final class MovementSpeedRules {
    public static final double BPS_TO_DELTA = 1.0D / 20.0D;
    public static final double KEEP_SPRINT_GROUND_BASE_BPS = 3.06D;
    public static final double KEEP_SPRINT_AIR_START_BPS = 6.68D;
    public static final double KEEP_SPRINT_AIR_STEP_BPS = 0.0D;
    public static final int KEEP_SPRINT_AIR_STEP_TICKS = 8;

    private MovementSpeedRules() {
    }

    public static double keepSprintGroundTargetSpeed(double scale) {
        return KEEP_SPRINT_GROUND_BASE_BPS * scale * BPS_TO_DELTA;
    }

    public static double keepSprintAirTargetSpeed(int keepSprintAirTicks, double scale) {
        int steppedTicks = Math.min(keepSprintAirTicks, KEEP_SPRINT_AIR_STEP_TICKS);
        double baseBps = KEEP_SPRINT_AIR_START_BPS + Math.max(0, steppedTicks - 1) * KEEP_SPRINT_AIR_STEP_BPS;
        return baseBps * scale * BPS_TO_DELTA;
    }

    public static double longJumpHorizontalSpeed(double power) {
        return KEEP_SPRINT_GROUND_BASE_BPS * power * BPS_TO_DELTA;
    }

    public static double speedTarget(boolean onGround, String mode, double currentSpeed,
                                     double groundMultiplier, double airMultiplier) {
        double groundTarget = KEEP_SPRINT_GROUND_BASE_BPS
                * Math.max(0.0D, Math.min(2.0D, groundMultiplier))
                * BPS_TO_DELTA;
        double airTarget = KEEP_SPRINT_GROUND_BASE_BPS
                * Math.max(0.0D, Math.min(2.0D, airMultiplier))
                * BPS_TO_DELTA;
        if ("Vanilla".equals(mode)) {
            return onGround ? groundTarget : airTarget;
        }
        if ("BHop".equals(mode)) {
            return onGround ? groundTarget : Math.max(currentSpeed, airTarget);
        }
        return onGround ? Math.max(currentSpeed, groundTarget) : Math.max(currentSpeed, airTarget);
    }

    public static double noSlowTargetSpeed(boolean sprintingOrKeepSprint) {
        return (sprintingOrKeepSprint ? KEEP_SPRINT_GROUND_BASE_BPS : 2.15D) * BPS_TO_DELTA;
    }
}