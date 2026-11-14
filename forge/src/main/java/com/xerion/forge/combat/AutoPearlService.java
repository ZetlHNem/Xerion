package com.xerion.forge.combat;

import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.combat.CombatTargetRules;
import com.xerion.common.movement.ScaffoldRotation;

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
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class AutoPearlService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final float PEARL_SPEED = 1.5f;
    private static final float PEARL_GRAVITY = 0.03f;
    private static final float TARGET_SWITCH_DEGREES = 6.0f;
    private static final double TARGET_SWITCH_DISTANCE_SQ = 1.35D * 1.35D;
    private static final float TARGET_SWITCH_HEALTH = 2.0f;

    private int cooldownTicks;
    private int targetId = -1;
    private long lastAimNanos;
    private boolean aimInitialized;
    private boolean hasTarget;
    private float aimYaw;
    private float aimPitch;
    private float targetYaw;
    private float targetPitch;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!CombatSettings.autoPearlEnabled()) {
            resetAim();
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null
                || player.isSpectator() || CombatSettings.killAuraEnabled()) {
            resetAim();
            return;
        }
        if (cooldownTicks > 0 || player.getCooldowns().isOnCooldown(Items.ENDER_PEARL)) {
            return;
        }

        PearlHand hand = findPearl(player);
        if (hand == null) {
            resetAim();
            return;
        }

        LivingEntity target = findTarget(player, level);
        if (target == null) {
            resetAim();
            return;
        }

        Rotation rotation = pearlRotationTo(player, target);
        if (rotation == null) {
            resetAim();
            return;
        }

        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }

        if (CombatSettings.autoPearlSilentRotate()) {
            ScaffoldRotation.requestPacketOnly(rotation.yaw(), CombatAimRules.clamp(rotation.pitch(), -90.0f, 90.0f), true);
        } else {
            player.setYRot(rotation.yaw());
            player.setXRot(CombatAimRules.clamp(rotation.pitch(), -90.0f, 90.0f));
        }

        InteractionResult result = MC.gameMode.useItem(player, hand.hand());
        if (result.consumesAction()) {
            player.swing(hand.hand());
            cooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.autoPearlDelayTicks(), 1);
        }

        if (CombatSettings.autoPearlRestoreSlot() && hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
    }

    public void renderTick(float partialTick) {
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (!CombatSettings.autoPearlEnabled() || !CombatSettings.pearlAimEnabled()
                || player == null || level == null || MC.screen != null || player.isSpectator()
                || CombatSettings.killAuraEnabled() || !holdingPearl(player)) {
            resetAim();
            return;
        }

        LivingEntity target = findTarget(player, level);
        if (target == null) {
            resetAim();
            return;
        }
        Rotation rotation = pearlRotationTo(player, target);
        if (rotation == null) {
            resetAim();
            return;
        }

        if (target.getId() != targetId || !aimInitialized) {
            targetId = target.getId();
            aimYaw = player.getYRot();
            aimPitch = player.getXRot();
            aimInitialized = true;
            lastAimNanos = System.nanoTime();
        }
        targetYaw = rotation.yaw();
        targetPitch = rotation.pitch();
        hasTarget = true;
        applyAim(player);
    }

    private void applyAim(LocalPlayer player) {
        if (!hasTarget) {
            return;
        }
        long now = System.nanoTime();
        float dt = lastAimNanos == 0L ? 0.0f : Math.min(0.05f, (now - lastAimNanos) / 1_000_000_000.0f);
        lastAimNanos = now;
        if (dt <= 0.0f) {
            return;
        }

        if (CombatSettings.pearlAimSmooth() <= 0.5f) {
            aimYaw = targetYaw;
            aimPitch = CombatAimRules.clamp(targetPitch, -90.0f, 90.0f);
            player.setYRot(aimYaw);
            player.setXRot(aimPitch);
            return;
        }

        float alpha = aimLerpAlpha(dt);
        float maxStep = aimMaxStep(dt);
        aimYaw = smoothYawStep(player.getYRot(), targetYaw, alpha, maxStep);
        aimPitch = smoothStep(player.getXRot(), targetPitch, alpha * 0.82f, maxStep * 0.72f);
        aimPitch = CombatAimRules.clamp(aimPitch, -90.0f, 90.0f);
        player.setYRot(aimYaw);
        player.setXRot(aimPitch);
    }

    private LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        double maxDistance = CombatSettings.autoPearlDistance();
        float aimRadius = CombatSettings.pearlAimRange();
        List<LivingEntity> targets = new java.util.ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof LivingEntity target
                    && validTarget(player, target, maxDistance)
                    && insideAimCircle(player, target, aimRadius)
                    && (CombatSettings.throughWalls() || CombatTargeting.hasVisiblePart(player, target))) {
                targets.add(target);
            }
        }
        if (targets.isEmpty()) {
            return null;
        }
        targets.sort(targetComparator(player));
        return selectStableTarget(player, targets);
    }

    private LivingEntity selectStableTarget(LocalPlayer player, List<LivingEntity> targets) {
        LivingEntity best = targets.get(0);
        LivingEntity ccurrent = null;
        for (LivingEntity target : targets) {
            if (target.getId() == targetId) {
                ccurrent = target;
                break;
            }
        }
        if (ccurrent == null || ccurrent == best) {
            return best;
        }

        return switch (CombatSettings.pearlAimPriority()) {
            case CombatSettings.AIM_PRIORITY_DISTANCE -> {
                double bestDistance = CombatTargeting.distanceToBoxSqr(player, best);
                double ccurrentDistance = CombatTargeting.distanceToBoxSqr(player, ccurrent);
                yield CombatTargetRules.shouldSwitchByDistance(
                        bestDistance,
                        ccurrentDistance,
                        TARGET_SWITCH_DISTANCE_SQ) ? best : ccurrent;
            }
            case CombatSettings.AIM_PRIORITY_LOW_HEALTH -> {
                double bestDistance = CombatTargeting.distanceToBoxSqr(player, best);
                double ccurrentDistance = CombatTargeting.distanceToBoxSqr(player, ccurrent);
                yield CombatTargetRules.shouldSwitchByLowHealth(
                        best.getHealth() + best.getAbsorptionAmount(),
                        ccurrent.getHealth() + ccurrent.getAbsorptionAmount(),
                        bestDistance,
                        ccurrentDistance,
                        TARGET_SWITCH_HEALTH,
                        TARGET_SWITCH_DISTANCE_SQ) ? best : ccurrent;
            }
            default -> {
                float bestAngle = aimCircleDistance(player, best);
                float ccurrentAngle = aimCircleDistance(player, ccurrent);
                yield CombatTargetRules.shouldSwitchByAngle(
                        bestAngle,
                        ccurrentAngle,
                        TARGET_SWITCH_DEGREES) ? best : ccurrent;
            }
        };
    }

    private static boolean validTarget(LocalPlayer player, LivingEntity target, double range) {
        if (target == player || !target.isAlive() || target.isRemoved() || target.isSpectator()) {
            return false;
        }
        if (target.isInvisible() && !CombatSettings.targetInvisible()) {
            return false;
        }
        if (FakePlayerRegistry.isDummy(target)) {
            return CombatTargeting.distanceToBoxSqr(player, target) <= range * range;
        }
        if (target instanceof Player otherPlayer) {
            if (!CombatSettings.targetPlayers()
                    || AntiBotService.isBot(player, otherPlayer)
                    || SocialProfileStore.isProtected(player, otherPlayer)) {
                return false;
            }
        } else if (target instanceof Mob) {
            if (!CombatSettings.targetMobs()) {
                return false;
            }
        } else {
            return false;
        }
        return CombatTargeting.distanceToBoxSqr(player, target) <= range * range;
    }

    private static Comparator<LivingEntity> targetComparator(LocalPlayer player) {
        return switch (CombatSettings.pearlAimPriority()) {
            case CombatSettings.AIM_PRIORITY_DISTANCE -> Comparator
                    .comparingDouble((LivingEntity target) -> CombatTargeting.distanceToBoxSqr(player, target))
                    .thenComparingDouble(target -> aimCircleDistance(player, target));
            case CombatSettings.AIM_PRIORITY_LOW_HEALTH -> Comparator
                    .comparingDouble((LivingEntity target) -> target.getHealth() + target.getAbsorptionAmount())
                    .thenComparingDouble(target -> CombatTargeting.distanceToBoxSqr(player, target))
                    .thenComparingDouble(target -> aimCircleDistance(player, target));
            default -> Comparator
                    .comparingDouble((LivingEntity target) -> aimCircleDistance(player, target))
                    .thenComparingDouble(target -> CombatTargeting.distanceToBoxSqr(player, target));
        };
    }

    private static boolean insideAimCircle(LocalPlayer player, LivingEntity target, float radius) {
        return aimCircleDistance(player, target) <= radius;
    }

    private static float aimCircleDistance(LocalPlayer player, LivingEntity target) {
        Rotation rotation = rotationTo(player, CombatTargeting.aimPoint(player, target, !CombatSettings.throughWalls()));
        return CombatAimRules.aimCircleDistance(player.getYRot(), player.getXRot(), rotation.yaw(), rotation.pitch());
    }

    private static Rotation pearlRotationTo(LocalPlayer player, LivingEntity target) {
        Vec3 eyes = player.getEyePosition();
        Vec3 targetPoint = CombatTargeting.aimPoint(player, target, !CombatSettings.throughWalls());
        Rotation first = ballisticRotation(eyes, targetPoint, PEARL_SPEED, PEARL_GRAVITY);
        double horizontalDistance = CombatAimRules.horizontalDistance(eyes.x, eyes.z, targetPoint.x, targetPoint.z);
        double predictedTicks = CombatAimRules.projectilePredictionTicks(
                horizontalDistance,
                PEARL_SPEED,
                first != null,
                first == null ? 0.0F : first.pitch()
        );
        double prediction = CombatAimRules.predictionScale(CombatSettings.pearlAimPrediction());
        Vec3 motion = target.getDeltaMovement();
        Vec3 predictedPoint = targetPoint.add(
                motion.x * predictedTicks * prediction,
                motion.y * predictedTicks * prediction * 0.35,
                motion.z * predictedTicks * prediction
        );
        return ballisticRotation(eyes, predictedPoint, PEARL_SPEED, PEARL_GRAVITY);
    }

    private static Rotation ballisticRotation(Vec3 from, Vec3 to, float speed, float gravity) {
        return toForgeRotation(CombatAimRules.ballisticRotation(
                from.x,
                from.y,
                from.z,
                to.x,
                to.y,
                to.z,
                speed,
                gravity,
                -72.0F,
                72.0F
        ));
    }

    private static Rotation directRotation(Vec3 from, Vec3 to) {
        return toForgeRotation(CombatAimRules.rotationBetween(
                from.x,
                from.y,
                from.z,
                to.x,
                to.y,
                to.z
        ));
    }

    private static Rotation rotationTo(LocalPlayer player, Vec3 targetPoint) {
        return directRotation(player.getEyePosition(), targetPoint);
    }


    private static PearlHand findPearl(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.ENDER_PEARL)) {
            return new PearlHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.ENDER_PEARL)) {
            return new PearlHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.ENDER_PEARL)) {
                return new PearlHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static boolean holdingPearl(LocalPlayer player) {
        return player.getMainHandItem().is(Items.ENDER_PEARL) || player.getOffhandItem().is(Items.ENDER_PEARL);
    }

    private void resetAim() {
        targetId = -1;
        lastAimNanos = 0L;
        aimInitialized = false;
        hasTarget = false;
    }

    private static float smoothYawStep(float ccurrent, float target, float alpha, float maxStep) {
        return CombatAimRules.smoothYawStep(ccurrent, target, alpha, maxStep);
    }

    private static float smoothStep(float ccurrent, float target, float alpha, float maxStep) {
        return CombatAimRules.smoothStep(ccurrent, target, alpha, maxStep);
    }

    private static float aimLerpAlpha(float dt) {
        return CombatAimRules.pearlAimLerpAlpha(CombatSettings.pearlAimSmooth(), dt);
    }

    private static float aimMaxStep(float dt) {
        return CombatAimRules.pearlAimMaxStep(CombatSettings.pearlAimSmooth(), dt);
    }


    private static Rotation toForgeRotation(CombatAimRules.Rotation rotation) {
        return rotation == null ? null : new Rotation(rotation.yaw(), rotation.pitch());
    }
    private record PearlHand(InteractionHand hand, int slot) {
    }

    private record Rotation(float yaw, float pitch) {
    }
}
