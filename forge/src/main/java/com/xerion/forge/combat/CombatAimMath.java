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

import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.config.CombatSettings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;

public final class CombatAimMath {
    private static final float BOW_AIM_MIN_POWER = 0.85f;

    private CombatAimMath() {
    }

    public static boolean insideAimCircle(LocalPlayer player, LivingEntity target, float radius) {
        return aimCircleDistance(player, target) <= radius;
    }

    public static float aimCircleDistance(LocalPlayer player, LivingEntity target) {
        Rotation rotation = rotationTo(player, target);
        return CombatAimRules.aimCircleDistance(player.getYRot(), player.getXRot(), rotation.yaw(), rotation.pitch());
    }

    public static float ragebotAimCircleDistance(LocalPlayer player, LivingEntity target) {
        Rotation rotation = gunRotationTo(player, target);
        return CombatAimRules.aimCircleDistance(player.getYRot(), player.getXRot(), rotation.yaw(), rotation.pitch());
    }

    public static float yawDelta(LocalPlayer player, LivingEntity target) {
        float targetYaw = rotationTo(player, target).yaw();
        return CombatAimRules.yawDelta(player.getYRot(), targetYaw);
    }

    public static Rotation rotationTo(LocalPlayer player, LivingEntity target) {
        Vec3 eyes = player.getEyePosition();
        Vec3 targetPoint = CombatTargeting.aimPoint(player, target, !CombatSettings.throughWalls());
        return rotationBetween(eyes, targetPoint);
    }

    public static Rotation projectileRotationTo(LocalPlayer player, LivingEntity target, ProjectileInfo info) {
        Vec3 eyes = player.getEyePosition();
        Vec3 targetPoint = CombatTargeting.aimPoint(player, target, !CombatSettings.throughWalls());
        Rotation first = ballisticRotation(eyes, targetPoint, info.speed(), info.gravity());
        double horizontalDistance = CombatAimRules.horizontalDistance(eyes.x, eyes.z, targetPoint.x, targetPoint.z);
        double predictedTicks = CombatAimRules.projectilePredictionTicks(
                horizontalDistance,
                info.speed(),
                first != null,
                first == null ? 0.0F : first.pitch()
        );
        double prediction = CombatAimRules.predictionScale(CombatSettings.projectileAimPrediction());
        Vec3 motion = target.getDeltaMovement();
        Vec3 predictedPoint = targetPoint.add(motion.x * predictedTicks * prediction,
                motion.y * predictedTicks * prediction * 0.35,
                motion.z * predictedTicks * prediction);
        return ballisticRotation(eyes, predictedPoint, info.speed(), info.gravity());
    }

    public static Rotation gunRotationTo(LocalPlayer player, LivingEntity target) {
        Vec3 eyes = player.getEyePosition();
        Vec3 targetPoint = CombatTargeting.ragebotAimPoint(player, target, !CombatSettings.throughWalls());
        double distance = eyes.distanceTo(targetPoint);
        double predictedTicks = distance / 6.0D;
        double prediction = CombatAimRules.predictionScale(CombatSettings.ragebotPrediction());
        Vec3 motion = target.getDeltaMovement();
        Vec3 predictedPoint = targetPoint.add(motion.x * predictedTicks * prediction,
                motion.y * predictedTicks * prediction * 0.25D,
                motion.z * predictedTicks * prediction);
        return rotationBetween(eyes, predictedPoint);
    }

    public static ProjectileInfo projectileInfo(LocalPlayer player) {
        ItemStack useStack = player.getUseItem();
        Item useItem = useStack.getItem();
        if (player.isUsingItem() && useItem instanceof BowItem) {
            int charge = useStack.getUseDuration() - player.getUseItemRemainingTicks();
            float power = BowItem.getPowerForTime(charge);
            if (power < BOW_AIM_MIN_POWER) {
                return null;
            }
            return new ProjectileInfo(3.0f, 0.05f);
        }
        if (player.isUsingItem() && useItem instanceof TridentItem) {
            int charge = useStack.getUseDuration() - player.getUseItemRemainingTicks();
            if (charge < 10) {
                return null;
            }
            return new ProjectileInfo(2.5f, 0.05f);
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (mainHand.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(mainHand)) {
            return new ProjectileInfo(3.15f, 0.05f);
        }
        if (offHand.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(offHand)) {
            return new ProjectileInfo(3.15f, 0.05f);
        }
        return null;
    }

    public static float smoothYawStep(float current, float target, float alpha, float maxStep) {
        return CombatAimRules.smoothYawStep(current, target, alpha, maxStep);
    }

    public static float smoothStep(float current, float target, float alpha, float maxStep) {
        return CombatAimRules.smoothStep(current, target, alpha, maxStep);
    }

    public static float strengthTargetYaw(float current, float target) {
        return CombatAimRules.strengthTargetYaw(current, target, CombatSettings.aimAssistStrength());
    }

    public static float strengthTargetPitch(float current, float target) {
        return CombatAimRules.strengthTargetPitch(current, target, CombatSettings.aimAssistStrength());
    }

    public static float aimAssistLerpAlpha(float dt) {
        return CombatAimRules.aimAssistLerpAlpha(CombatSettings.aimAssistSmooth(), dt);
    }

    public static float aimAssistMaxStep(float dt) {
        return CombatAimRules.aimAssistMaxStep(CombatSettings.aimAssistSmooth(), dt);
    }

    public static float projectileAimLerpAlpha(float dt) {
        return CombatAimRules.projectileAimLerpAlpha(CombatSettings.projectileAimSmooth(), dt);
    }

    public static float projectileAimMaxStep(float dt) {
        return CombatAimRules.projectileAimMaxStep(CombatSettings.projectileAimSmooth(), dt);
    }

    public static float gunAimLerpAlpha(float dt) {
        return CombatAimRules.timedLerpAlpha(CombatSettings.ragebotSmooth(), dt, 78.0F, 4.8F);
    }

    public static float gunAimMaxStep(float dt) {
        return CombatAimRules.timedMaxStep(CombatSettings.ragebotSmooth(), dt, 3200.0F, 150.0F);
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
                gravity
        ));
    }

    private static Rotation rotationBetween(Vec3 from, Vec3 to) {
        return toForgeRotation(CombatAimRules.rotationBetween(
                from.x,
                from.y,
                from.z,
                to.x,
                to.y,
                to.z
        ));
    }

    private static Rotation toForgeRotation(CombatAimRules.Rotation rotation) {
        return rotation == null ? null : new Rotation(rotation.yaw(), rotation.pitch());
    }

    public record ProjectileInfo(float speed, float gravity) {
    }

    public record Rotation(float yaw, float pitch) {
    }
}
