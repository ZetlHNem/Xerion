package com.xerion.common.player;

import com.xerion.common.config.CombatSettings;

import java.util.Locale;

public final class PlayerItemRules {
    private static final double ARMOR_DEFENSE_WEIGHT = 10.0D;
    private static final double ARMOR_TOUGHNESS_WEIGHT = 4.0D;
    private static final double ARMOR_ENCHANTMENT_WEIGHT = 0.75D;
    private static final double ARMOR_DURABILITY_WEIGHT = 3.0D;
    private static final double TOOL_DURABILITY_WEIGHT = 0.1D;

    private PlayerItemRules() {
    }

    public static double armorScore(int defense, float toughness, int enchantmentCount,
                                    boolean preferDurability, boolean damageable,
                                    int maxDamage, int damageValue) {
        double score = defense * ARMOR_DEFENSE_WEIGHT + toughness * ARMOR_TOUGHNESS_WEIGHT;
        score += enchantmentCount * ARMOR_ENCHANTMENT_WEIGHT;
        if (preferDurability && damageable) {
            score += remainingDurabilityRatio(damageable, maxDamage, damageValue) * ARMOR_DURABILITY_WEIGHT;
        }
        return score;
    }

    public static boolean isArmorUpgrade(double candidateScore, double currentScore, double minimumGain) {
        return candidateScore - currentScore > minimumGain;
    }

    public static float foodScore(int nutrition, float saturationModifier) {
        return nutrition * 2.0F + saturationModifier;
    }

    public static boolean needsFood(int foodLevel, boolean needsFood, float threshold) {
        return foodLevel <= threshold && needsFood;
    }

    public static boolean betterFoodCandidate(float candidateScore, float bestScore) {
        return candidateScore > bestScore;
    }

    public static int positiveDelayTicks(int configuredTicks) {
        return Math.max(1, configuredTicks);
    }

    public static int nonNegativeDelayTicks(int configuredTicks) {
        return Math.max(0, configuredTicks);
    }

    public static int roundedPositiveDelayTicks(float configuredTicks) {
        return positiveDelayTicks(Math.round(configuredTicks));
    }

    public static int roundedNonNegativeDelayTicks(float configuredTicks) {
        return nonNegativeDelayTicks(Math.round(configuredTicks));
    }

    public static int refillThreshold(float configuredThreshold) {
        return Math.max(1, Math.round(configuredThreshold));
    }

    public static int hotbarScanStart(boolean currentSlotOnly, int selectedSlot) {
        return currentSlotOnly ? selectedSlot : 0;
    }

    public static int hotbarScanEnd(boolean currentSlotOnly, int selectedSlot) {
        return currentSlotOnly ? selectedSlot + 1 : 9;
    }

    public static boolean shouldSkipRefill(boolean currentEmpty, int currentCount, int maxStackSize, int threshold) {
        return !currentEmpty && (currentCount > threshold || currentCount >= maxStackSize);
    }

    public static boolean betterRefillSource(int candidateCount, int bestCount) {
        return candidateCount > bestCount;
    }

    public static double toolScore(boolean empty, double destroySpeed, int efficiencyLevel, boolean correctTool,
                                   boolean preferDurability, boolean damageable,
                                   int maxDamage, int damageValue) {
        if (empty) {
            return 1.0D;
        }
        if (destroySpeed <= 1.0D) {
            return destroySpeed;
        }
        double score = destroySpeed + (efficiencyLevel > 0 ? efficiencyLevel * efficiencyLevel + 1.0D : 0.0D);
        if (correctTool) {
            score += 0.25D;
        }
        if (preferDurability && damageable) {
            score += remainingDurabilityRatio(damageable, maxDamage, damageValue) * TOOL_DURABILITY_WEIGHT;
        }
        return score;
    }

    public static boolean betterDurability(boolean preferDurability,
                                           boolean candidateDamageable, int candidateMaxDamage, int candidateDamage,
                                           boolean currentDamageable, int currentMaxDamage, int currentDamage) {
        return preferDurability
                && candidateDamageable
                && remainingDurabilityRatio(candidateDamageable, candidateMaxDamage, candidateDamage)
                > remainingDurabilityRatio(currentDamageable, currentMaxDamage, currentDamage);
    }

    public static boolean betterToolCandidate(double candidateScore, double bestScore, boolean betterDurability) {
        return candidateScore > bestScore || candidateScore == bestScore && betterDurability;
    }

    public static int selectedToolSlot(double bestScore, double currentScore, int bestSlot, int selectedSlot) {
        return bestScore > currentScore || bestSlot == selectedSlot ? bestSlot : -1;
    }

    public static double remainingDurabilityRatio(boolean damageable, int maxDamage, int damageValue) {
        if (!damageable) {
            return 1.0D;
        }
        return (double) (maxDamage - damageValue) / Math.max(1, maxDamage);
    }

    public static boolean autoExpChecksArmor(String mode) {
        return !CombatSettings.AUTO_EXP_MODE_HELD.equals(mode);
    }

    public static boolean autoExpChecksHeld(String mode) {
        return !CombatSettings.AUTO_EXP_MODE_ARMOR.equals(mode);
    }

    public static boolean autoExpShouldRestoreSlot(boolean restoreSlot, int selectedSlot, int previousSlot) {
        return restoreSlot && selectedSlot >= 0 && selectedSlot != previousSlot;
    }

    public static float autoExpThrowPitch() {
        return 90.0F;
    }

