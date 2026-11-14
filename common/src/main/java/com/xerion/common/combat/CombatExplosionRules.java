package com.xerion.common.combat;

import com.xerion.common.config.CombatSettings;

public final class CombatExplosionRules {
    public static final double CRYSTAL_EXPLOSION_RADIUS = 12.0D;
    public static final double ANCHOR_EXPLOSION_RADIUS = 10.0D;
    public static final double BED_EXPLOSION_RADIUS = 10.0D;

    private CombatExplosionRules() {
    }

    public static double estimatedDamage(double distance, double radius, double baseDamage, int armorValue) {
        double exposure = Math.max(0.0D, 1.0D - distance / radius);
        double damage = (exposure * exposure + exposure) * baseDamage;
        double armorScale = 1.0D - Math.min(20, armorValue) * 0.025D;
        return damage * Math.max(0.45D, armorScale);
    }

    public static float autoCrystalMinDamage(float configuredMinDamage, boolean facePlace,
                                             double targetHealth, float facePlaceHealth) {
        if (facePlace && targetHealth <= facePlaceHealth) {
            return Math.min(configuredMinDamage, 1.0F);
        }
        return configuredMinDamage;
    }

    public static boolean validExplosionDamage(double targetDamage, double selfDamage, double selfHealth,
                                               float minDamage, float maxSelfDamage, boolean antiSuicide) {
        if (targetDamage < minDamage) {
            return false;
        }
        if (selfDamage > maxSelfDamage) {
            return false;
        }
        return !antiSuicide || selfDamage < selfHealth - 1.0F;
    }

    public static double autoCrystalTargetScore(String priority, double distanceSq, double targetHealth, double range) {
        return switch (priority) {
            case CombatSettings.AUTO_CRYSTAL_PRIORITY_DISTANCE -> -distanceSq;
            case CombatSettings.AUTO_CRYSTAL_PRIORITY_HEALTH -> -targetHealth;
            default -> autoCrystalTargetProximityDamageScore(distanceSq, targetHealth, range);
        };
    }

    public static double autoCrystalTargetProximityDamageScore(double distanceSq, double targetHealth, double range) {
        double health = Math.max(1.0D, targetHealth);
        double targetDistance = Math.sqrt(distanceSq);
        double rangeBias = Math.max(0.0D, range - targetDistance);
        return rangeBias * 8.0D - health * 0.8D - distanceSq * 0.02D;
    }

    public static double autoCrystalCrystalScore(String priority, double playerExplosionDistanceSq,
                                                 double targetHealth, double targetDamage, double selfDamage) {
        double distancePenalty = playerExplosionDistanceSq * 0.015D;
        return switch (priority) {
            case CombatSettings.AUTO_CRYSTAL_PRIORITY_DISTANCE -> -distancePenalty + targetDamage * 2.0D - selfDamage;
            case CombatSettings.AUTO_CRYSTAL_PRIORITY_HEALTH ->
                    targetDamage * 3.0D - targetHealth - selfDamage * 1.15D;
            default -> targetDamage * 4.0D - selfDamage * 1.35D - distancePenalty;
        };
    }

    public static double anchorBreakScore(double targetDamage, double selfDamage, double playerDistanceSq) {
        return targetDamage * 3.2D - selfDamage * 1.3D - playerDistanceSq * 0.015D;
    }

    public static double anchorPlaceScore(double targetDamage, double selfDamage, double playerDistanceSq) {
        return targetDamage * 3.0D - selfDamage * 1.35D - playerDistanceSq * 0.015D;
    }
    public static double bedBreakScore(double targetDamage, double selfDamage, double playerDistanceSq) {
        return targetDamage * 3.0D - selfDamage * 1.25D - playerDistanceSq * 0.015D;
    }

    public static double bedPlaceScore(double targetDamage, double selfDamage, double playerDistanceSq) {
        return targetDamage * 3.0D - selfDamage * 1.35D - playerDistanceSq * 0.015D;
    }}