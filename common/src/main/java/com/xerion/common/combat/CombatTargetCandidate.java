package com.xerion.common.combat;

public record CombatTargetCandidate(
        int entityId,
        TargetKind kind,
        boolean self,
        boolean alive,
        boolean removed,
        boolean spectator,
        boolean invisible,
        boolean bot,
        boolean protectedProfile,
        double distanceSq,
        double rangeSq,
        boolean insideFov,
        boolean visible,
        double health,
        double absorption,
        int armor,
        float yawDelta
) {
    public double effectiveHealth() {
        return health + absorption;
    }

    public CombatTargetCandidate withVisible(boolean newVisible) {
        return new CombatTargetCandidate(
                entityId,
                kind,
                self,
                alive,
                removed,
                spectator,
                invisible,
                bot,
                protectedProfile,
                distanceSq,
                rangeSq,
                insideFov,
                newVisible,
                health,
                absorption,
                armor,
                yawDelta
        );
    }

    public enum TargetKind {
        PLAYER,
        MOB,
        OTHER
    }
}
