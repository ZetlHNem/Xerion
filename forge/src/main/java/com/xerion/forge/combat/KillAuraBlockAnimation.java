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

import com.xerion.common.combat.CombatActionRules;
import com.xerion.common.config.CombatSettings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KillAuraBlockAnimation {
    private static final Logger LOGGER = LoggerFactory.getLogger(XerionForge.MOD_ID);
    private static final long ATTACK_ANIMATION_NANOS = 360_000_000L;
    private static volatile boolean loggedHook;
    private static volatile boolean loggedApply;
    private static volatile long lastAttackNanos;

    private KillAuraBlockAnimation() {
    }

    public static void markAttack() {
        lastAttackNanos = System.nanoTime();
    }

    public static void renderHook(InteractionHand hand, ItemStack stack) {
        if (loggedHook) {
            return;
        }
        loggedHook = true;
        LOGGER.info("[Xerion][KillAura] hand renderer mixin called hand={} item={} enabled={} mode={} targetActive={}",
                hand,
                stack == null || stack.isEmpty() ? "empty" : stack.getItem().toString(),
                CombatSettings.killAuraEnabled(),
                CombatSettings.killAuraBlockAnimation(),
                KillAuraService.visualBlockTargetActive());
    }

    public static boolean shouldApply(InteractionHand hand, ItemStack stack) {
        if (hand != InteractionHand.MAIN_HAND || stack == null || stack.isEmpty()) {
            return false;
        }
        boolean fakeAutoblock = fakeAutoblockActive();
        if (!CombatActionRules.blockAnimationEnabled(
                CombatSettings.killAuraEnabled(),
                CombatSettings.killAuraBlockAnimation())) {
            if (!fakeAutoblock) {
                return false;
            }
        }
        if (!attackAnimationActive() && !fakeAutoblock) {
            return false;
        }
        if (Minecraft.getInstance().player == null) {
            return false;
        }
        Item item = stack.getItem();
        if (fakeAutoblock) {
            return item instanceof SwordItem;
        }
        return item instanceof SwordItem || item instanceof AxeItem;
    }

    private static boolean attackAnimationActive() {
        return CombatActionRules.timedAnimationActive(System.nanoTime(), lastAttackNanos, ATTACK_ANIMATION_NANOS);
    }

    private static boolean fakeAutoblockActive() {
        return CombatSettings.AUTOBLOCK_FAKE.equals(CombatSettings.autoblockMode())
                && KillAuraService.visualBlockTargetActive()
                && CombatSettings.killAuraEnabled();
    }

    public static boolean shouldApplyThirdPerson(AbstractClientPlayer player, HumanoidArm arm) {
        Minecraft minecraft = Minecraft.getInstance();
        return player == minecraft.player
                && player.getMainArm() == arm
                && shouldApply(InteractionHand.MAIN_HAND, player.getMainHandItem());
    }

    public static void apply(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        logAppliedOnce();
        String mode = CombatSettings.killAuraBlockAnimation();
        if (fakeAutoblockActive() && CombatSettings.BLOCK_ANIMATION_OFF.equals(mode)) {
            mode = CombatSettings.BLOCK_ANIMATION_1_7;
        }
        float attack = Mth.clamp(swingProgress, 0.0f, 1.0f);
        if (CombatSettings.BLOCK_ANIMATION_1_7.equals(mode)) {
            poseStack.translate(arm == HumanoidArm.RIGHT ? -0.12F : 0.12F, 0.66F, -0.04F);
            applyAttackTransform(poseStack, arm, attack, 0.90F);
            applyBlockPose(poseStack, arm, 0.0F, 0.0F, 0.0F, -102.25F, 13.365F, 78.05F);
        } else if (CombatSettings.BLOCK_ANIMATION_PUSHDOWN.equals(mode)) {
            float swing = Mth.sin(Mth.sqrt(attack) * Mth.PI);
            poseStack.translate(arm == HumanoidArm.RIGHT ? -0.12F : 0.12F, 0.70F, -0.04F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(armSign(arm) * swing * 10.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(swing * -35.0F));
            applyBlockPose(poseStack, arm, 0.0F, 0.0F, 0.0F, -102.25F, 13.365F, 78.05F);
        } else if (CombatSettings.BLOCK_ANIMATION_SCALE.equals(mode)) {
            poseStack.translate(arm == HumanoidArm.RIGHT ? -0.18F : 0.18F, 0.66F, 0.10F);
            poseStack.scale(0.85F, 0.85F, 0.85F);
            applyAttackTransform(poseStack, arm, attack, 0.35F);
            applyBlockPose(poseStack, arm, 0.0F, 0.0F, 0.0F, -100.0F, 18.0F, 72.0F);
        } else if (CombatSettings.BLOCK_ANIMATION_LEAKED.equals(mode)) {
            applyAttackTransform(poseStack, arm, attack, 0.60F);
            applyBlockPose(poseStack, arm, -0.18F, 0.68F, 0.06F, -96.0F, 24.0F, 68.0F);
        } else if (CombatSettings.BLOCK_ANIMATION_NINJA.equals(mode)) {
            applyAttackTransform(poseStack, arm, attack, 0.25F);
            applyBlockPose(poseStack, arm, -0.05F, 0.70F, 0.14F, -88.0F, 35.0F, 82.0F);
        } else if (CombatSettings.BLOCK_ANIMATION_DOWN.equals(mode)) {
            applyAttackTransform(poseStack, arm, attack, 0.20F);
            applyBlockPose(poseStack, arm, -0.25F, 0.50F, 0.02F, -120.0F, 8.0F, 72.0F);
        }
    }

    private static void logAppliedOnce() {
        if (loggedApply) {
            return;
        }
        loggedApply = true;
        LOGGER.info("[Xerion][KillAura] block animation mixin active mode={}",
                CombatSettings.killAuraBlockAnimation());
    }

    private static void applyAttackTransform(PoseStack poseStack, HumanoidArm arm, float attack, float scale) {
        int sign = armSign(arm);
        float ySwingRotation = Mth.sin(attack * attack * Mth.PI);
        poseStack.mulPose(Axis.YP.rotationDegrees(sign * (45.0F + ySwingRotation * -20.0F * scale)));
        float swingRotation = Mth.sin(Mth.sqrt(attack) * Mth.PI);
        poseStack.mulPose(Axis.ZP.rotationDegrees(sign * swingRotation * -20.0F * scale));
        poseStack.mulPose(Axis.XP.rotationDegrees(swingRotation * -80.0F * scale));
        poseStack.mulPose(Axis.YP.rotationDegrees(sign * -45.0F));
    }

    private static void applyVisibleAttackTransform(PoseStack poseStack, HumanoidArm arm, float attack, float scale) {
        int sign = armSign(arm);
        float swing = Mth.sin(Mth.sqrt(attack) * Mth.PI);
        float ySwing = Mth.sin(attack * attack * Mth.PI);
        poseStack.translate(-sign * 0.04F * swing * scale, swing * 0.02F * scale, -0.03F * swing * scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(sign * ySwing * -12.0F * scale));
        poseStack.mulPose(Axis.ZP.rotationDegrees(sign * swing * -10.0F * scale));
        poseStack.mulPose(Axis.XP.rotationDegrees(swing * -18.0F * scale));
    }

    private static void applyVisibleBlockPose(PoseStack poseStack, HumanoidArm arm,
                                              float translateX, float translateY, float translateZ,
                                              float rotateX, float rotateY, float rotateZ) {
        int sign = armSign(arm);
        poseStack.translate(sign * translateX, translateY, translateZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotateX));
        poseStack.mulPose(Axis.YP.rotationDegrees(sign * rotateY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(sign * rotateZ));
    }

    private static void applyBlockPose(PoseStack poseStack, HumanoidArm arm,
                                       float translateX, float translateY, float translateZ,
                                       float rotateX, float rotateY, float rotateZ) {
        int sign = armSign(arm);
        poseStack.translate(sign * translateX, translateY, translateZ);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotateX));
        poseStack.mulPose(Axis.YP.rotationDegrees(sign * rotateY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(sign * rotateZ));
    }

    private static int armSign(HumanoidArm arm) {
        return CombatActionRules.armSign(arm == HumanoidArm.RIGHT);
    }
}
