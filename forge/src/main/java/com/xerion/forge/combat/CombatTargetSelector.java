package com.xerion.forge.combat;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.common.combat.CombatTargetRules;
import com.xerion.common.config.CombatSettings;
import com.xerion.forge.runtime.SocialProfileStore;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CombatTargetSelector {
    private static final float TARGET_SWITCH_DEGREES = 6.0f;
    private static final double TARGET_SWITCH_DISTANCE_SQ = 1.35 * 1.35;
    private static final float TARGET_SWITCH_HEALTH = 2.0f;

    private CombatTargetSelector() {
    }

    public static boolean validPlayerOrDummyTarget(LocalPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || target.isRemoved() || target.isSpectator()) {
            return false;
        }
        if (FakePlayerRegistry.isDummy(target)) {
            return true;
        }
        if (target.isInvisible() && !CombatSettings.targetInvisible()) {
            return false;
        }
        if (target instanceof Player otherPlayer) {
            return CombatSettings.targetPlayers()
                    && !AntiBotService.isBot(player, otherPlayer)
                    && !SocialProfileStore.isProtected(player, otherPlayer);
        }
        return false;
    }

    public static LivingEntity bestPlayerOrDummyTarget(LocalPlayer player, ClientLevel level, double range, double healthWeight) {
        double rangeSq = range * range;
        LivingEntity best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Player target : level.players()) {
            if (!validPlayerOrDummyTarget(player, target)) {
                continue;
            }
            double distance = player.distanceToSqr(target);
            if (distance > rangeSq) {
                continue;
            }
            double score = -distance - (target.getHealth() + target.getAbsorptionAmount()) * healthWeight;
            if (score > bestScore) {
                bestScore = score;
                best = target;
            }
        }
        return best;
    }

    public static LivingEntity closestAntiAimTarget(LocalPlayer player, ClientLevel level, double range) {
        double rangeSq = range * range;
        double bestDistance = Double.MAX_VALUE;
        LivingEntity best = null;
        for (LivingEntity target : nearbyLivingEntities(player, level, range)) {
            if (!validAntiAimTarget(player, target)) {
                continue;
            }
            double distance = CombatTargeting.distanceToBoxSqr(player, target);
            if (distance <= rangeSq && distance < bestDistance) {
                bestDistance = distance;
                best = target;
            }
        }
        return best;
    }

    private static boolean validAntiAimTarget(LocalPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || target.isRemoved() || target.isSpectator()) {
            return false;
        }
        if (FakePlayerRegistry.isDummy(target)) {
            return true;
        }
        if (target.isInvisible() && !CombatSettings.targetInvisible()) {
            return false;
        }
        if (target instanceof Player otherPlayer) {
            return CombatSettings.targetPlayers()
                    && !AntiBotService.isBot(player, otherPlayer)
                    && !SocialProfileStore.isProtected(player, otherPlayer);
        }
        return target instanceof Mob && CombatSettings.targetMobs();
    }

    public static boolean validTarget(LocalPlayer player, LivingEntity target, double range) {
        if (target == player || !target.isAlive() || target.isRemoved() || target.isSpectator()) {
            return false;
        }
        if (target.isInvisible() && !CombatSettings.targetInvisible()) {
            return false;
        }
        if (target instanceof Player otherPlayer) {
            if (!CombatSettings.targetPlayers()) {
                return false;
            }
            if (AntiBotService.isBot(player, otherPlayer)) {
                return false;
            }
            if (SocialProfileStore.isProtected(player, otherPlayer)) {
                return false;
            }
        } else if (target instanceof Mob) {
            if (!CombatSettings.targetMobs()) {
                return false;
            }
        } else {
            return false;
        }
        if (CombatTargeting.distanceToBoxSqr(player, target) > range * range) {
            return false;
        }
        return CombatSettings.throughWalls() || CombatTargeting.hasVisiblePart(player, target);
    }

    public static List<LivingEntity> aimAssistTargets(LocalPlayer player, ClientLevel level, double maxDistance, float aimRadius) {
        List<LivingEntity> targets = new ArrayList<>();
        for (LivingEntity target : nearbyLivingEntities(player, level, maxDistance)) {
            if (CombatAimMath.insideAimCircle(player, target, aimRadius)
                    && validTarget(player, target, maxDistance)) {
                targets.add(target);
            }
        }
        return targets;
    }

    public static List<LivingEntity> projectileAimTargets(LocalPlayer player, ClientLevel level, double maxDistance, float aimRadius) {
        List<LivingEntity> targets = new ArrayList<>();
        for (LivingEntity target : nearbyLivingEntities(player, level, maxDistance)) {
            if (CombatAimMath.insideAimCircle(player, target, aimRadius)
                    && validTarget(player, target, maxDistance)) {
                targets.add(target);
            }
        }
        return targets;
    }

    public static List<LivingEntity> gunAimTargets(LocalPlayer player, ClientLevel level, double maxDistance, float aimRadius) {
        List<LivingEntity> targets = new ArrayList<>();
        for (LivingEntity target : nearbyLivingEntities(player, level, maxDistance)) {
            if (CombatAimMath.ragebotAimCircleDistance(player, target) <= aimRadius
                    && validTarget(player, target, maxDistance)) {
                targets.add(target);
            }
        }
        return targets;
    }

    public static Comparator<LivingEntity> aimAssistComparator(LocalPlayer player) {
        return comparator(player, CombatSettings.aimAssistPriority());
    }

    public static Comparator<LivingEntity> projectileAimComparator(LocalPlayer player) {
        return comparator(player, CombatSettings.projectileAimPriority());
    }

    public static Comparator<LivingEntity> gunAimComparator(LocalPlayer player) {
        return (left, right) -> CombatTargetRules.compareAimTargets(
                CombatSettings.ragebotPriority(),
                CombatTargeting.distanceToBoxSqr(player, left),
                left.getHealth() + left.getAbsorptionAmount(),
                CombatAimMath.ragebotAimCircleDistance(player, left),
                CombatTargeting.distanceToBoxSqr(player, right),
                right.getHealth() + right.getAbsorptionAmount(),
                CombatAimMath.ragebotAimCircleDistance(player, right)
        );
    }

    public static LivingEntity selectStableAimTarget(LocalPlayer player, List<LivingEntity> targets, int currentTargetId) {
        return selectStableTarget(player, targets, currentTargetId, CombatSettings.aimAssistPriority());
    }

    public static LivingEntity selectStableProjectileTarget(LocalPlayer player, List<LivingEntity> targets, int currentTargetId) {
        return selectStableTarget(player, targets, currentTargetId, CombatSettings.projectileAimPriority());
    }

    public static LivingEntity selectStableGunTarget(LocalPlayer player, List<LivingEntity> targets, int currentTargetId) {
        LivingEntity best = bestTarget(targets, gunAimComparator(player));
        LivingEntity current = null;
        for (LivingEntity target : targets) {
            if (target.getId() == currentTargetId) {
                current = target;
                break;
            }
        }
        if (current == null || current == best) {
            return best;
        }

        return switch (CombatSettings.ragebotPriority()) {
            case CombatSettings.AIM_PRIORITY_DISTANCE -> {
                double bestDistance = CombatTargeting.distanceToBoxSqr(player, best);
                double currentDistance = CombatTargeting.distanceToBoxSqr(player, current);
                yield CombatTargetRules.shouldSwitchByDistance(
                        bestDistance,
                        currentDistance,
                        TARGET_SWITCH_DISTANCE_SQ) ? best : current;
            }
            case CombatSettings.AIM_PRIORITY_LOW_HEALTH -> {
                double bestDistance = CombatTargeting.distanceToBoxSqr(player, best);
                double currentDistance = CombatTargeting.distanceToBoxSqr(player, current);
                yield CombatTargetRules.shouldSwitchByLowHealth(
                        best.getHealth() + best.getAbsorptionAmount(),
                        current.getHealth() + current.getAbsorptionAmount(),
                        bestDistance,
                        currentDistance,
                        TARGET_SWITCH_HEALTH,
                        TARGET_SWITCH_DISTANCE_SQ) ? best : current;
            }
            default -> {
                float bestAngle = CombatAimMath.ragebotAimCircleDistance(player, best);
                float currentAngle = CombatAimMath.ragebotAimCircleDistance(player, current);
                yield CombatTargetRules.shouldSwitchByAngle(
                        bestAngle,
                        currentAngle,
                        TARGET_SWITCH_DEGREES) ? best : current;
            }
        };
    }

    private static Comparator<LivingEntity> comparator(LocalPlayer player, String priority) {
        return (left, right) -> CombatTargetRules.compareAimTargets(
                priority,
                CombatTargeting.distanceToBoxSqr(player, left),
                left.getHealth() + left.getAbsorptionAmount(),
                CombatAimMath.aimCircleDistance(player, left),
                CombatTargeting.distanceToBoxSqr(player, right),
                right.getHealth() + right.getAbsorptionAmount(),
                CombatAimMath.aimCircleDistance(player, right)
        );
    }

    private static List<LivingEntity> nearbyLivingEntities(LocalPlayer player, ClientLevel level, double range) {
        double inflation = Math.max(0.0D, range) + 1.0D;
        AABB searchBox = player.getBoundingBox().inflate(inflation);
        return level.getEntitiesOfClass(LivingEntity.class, searchBox);
    }

    private static LivingEntity selectStableTarget(LocalPlayer player, List<LivingEntity> targets, int currentTargetId, String priority) {
        LivingEntity best = bestTarget(targets, comparator(player, priority));
        LivingEntity current = null;
        for (LivingEntity target : targets) {
            if (target.getId() == currentTargetId) {
                current = target;
                break;
            }
        }
        if (current == null || current == best) {
            return best;
        }

        return switch (priority) {
            case CombatSettings.AIM_PRIORITY_DISTANCE -> {
                double bestDistance = CombatTargeting.distanceToBoxSqr(player, best);
                double currentDistance = CombatTargeting.distanceToBoxSqr(player, current);
                yield CombatTargetRules.shouldSwitchByDistance(
                        bestDistance,
                        currentDistance,
                        TARGET_SWITCH_DISTANCE_SQ) ? best : current;
            }
            case CombatSettings.AIM_PRIORITY_LOW_HEALTH -> {
                double bestDistance = CombatTargeting.distanceToBoxSqr(player, best);
                double currentDistance = CombatTargeting.distanceToBoxSqr(player, current);
                yield CombatTargetRules.shouldSwitchByLowHealth(
                        best.getHealth() + best.getAbsorptionAmount(),
                        current.getHealth() + current.getAbsorptionAmount(),
                        bestDistance,
                        currentDistance,
                        TARGET_SWITCH_HEALTH,
                        TARGET_SWITCH_DISTANCE_SQ) ? best : current;
            }
            default -> {
                float bestAngle = CombatAimMath.aimCircleDistance(player, best);
                float currentAngle = CombatAimMath.aimCircleDistance(player, current);
                yield CombatTargetRules.shouldSwitchByAngle(
                        bestAngle,
                        currentAngle,
                        TARGET_SWITCH_DEGREES) ? best : current;
            }
        };
    }

    private static LivingEntity bestTarget(List<LivingEntity> targets, Comparator<LivingEntity> comparator) {
        LivingEntity best = targets.get(0);
        for (int i = 1; i < targets.size(); i++) {
            LivingEntity candidate = targets.get(i);
            if (comparator.compare(candidate, best) < 0) {
                best = candidate;
            }
        }
        return best;
    }
}
