package com.xerion.common.combat;

import com.xerion.common.config.CombatSettings;

public final class CombatTargetRules {
    private static final double VISIBILITY_EPSILON = 0.04D;

    private CombatTargetRules() {
    }

    public static boolean shouldSwitchByDistance(double bestDistanceSq, double currentDistanceSq, double switchMarginSq) {
        return bestDistanceSq + switchMarginSq < currentDistanceSq;
    }

    public static boolean shouldSwitchByAngle(float bestAngle, float currentAngle, float switchDegrees) {
        return bestAngle + switchDegrees < currentAngle;
    }

    public static boolean shouldSwitchByLowHealth(float bestHealth, float currentHealth,
                                                  double bestDistanceSq, double currentDistanceSq,
                                                  float switchHealth, double switchDistanceSq) {
        if (bestHealth + switchHealth < currentHealth) {
            return true;
        }
        return Math.abs(bestHealth - currentHealth) <= switchHealth
                && bestDistanceSq + switchDistanceSq < currentDistanceSq;
    }

    public static int backtrackMaxAge(int configuredTicks, int absoluteMaxHistory) {
        return Math.max(1, Math.min(absoluteMaxHistory, configuredTicks));
    }

    public static boolean backtrackSameTick(long existingTick, long tick) {
        return existingTick == tick;
    }

    public static boolean backtrackEntryExpired(long currentTick, long entryTick, int maxAge) {
        return currentTick - entryTick > maxAge;
    }

    public static boolean backtrackHistoryTooLarge(int size, int absoluteMaxHistory) {
        return size > absoluteMaxHistory;
    }

    public static int compareAimTargets(String priority,
                                        double leftDistanceSq, double leftHealth, float leftAngle,
                                        double rightDistanceSq, double rightHealth, float rightAngle) {
        return switch (priority) {
            case CombatSettings.AIM_PRIORITY_DISTANCE -> compareByDistanceThenAngle(
                    leftDistanceSq, leftAngle, rightDistanceSq, rightAngle);
            case CombatSettings.AIM_PRIORITY_HITCHANCE -> compareByAngleThenDistance(
                    leftAngle, leftDistanceSq, rightAngle, rightDistanceSq);
            case CombatSettings.AIM_PRIORITY_LOW_HEALTH -> compareByHealthDistanceAngle(
                    leftHealth, leftDistanceSq, leftAngle, rightHealth, rightDistanceSq, rightAngle);
            default -> compareByAngleThenDistance(leftAngle, leftDistanceSq, rightAngle, rightDistanceSq);
        };
    }

    public static int compareKillAuraTargets(String priority,
                                             double leftDistanceSq, double leftHealth, int leftArmor, float leftAngle,
                                             double rightDistanceSq, double rightHealth, int rightArmor, float rightAngle) {
        return switch (priority) {
            case "Health" -> compareByHealthThenDistance(leftHealth, leftDistanceSq, rightHealth, rightDistanceSq);
            case "Armor" -> compareByArmorThenDistance(leftArmor, leftDistanceSq, rightArmor, rightDistanceSq);
            case "Angle" -> compareByAngleThenDistance(leftAngle, leftDistanceSq, rightAngle, rightDistanceSq);
            default -> Double.compare(leftDistanceSq, rightDistanceSq);
        };
    }

    public static float hudHealth(float health) {
        return Math.max(0.0F, health);
    }

    public static float hudMaxHealth(float maxHealth) {
        return Math.max(1.0F, maxHealth);
    }

    public static float hudAbsorption(float absorption) {
        return Math.max(0.0F, absorption);
    }

    public static boolean hudSnapshotExpired(long nowMillis, long updatedAtMillis, long timeoutMillis) {
        return nowMillis - updatedAtMillis > timeoutMillis;
    }

    public static boolean clearLineByDistance(double hitDistanceSq, double pointDistanceSq) {
        return hitDistanceSq + VISIBILITY_EPSILON >= pointDistanceSq;
    }

    public static double distanceToBoxSqr(double pointX, double pointY, double pointZ,
                                          double minX, double minY, double minZ,
                                          double maxX, double maxY, double maxZ) {
        double x = pointX < minX ? minX - pointX : Math.max(0.0D, pointX - maxX);
        double y = pointY < minY ? minY - pointY : Math.max(0.0D, pointY - maxY);
        double z = pointZ < minZ ? minZ - pointZ : Math.max(0.0D, pointZ - maxZ);
        return x * x + y * y + z * z;
    }

    public static double angularScore(double fromX, double fromY, double fromZ,
                                      double toX, double toY, double toZ,
                                      float currentYaw, float currentPitch) {
        CombatAimRules.Rotation rotation = CombatAimRules.rotationBetween(fromX, fromY, fromZ, toX, toY, toZ);
        double yawDelta = CombatAimRules.wrapDegrees(rotation.yaw() - currentYaw);
        double pitchDelta = rotation.pitch() - currentPitch;
        return yawDelta * yawDelta + pitchDelta * pitchDelta;
    }

    public static double raycastSearchInflation(double hitboxExpansion) {
        return hitboxExpansion + 1.0D;
    }

    public static double blockingDistanceSq(boolean blocked, double blockDistanceSq, double fallbackDistanceSq) {
        return blocked ? Math.min(fallbackDistanceSq, blockDistanceSq) : fallbackDistanceSq;
    }

    public static boolean betterRaycastHit(double hitDistanceSq, double bestDistanceSq) {
        return hitDistanceSq <= bestDistanceSq;
    }

    private static int compareByDistanceThenAngle(double leftDistanceSq, float leftAngle,
                                                  double rightDistanceSq, float rightAngle) {
        int distance = Double.compare(leftDistanceSq, rightDistanceSq);
        return distance != 0 ? distance : Float.compare(leftAngle, rightAngle);
    }

    private static int compareByHealthDistanceAngle(double leftHealth, double leftDistanceSq, float leftAngle,
                                                    double rightHealth, double rightDistanceSq, float rightAngle) {
        int health = Double.compare(leftHealth, rightHealth);
        if (health != 0) {
            return health;
        }
        return compareByDistanceThenAngle(leftDistanceSq, leftAngle, rightDistanceSq, rightAngle);
    }

    private static int compareByHealthThenDistance(double leftHealth, double leftDistanceSq,
                                                   double rightHealth, double rightDistanceSq) {
        int health = Double.compare(leftHealth, rightHealth);
        return health != 0 ? health : Double.compare(leftDistanceSq, rightDistanceSq);
    }

    private static int compareByArmorThenDistance(int leftArmor, double leftDistanceSq,
                                                  int rightArmor, double rightDistanceSq) {
        int armor = Integer.compare(leftArmor, rightArmor);
        return armor != 0 ? armor : Double.compare(leftDistanceSq, rightDistanceSq);
    }

    private static int compareByAngleThenDistance(float leftAngle, double leftDistanceSq,
                                                  float rightAngle, double rightDistanceSq) {
        int angle = Float.compare(leftAngle, rightAngle);
        return angle != 0 ? angle : Double.compare(leftDistanceSq, rightDistanceSq);
    }
}
