package com.xerion.common.movement;

public final class MovementSurfaceRules {
    private MovementSurfaceRules() {
    }

    public static boolean shouldQuickStop(double velocityX, double velocityZ) {
        return velocityX * velocityX + velocityZ * velocityZ >= 1.0E-5D;
    }

    public static double edgeJumpLookahead(double horizontalSpeed, double minimumLookahead, double extraLookahead) {
        return Math.max(minimumLookahead, horizontalSpeed + extraLookahead);
    }

    public static double safeWalkInputProbe(double horizontalSpeed, double minimumProbe, double extraProbe) {
        return Math.max(minimumProbe, horizontalSpeed + extraProbe);
    }

    public static boolean supportAxisAllowed(double current, double target,
                                             double supportMin, double supportMax,
                                             double centerOverhang) {
        double min = supportMin - centerOverhang;
        double max = supportMax + centerOverhang;
        if (target < min && target < current) {
            return false;
        }
        return target <= max || target <= current;
    }

    public static double scaffoldRotationFactor(double speed) {
        return Math.max(0.08D, Math.min(1.0D, 1.0D - Math.exp(-speed * 0.28D)));
    }

    public static double scaffoldRandomOffset(double randomUnit) {
        return randomUnit * 0.6D - 0.3D;
    }

    public static float scaffoldRandomSmallAngle(double randomUnit) {
        return (float) scaffoldRandomOffset(randomUnit);
    }

    public static float scaffoldRotationJitter(String rotationMode, double randomUnit) {
        return "Hypixel".equals(rotationMode) ? 0.0F : scaffoldRandomSmallAngle(randomUnit);
    }

    public static boolean scaffoldRaytraceAllowed(String raytraceMode, boolean sameFace) {
        return !"Strict".equals(raytraceMode) || sameFace;
    }

    public static String debugAgeText(long nowMillis, long thenMillis) {
        if (thenMillis == 0L) {
            return " age=never";
        }
        return " age=" + Math.max(0L, nowMillis - thenMillis) + "ms";
    }

    public static double jesusMotionY(boolean jumpDown, boolean inFluid, String mode, int tickCount, double currentY) {
        if (jumpDown) {
            return Math.max(currentY, 0.18D);
        }
        if (inFluid) {
            return 0.10D;
        }
        if ("Bounce".equals(mode)) {
            return tickCount % 2 == 0 ? 0.025D : 0.0D;
        }
        return Math.max(currentY, 0.0D);
    }
}
