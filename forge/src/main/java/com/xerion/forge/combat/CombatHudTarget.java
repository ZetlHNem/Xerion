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

import com.xerion.common.combat.CombatTargetRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class CombatHudTarget {
    private static final Minecraft MC = Minecraft.getInstance();
    private static int entityId = -1;
    private static String name = "";
    private static float health;
    private static float maxHealth = 20.0f;
    private static float absorption;
    private static float distance;
    private static ResourceLocation skin;
    private static long updatedAtMillis;

    private CombatHudTarget() {
    }

    public static void update(LivingEntity target) {
        LocalPlayer player = MC.player;
        if (target == null || player == null || !target.isAlive() || target.isRemoved()) {
            clear();
            return;
        }
        entityId = target.getId();
        name = target.getDisplayName().getString();
        health = CombatTargetRules.hudHealth(target.getHealth());
        maxHealth = CombatTargetRules.hudMaxHealth(target.getMaxHealth());
        absorption = CombatTargetRules.hudAbsorption(target.getAbsorptionAmount());
        distance = player.distanceTo(target);
        skin = target instanceof AbstractClientPlayer clientPlayer ? clientPlayer.getSkinTextureLocation() : null;
        updatedAtMillis = System.currentTimeMillis();
    }

    public static void clear() {
        entityId = -1;
        name = "";
        health = 0.0f;
        maxHealth = 20.0f;
        absorption = 0.0f;
        distance = 0.0f;
        skin = null;
        updatedAtMillis = 0L;
    }

    public static Snapshot snapshot() {
        if (entityId < 0 || CombatTargetRules.hudSnapshotExpired(System.currentTimeMillis(), updatedAtMillis, 1200L)) {
            Snapshot crosshair = crosshairSnapshot();
            return crosshair.present() ? crosshair : Snapshot.empty();
        }
        return new Snapshot(true, entityId, name, health, maxHealth, absorption, distance, skin);
    }

    private static Snapshot crosshairSnapshot() {
        LocalPlayer player = MC.player;
        if (player == null || !(MC.hitResult instanceof EntityHitResult hit)
                || hit.getType() != HitResult.Type.ENTITY
                || !(hit.getEntity() instanceof LivingEntity target)
                || target == player
                || !target.isAlive()
                || target.isRemoved()
                || target.isSpectator()) {
            return Snapshot.empty();
        }
        if (target instanceof Player other && SocialProfileStore.isProtected(player, other)) {
            return Snapshot.empty();
        }
        return new Snapshot(true,
                target.getId(),
                target.getDisplayName().getString(),
                CombatTargetRules.hudHealth(target.getHealth()),
                CombatTargetRules.hudMaxHealth(target.getMaxHealth()),
                CombatTargetRules.hudAbsorption(target.getAbsorptionAmount()),
                player.distanceTo(target),
                target instanceof AbstractClientPlayer clientPlayer ? clientPlayer.getSkinTextureLocation() : null);
    }

    public static LivingEntity entity(int id) {
        if (id < 0 || MC.level == null) {
            return null;
        }
        Entity entity = MC.level.getEntity(id);
        return entity instanceof LivingEntity living && living.isAlive() && !living.isRemoved() ? living : null;
    }

    public record Snapshot(boolean present, int entityId, String name, float health, float maxHealth, float absorption,
                           float distance, ResourceLocation skin) {
        public static Snapshot empty() {
            return new Snapshot(false, -1, "Target", 16.0f, 20.0f, 2.0f, 6.4f, null);
        }
    }
}
