package com.xerion.common.combat;

public final class CombatTargetSelection {
    private CombatTargetSelection() {
    }

    public static double clampKillAuraRange(double range) {
        return Math.max(0.5D, range);
    }

    public static boolean killAuraAllowed(CombatTargetCandidate candidate,
                                          boolean targetPlayers,
                                          boolean targetMobs,
                                          boolean targetInvisible,
                                          boolean throughWalls,
                                          boolean rayTrace) {
        if (candidate.self() || !candidate.alive() || candidate.removed() || candidate.spectator()) {
            return false;
        }
        if (candidate.invisible() && !targetInvisible) {
            return false;
        }
        if (candidate.kind() == CombatTargetCandidate.TargetKind.PLAYER) {
            if (!targetPlayers || candidate.bot() || candidate.protectedProfile()) {
                return false;
            }
        } else if (candidate.kind() == CombatTargetCandidate.TargetKind.MOB) {
            if (!targetMobs) {
                return false;
            }
        } else {
            return false;
        }
        if (candidate.distanceSq() > candidate.rangeSq()) {
            return false;
        }
        if (!candidate.insideFov()) {
            return false;
        }
        return throughWalls || !rayTrace || candidate.visible();
    }

    public static int compareKillAuraCandidates(String priority,
                                                CombatTargetCandidate left,
                                                CombatTargetCandidate right) {
        return CombatTargetRules.compareKillAuraTargets(
                priority,
                left.distanceSq(),
                left.effectiveHealth(),
                left.armor(),
                left.yawDelta(),
                right.distanceSq(),
                right.effectiveHealth(),
                right.armor(),
                right.yawDelta()
        );
    }
}
