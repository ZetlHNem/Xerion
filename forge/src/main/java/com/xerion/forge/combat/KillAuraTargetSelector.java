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

import com.xerion.common.combat.CombatTargetCandidate;
import com.xerion.common.combat.CombatTargetSelection;
import com.xerion.common.config.CombatSettings;
import com.xerion.forge.runtime.SocialProfileStore;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public final class KillAuraTargetSelector {
    private KillAuraTargetSelector() {
    }

    public static List<LivingEntity> targets(LocalPlayer player, ClientLevel level, double range) {
        if (player == null || level == null) {
            return List.of();
        }
        double clampedRange = CombatTargetSelection.clampKillAuraRange(range);
        AABB searchBox = player.getBoundingBox().inflate(clampedRange + 1.0D);
        List<TargetEntry> entries = new ArrayList<>();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            CombatTargetCandidate candidate = allowedCandidate(player, target, clampedRange);
            if (candidate != null) {
                entries.add(new TargetEntry(target, candidate));
            }
        }

        String priority = CombatSettings.priority();
        entries.sort((left, right) -> CombatTargetSelection.compareKillAuraCandidates(
                priority, left.candidate(), right.candidate()));
        List<LivingEntity> targets = new ArrayList<>(entries.size());
        for (TargetEntry entry : entries) {
            targets.add(entry.target());
        }
        return targets;
    }

    public static boolean validTarget(LocalPlayer player, LivingEntity target, double range) {
        return player != null && target != null
                && allowedCandidate(player, target, CombatTargetSelection.clampKillAuraRange(range)) != null;
    }

    public static double closestTargetDistance(LocalPlayer player, ClientLevel level, double range) {
        LivingEntity nearest = nearestTarget(player, level, range);
        return nearest == null ? Double.MAX_VALUE : player.distanceTo(nearest);
    }

    public static LivingEntity nearestTarget(LocalPlayer player, ClientLevel level, double range) {
        if (player == null || level == null) {
            return null;
        }
        double clampedRange = CombatTargetSelection.clampKillAuraRange(range);
        AABB searchBox = player.getBoundingBox().inflate(clampedRange + 1.0D);
        LivingEntity nearest = null;
        double closest = Double.MAX_VALUE;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            CombatTargetCandidate candidate = candidate(player, target, clampedRange);
            if (candidate != null) {
                double distance = Math.sqrt(candidate.distanceSq());
                if (distance < closest) {
                    closest = distance;
                    nearest = target;
                }
            }
        }
        return nearest;
    }

    public static LivingEntity nearestVisibleTarget(LocalPlayer player, ClientLevel level, double range) {
        if (player == null || level == null) {
            return null;
        }
        double clampedRange = CombatTargetSelection.clampKillAuraRange(range);
        AABB searchBox = player.getBoundingBox().inflate(clampedRange + 1.0D);
        LivingEntity nearest = null;
        double closest = Double.MAX_VALUE;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            CombatTargetCandidate candidate = allowedCandidate(player, target, clampedRange);
            if (candidate != null) {
                double distance = Math.sqrt(candidate.distanceSq());
                if (distance < closest) {
                    closest = distance;
                    nearest = target;
                }
            }
        }
        return nearest;
    }

    private static CombatTargetCandidate allowedCandidate(LocalPlayer player, LivingEntity target, double range) {
        CombatTargetCandidate candidate = candidate(player, target, range);
        if (candidate == null) {
            return null;
        }
        boolean needsVisibility = !CombatSettings.throughWalls() && CombatSettings.rayTrace();
        boolean visible = !needsVisibility || CombatTargeting.hasVisiblePart(player, target);
        candidate = candidate.withVisible(visible);
        return CombatTargetSelection.killAuraAllowed(candidate,
                CombatSettings.targetPlayers(), CombatSettings.targetMobs(),
                CombatSettings.targetInvisible(), CombatSettings.throughWalls(), CombatSettings.rayTrace())
                ? candidate : null;
    }

    private static CombatTargetCandidate candidate(LocalPlayer player, LivingEntity target, double range) {
        boolean targetPlayer = target instanceof Player;
        CombatTargetCandidate.TargetKind kind = targetKind(target);
        boolean bot = targetPlayer && AntiBotService.isBot(player, (Player) target);
        boolean protectedProfile = targetPlayer && SocialProfileStore.isProtected(player, (Player) target);
        double distanceSq = CombatTargeting.distanceToBoxSqr(player, target);

        if (target == player || !target.isAlive() || target.isRemoved() || target.isSpectator()
                || target.isInvisible() && !CombatSettings.targetInvisible()
                || kind == CombatTargetCandidate.TargetKind.PLAYER && (!CombatSettings.targetPlayers() || bot || protectedProfile)
                || kind == CombatTargetCandidate.TargetKind.MOB && !CombatSettings.targetMobs()
                || kind == CombatTargetCandidate.TargetKind.OTHER
                || distanceSq > range * range
                || !KillAuraAimMath.insideFov(player, target, CombatSettings.fov())) {
            return null;
        }

        return new CombatTargetCandidate(
                target.getId(),
                kind,
                false,
                true,
                false,
                false,
                false,
                bot,
                protectedProfile,
                distanceSq,
                range * range,
                true,
                false,
                target.getHealth(),
                target.getAbsorptionAmount(),
                armorValue(target),
                KillAuraAimMath.yawDelta(player, target)
        );
    }

    private static CombatTargetCandidate.TargetKind targetKind(LivingEntity target) {
        if (target instanceof Player) {
            return CombatTargetCandidate.TargetKind.PLAYER;
        }
        if (target instanceof Mob) {
            return CombatTargetCandidate.TargetKind.MOB;
        }
        return CombatTargetCandidate.TargetKind.OTHER;
    }

    private static int armorValue(LivingEntity target) {
        int value = 0;
        for (var stack : target.getArmorSlots()) {
            value += stack.getDamageValue() >= stack.getMaxDamage() ? 0 : stack.getMaxDamage() - stack.getDamageValue();
        }
        return value;
    }

    private record TargetEntry(LivingEntity target, CombatTargetCandidate candidate) {
    }
}
