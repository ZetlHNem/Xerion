package com.xerion.common.combat;

import com.xerion.common.config.CombatSettings;

public final class CombatActionRules {
    private CombatActionRules() {
    }

    public static boolean shouldAutoHeal(float health, float absorption, boolean includeAbsorption, float threshold) {
        float effectiveHealth = includeAbsorption ? health + absorption : health;
        return effectiveHealth <= threshold;
    }

    public static boolean shouldReleaseBow(int chargeTicks, int requiredChargeTicks) {
        return chargeTicks >= requiredChargeTicks;
    }

    public static int cooldownTicks(int configuredDelay, int minimum) {
        return Math.max(minimum, configuredDelay);
    }

    public static int roundedDelayTicks(float delay) {
        return Math.max(0, Math.round(delay));
    }

    public static int raiseCooldown(int currentTicks, int minimumTicks) {
        return Math.max(currentTicks, minimumTicks);
    }

    public static boolean shouldRunImmediately(int delayTicks) {
        return delayTicks <= 0;
    }

    public static boolean isSwitchMode(String mode) {
        return "Switch".equals(mode);
    }

    public static boolean isMultiMode(String mode) {
        return "Multi".equals(mode);
    }

    public static boolean pearlInventorySwapAlreadyPrepared(String mode, int inventorySlot) {
        return isSwitchMode(mode) && inventorySlot >= 0;
    }

    public static int attackDelayTicks(float cps) {
        float clampedCps = Math.max(1.0F, cps);
        return Math.max(1, Math.round(20.0F / clampedCps));
    }

    public static float minimumCps(float cps) {
        return Math.max(1.0F, cps);
    }

    public static double minimumReach(double reach, double minimum) {
        return Math.max(minimum, reach);
    }

    public static double effectiveReach(boolean reachEnabled, double configuredReach, double vanillaReach) {
        return reachEnabled ? minimumReach(configuredReach, vanillaReach) : vanillaReach;
    }

    public static double hitboxExpansion(boolean hitboxesEnabled, double configuredSize) {
        return hitboxesEnabled ? Math.max(0.0D, configuredSize) : 0.0D;
    }

    public static boolean shouldSweepAttack(int targetCount, boolean shifting, boolean holdingSword) {
        return targetCount >= 2 && !shifting && holdingSword;
    }

    public static float requiredAttackStrength(boolean sweepAttack, String behaviorMode, boolean randomNextSmash) {
        if (sweepAttack) {
            return 0.92F;
        }
        if (CombatSettings.BEHAVIOR_SMASH.equals(behaviorMode)) {
            return 0.98F;
        }
        if (CombatSettings.BEHAVIOR_RANDOM.equals(behaviorMode)) {
            return randomNextSmash ? 0.98F : 0.0F;
        }
        return 0.0F;
    }

    public static float rotationJitter(float randomization) {
        return randomization * 0.015F;
    }

    public static boolean blockAnimationEnabled(boolean killAuraEnabled, String animationMode) {
        return killAuraEnabled && !CombatSettings.BLOCK_ANIMATION_OFF.equals(animationMode);
    }

    public static boolean timedAnimationActive(long nowNanos, long startNanos, long durationNanos) {
        long elapsed = nowNanos - startNanos;
        return elapsed >= 0L && elapsed <= durationNanos;
    }

    public static int armSign(boolean rightArm) {
        return rightArm ? 1 : -1;
    }

    public static boolean hitSoundAlreadyPlayed(int entityId, long gameTime, int lastEntityId, long lastGameTime) {
        return entityId == lastEntityId && gameTime == lastGameTime;
    }

    public static String hitSoundKey(String mode) {
        return switch (mode) {
            case "Pling" -> "pling";
            case "Bell" -> "bell";
            case "Click" -> "click";
            case "Pop" -> "pop";
            case "Crit" -> "crit";
            default -> "orb";
        };
    }

    public static double velocityMultiplier(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    public static float frameDeltaSeconds(long nowNanos, long lastNanos, float maxSeconds) {
        return lastNanos == 0L ? 0.0F : Math.min(maxSeconds, (nowNanos - lastNanos) / 1_000_000_000.0F);
    }

    public static boolean immediateAim(float smooth) {
        return smooth <= 0.5F;
    }

    public static int cappedTargetCount(int configuredLimit, int targetCount) {
        return Math.min(configuredLimit, targetCount);
    }

    public static int normalizedSwitchIndex(int switchIndex, int targetCount) {
        return switchIndex >= targetCount ? 0 : switchIndex;
    }

    public static int nextSwitchIndex(int switchIndex, int targetCount) {
        return (switchIndex + 1) % Math.max(1, targetCount);
    }

    public static boolean criticalStartDistanceAllowed(double currentDistanceSq, double range) {
        double maxStartRange = range + 0.65D;
        return currentDistanceSq <= maxStartRange * maxStartRange;
    }

    public static boolean criticalYawAllowed(float yawDelta) {
        return yawDelta <= 42.0F;
    }

    public static int criticalFallPredictionTicks() {
        return 6;
    }

    public static double criticalHitRange(double range) {
        return Math.max(2.7D, range - 0.12D);
    }

    public static boolean criticalPredictedDistanceAllowed(double predictedDistanceSq, double range) {
        double hitRange = criticalHitRange(range);
        return predictedDistanceSq <= hitRange * hitRange;
    }

    public static boolean criticalVerticalAllowed(double verticalDelta) {
        return verticalDelta < 2.35D;
    }

    public static boolean renderReturnComplete(float targetYaw, float renderYaw,
                                               float targetPitch, float renderPitch,
                                               float tolerance) {
        return Math.abs(CombatAimRules.wrapDegrees(targetYaw - renderYaw)) < tolerance
                && Math.abs(targetPitch - renderPitch) < tolerance;
    }
}