    public static boolean needsRepair(boolean empty, boolean damageable, int damageValue,
                                      boolean mendingOnly, int mendingLevel,
                                      int maxDamage, float durabilityThreshold) {
        if (empty || !damageable || damageValue <= 0) {
            return false;
        }
        if (mendingOnly && mendingLevel <= 0) {
            return false;
        }
        float remaining = (maxDamage - damageValue) * 100.0F / Math.max(1, maxDamage);
        return remaining <= durabilityThreshold;
    }

    public static boolean fishingRodTooDamaged(boolean damageable, int maxDamage, int damageValue) {
        return damageable && maxDamage - damageValue <= 1;
    }

    public static boolean autoFishHasHook(boolean hookPresent, boolean hookRemoved) {
        return hookPresent && !hookRemoved;
    }

    public static boolean autoFishReadyForRecast(boolean waitingForRecast, int recastCooldown) {
        return waitingForRecast && recastCooldown <= 0;
    }

    public static boolean autoFishReadyForAutoCast(boolean autoCast, int recastCooldown) {
        return autoCast && recastCooldown <= 0;
    }

    public static boolean autoFishHookBiting(boolean biting, int nibbleTicks) {
        return biting || nibbleTicks > 0;
    }

    public static boolean fastUseChargeReady(int chargeTicks, int requiredChargeTicks) {
        return chargeTicks >= requiredChargeTicks;
    }

    public static boolean fastUseThrowableAllowed(boolean throwablesEnabled,
                                                  boolean enderPearl, boolean autoPearlEnabled,
                                                  boolean experienceBottle, boolean autoExpEnabled) {
        if (!throwablesEnabled) {
            return false;
        }
        if (enderPearl && autoPearlEnabled) {
            return false;
        }
        return !experienceBottle || !autoExpEnabled;
    }

    public static boolean delayedSortAction(String action) {
        return "Delayed".equals(action);
    }

    public static int autoSortStartSlot(boolean includeHotbar) {
        return includeHotbar ? 0 : 9;
    }

    public static int autoSortEndSlot() {
        return 36;
    }

    public static int autoSortGuardLimit() {
        return 256;
    }

    public static boolean autoSortShouldContinue(int remainingGuard, boolean carriedEmpty) {
        return remainingGuard > 0 && carriedEmpty;
    }

    public static boolean autoSortMergeTarget(boolean empty, int count, int maxStackSize) {
        return !empty && count < maxStackSize;
    }

    public static boolean autoSortMergeSource(boolean empty, boolean sameItemSameTags) {
        return !empty && sameItemSameTags;
    }

    public static int autoSortDesiredSlot(int startSlot, int sortedIndex) {
        return startSlot + sortedIndex;
    }

    public static int compareSortedStacks(String sortMode,
                                          int leftCategory, String leftId, int leftCount,
                                          int rightCategory, String rightId, int rightCount) {
        if ("Count".equals(sortMode)) {
            int count = Integer.compare(rightCount, leftCount);
            if (count != 0) {
                return count;
            }
        }
        int category = Integer.compare(leftCategory, rightCategory);
        if ("Category".equals(sortMode) && category != 0) {
            return category;
        }
        int id = leftId.compareTo(rightId);
        if (id != 0) {
            return id;
        }
        return Integer.compare(rightCount, leftCount);
    }

    public static boolean containsConfigured(String config, String itemId) {
        if (config == null || config.isBlank()) {
            return false;
        }
        String normalizedItemId = itemId.toLowerCase(Locale.ROOT);
        for (String token : config.split("[,;\\s]+")) {
            if (!token.isBlank() && normalizedItemId.contains(token.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static int inventoryCleanerStartSlot(boolean cleanHotbar) {
        return cleanHotbar ? 0 : 9;
    }

    public static int inventoryCleanerEndSlot() {
        return 36;
    }

    public static boolean inventoryCleanerCandidate(boolean empty) {
        return !empty;
    }

    public static boolean chestStealSlotCandidate(boolean playerInventorySlot, boolean hasItem) {
        return !playerInventorySlot && hasItem;
    }

    public static boolean chestStealHasMove(int slot) {
        return slot >= 0;
    }

    public static boolean chestStealShouldAutoClose(boolean autoClose, int firstItemSlot) {
        return autoClose && firstItemSlot < 0;
    }

    public static boolean lockedMineTarget(String targetMode) {
        return "Locked Block".equals(targetMode);
    }

    public static boolean autoMineMineable(boolean air, float destroySpeed) {
        return !air && destroySpeed >= 0.0F;
    }

    public static boolean autoMineTargetChanged(boolean sameTarget) {
        return !sameTarget;
    }

    public static boolean autoMineShouldRetryStart(boolean continueAccepted) {
        return !continueAccepted;
    }

    public static boolean withinRangeSq(double distanceSq, double range) {
        return distanceSq <= range * range;
    }

    public static boolean quickSwitchHoldMode(String mode) {
        return "Hold".equals(mode);
    }

    public static boolean quickSwitchToggleMode(String mode) {
        return "Toggle".equals(mode);
    }

    public static double distanceFromSqr(double distanceSq) {
        return Math.sqrt(distanceSq);
    }

    public static float fakePlayerHealth(float configuredHealth) {
        return Math.max(1.0F, configuredHealth);
    }

    public static float hurtDirection(double attackerX, double attackerZ, double targetX, double targetZ) {
        double dx = attackerX - targetX;
        double dz = attackerZ - targetZ;
        return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
    }

    public static boolean healthAttributeChanged(float currentMaxHealth, float configuredHealth) {
        return Math.abs(currentMaxHealth - configuredHealth) > 0.01F;
    }

    public static float clampedHealth(float configuredHealth, float maxHealth) {
        return Math.min(configuredHealth, maxHealth);
    }
}
