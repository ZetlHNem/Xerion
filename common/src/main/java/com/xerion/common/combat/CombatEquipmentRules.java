package com.xerion.common.combat;

public final class CombatEquipmentRules {
    private CombatEquipmentRules() {
    }

    public static boolean allowedWeapon(String mode, String swordMode, String axeMode,
                                        boolean sword, boolean axe) {
        if (swordMode.equals(mode)) {
            return sword;
        }
        if (axeMode.equals(mode)) {
            return axe;
        }
        return sword || axe;
    }

    public static double weaponScore(double attackDamage, int enchantmentCount,
                                     boolean damageable, int maxDamage, int damageValue,
                                     boolean sword) {
        double enchantBonus = enchantmentCount * 0.35D;
        double durabilityBonus = damageable
                ? ((double) maxDamage - damageValue) / Math.max(1.0D, maxDamage) * 0.08D
                : 0.0D;
        double typeBias = sword ? 0.12D : 0.0D;
        return attackDamage + enchantBonus + durabilityBonus + typeBias;
    }

    public static int axeScore(boolean damageable, int maxDamage, int damageValue, int enchantmentCount) {
        int durability = damageable ? maxDamage - damageValue : 0;
        return durability + enchantmentCount * 1000;
    }
}