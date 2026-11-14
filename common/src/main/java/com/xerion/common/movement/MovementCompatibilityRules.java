package com.xerion.common.movement;

public final class MovementCompatibilityRules {
    private MovementCompatibilityRules() {
    }

    public static double randomizedScale(float percent, double sample) {
        double amount = Math.max(0.0D, percent) / 100.0D;
        double centered = Math.max(0.0D, Math.min(1.0D, sample)) * 2.0D - 1.0D;
        return 1.0D + centered * amount;
    }
}
