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

import com.xerion.forge.runtime.SocialProfileStore;

import com.xerion.common.combat.CombatActionRules;
import com.xerion.common.combat.CombatTargetRules;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class CombatRaycast {
    private static final double VANILLA_ENTITY_REACH = 3.0D;

    private CombatRaycast() {
    }

    public static boolean active() {
        return CombatSettings.reachEnabled()
                || CombatSettings.hitboxesEnabled()
                || CombatSettings.backtrackEnabled();
    }

    public static EntityHitResult pick(Minecraft minecraft, float partialTick) {
        if (!active()) {
            return null;
        }

        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null || player.isSpectator()) {
            return null;
        }

        double reach = CombatActionRules.effectiveReach(
                CombatSettings.reachEnabled(),
                CombatSettings.reach(),
                VANILLA_ENTITY_REACH);
        double expand = CombatActionRules.hitboxExpansion(
                CombatSettings.hitboxesEnabled(),
                CombatSettings.hitboxesSize());

        Vec3 start = player.getEyePosition(partialTick);
        Vec3 end = start.add(player.getViewVector(partialTick).scale(reach));
        double maxDistanceSq = reach * reach;
        double blockDistanceSq = blockingDistanceSq(minecraft.hitResult, start, maxDistanceSq);
        AABB searchBox = player.getBoundingBox()
                .expandTowards(end.subtract(start))
                .inflate(CombatTargetRules.raycastSearchInflation(expand));

        Entity bestEntity = null;
        Vec3 bestHit = null;
        double bestDistanceSq = Math.min(maxDistanceSq, blockDistanceSq);

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity target) || !targetAllowed(player, target)) {
                continue;
            }

            AABB box = target.getBoundingBox().inflate(expand);
            Hit hit = hitBox(target, box, searchBox, start, end);
            if (hit != null && CombatTargetRules.betterRaycastHit(hit.distanceSq, bestDistanceSq)) {
                bestEntity = target;
                bestHit = hit.location;
                bestDistanceSq = hit.distanceSq;
            }
        }

        if (CombatSettings.backtrackEnabled()) {
            for (BacktrackService.Entry entry : BacktrackService.entries()) {
                LivingEntity target = entry.entity();
                if (!targetAllowed(player, target)) {
                    continue;
                }
                AABB box = entry.box().inflate(expand);
                Hit hit = hitBox(target, box, searchBox, start, end);
                if (hit != null && CombatTargetRules.betterRaycastHit(hit.distanceSq, bestDistanceSq)) {
                    bestEntity = target;
                    bestHit = hit.location;
                    bestDistanceSq = hit.distanceSq;
                }
            }
        }

        return bestEntity == null ? null : new EntityHitResult(bestEntity, bestHit);
    }

    private static Hit hitBox(Entity entity, AABB box, AABB searchBox, Vec3 start, Vec3 end) {
        if (!box.intersects(searchBox)) {
            return null;
        }

        Optional<Vec3> clip = box.clip(start, end);
        if (box.contains(start)) {
            clip = Optional.of(start);
        }
        if (clip.isEmpty()) {
            return null;
        }

        Vec3 hit = clip.get();
        return new Hit(entity, hit, start.distanceToSqr(hit));
    }

    private static double blockingDistanceSq(HitResult hitResult, Vec3 start, double fallback) {
        return CombatTargetRules.blockingDistanceSq(
                hitResult != null && hitResult.getType() != HitResult.Type.MISS,
                hitResult == null ? fallback : start.distanceToSqr(hitResult.getLocation()),
                fallback
        );
    }

    private static boolean targetAllowed(LocalPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || target.isRemoved() || target.isSpectator()) {
            return false;
        }
        if (target.isInvisible() && !CombatSettings.targetInvisible()) {
            return false;
        }
        if (FakePlayerRegistry.isDummy(target)) {
            return CombatSettings.targetPlayers();
        }
        if (target instanceof Player otherPlayer) {
            return CombatSettings.targetPlayers()
                    && !AntiBotService.isBot(player, otherPlayer)
                    && !SocialProfileStore.isProtected(player, otherPlayer);
        }
        return target instanceof Mob && CombatSettings.targetMobs();
    }

    private record Hit(Entity entity, Vec3 location, double distanceSq) {
    }
}
