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

import com.xerion.common.combat.CombatExplosionRules;
import com.xerion.common.combat.CombatActionRules;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AnchorAuraService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final int SCAN_HORIZONTAL = 4;
    private static final int SCAN_DOWN = 2;
    private static final int SCAN_UP = 2;
    private static final int FAIL_COOLDOWN_TICKS = 6;

    private int placeCooldownTicks;
    private int breakCooldownTicks;

    public void tick() {
        if (placeCooldownTicks > 0) {
            placeCooldownTicks--;
        }
        if (breakCooldownTicks > 0) {
            breakCooldownTicks--;
        }
        if (!CombatSettings.anchorAuraEnabled()) {
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            return;
        }
        if (level.dimensionType().respawnAnchorWorks()) {
            return;
        }

        boolean canBreak = CombatSettings.anchorAuraBreak() && breakCooldownTicks <= 0;
        boolean canPlace = CombatSettings.anchorAuraPlace() && placeCooldownTicks <= 0;
        if (!canBreak && !canPlace) {
            return;
        }

        LivingEntity target = findTarget(player, level);
        if (target == null) {
            applyFailCooldown();
            return;
        }

        AnchorTarget anchor = findBestAnchor(player, level, target);
        if (canBreak && anchor != null && anchor.charge() > 0 && detonateAnchor(player, anchor.pos(), anchor.hit())) {
            breakCooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.anchorAuraBreakDelayTicks(), 0);
            return;
        }
        if (canPlace && anchor != null && anchor.charge() <= 0 && chargeAnchor(player, anchor.pos(), anchor.hit())) {
            placeCooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.anchorAuraPlaceDelayTicks(), 0);
            return;
        }
        if (canPlace && placeAnchor(player, level, target)) {
            placeCooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.anchorAuraPlaceDelayTicks(), 0);
        }
    }

    private boolean detonateAnchor(LocalPlayer player, BlockPos pos, Vec3 hitVec) {
        UseHand hand = findDetonateHand(player);
        if (hand == null) {
            return false;
        }
        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }
        rotateTo(player, hitVec);
        InteractionResult result = MC.gameMode.useItemOn(player, hand.hand(),
                new BlockHitResult(hitVec, Direction.UP, pos, false));
        if (result.consumesAction()) {
            player.swing(hand.hand());
        }
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
        return result.consumesAction();
    }

    private boolean chargeAnchor(LocalPlayer player, BlockPos pos, Vec3 hitVec) {
        UseHand hand = findGlowstoneHand(player);
        if (hand == null) {
            return false;
        }
        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }
        rotateTo(player, hitVec);
        InteractionResult result = MC.gameMode.useItemOn(player, hand.hand(),
                new BlockHitResult(hitVec, Direction.UP, pos, false));
        if (result.consumesAction()) {
            player.swing(hand.hand());
        }
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
        return result.consumesAction();
    }

    private boolean placeAnchor(LocalPlayer player, ClientLevel level, LivingEntity target) {
        UseHand hand = findAnchorHand(player);
        if (hand == null) {
            applyFailCooldown();
            return false;
        }

        AnchorPlacement placement = findBestPlacement(player, level, target);
        if (placement == null) {
            applyFailCooldown();
            return false;
        }

        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }
        rotateTo(player, placement.look());
        InteractionResult result = MC.gameMode.useItemOn(player, hand.hand(),
                new BlockHitResult(placement.hit(), Direction.UP, placement.base(), false));
        if (result.consumesAction()) {
            player.swing(hand.hand());
        }
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
        return result.consumesAction();
    }

    private AnchorTarget findBestAnchor(LocalPlayer player, ClientLevel level, LivingEntity target) {
        double range = CombatSettings.anchorAuraRange();
        double rangeSq = range * range;
        AnchorTarget best = null;
        BlockPos targetPos = target.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -SCAN_DOWN; dy <= SCAN_UP; dy++) {
            for (int dx = -SCAN_HORIZONTAL; dx <= SCAN_HORIZONTAL; dx++) {
                for (int dz = -SCAN_HORIZONTAL; dz <= SCAN_HORIZONTAL; dz++) {
                    pos.set(targetPos.getX() + dx, targetPos.getY() + dy, targetPos.getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.RESPAWN_ANCHOR)) {
                        continue;
                    }
                    Vec3 hit = Vec3.atCenterOf(pos);
                    if (player.distanceToSqr(hit) > rangeSq || !visible(player, level, hit) || !validDamage(player, target, hit)) {
                        continue;
                    }
                    int charge = state.getValue(RespawnAnchorBlock.CHARGE);
                    double damage = estimatedDamage(target, hit);
                    double selfDamage = estimatedDamage(player, hit);
                    double score = CombatExplosionRules.anchorBreakScore(damage, selfDamage, player.distanceToSqr(hit));
                    if (charge > 0) {
                        score += 8.0D;
                    }
                    if (best == null || score > best.score()) {
                        best = new AnchorTarget(pos.immutable(), hit, charge, score);
                    }
                }
            }
        }
        return best;
    }

    private AnchorPlacement findBestPlacement(LocalPlayer player, ClientLevel level, LivingEntity target) {
        double range = CombatSettings.anchorAuraRange();
        double rangeSq = range * range;
        AnchorPlacement best = null;
        BlockPos targetPos = target.blockPosition();
        BlockPos.MutableBlockPos anchor = new BlockPos.MutableBlockPos();
        for (int dy = -SCAN_DOWN; dy <= SCAN_UP; dy++) {
            for (int dx = -SCAN_HORIZONTAL; dx <= SCAN_HORIZONTAL; dx++) {
                for (int dz = -SCAN_HORIZONTAL; dz <= SCAN_HORIZONTAL; dz++) {
                    anchor.set(targetPos.getX() + dx, targetPos.getY() + dy, targetPos.getZ() + dz);
                    BlockPos base = anchor.below();
                    Vec3 hit = WorldGeometry.topCenter(base);
                    Vec3 explosion = Vec3.atCenterOf(anchor);
                    if (player.distanceToSqr(hit) > rangeSq
                            || !level.isEmptyBlock(anchor)
                            || !level.getBlockState(base).isFaceSturdy(level, base, Direction.UP)
                            || !visible(player, level, hit)
                            || !validDamage(player, target, explosion)) {
                        continue;
                    }
                    double damage = estimatedDamage(target, explosion);
                    double selfDamage = estimatedDamage(player, explosion);
                    double score = CombatExplosionRules.anchorPlaceScore(damage, selfDamage, player.distanceToSqr(hit));
                    if (best == null || score > best.score()) {
                        best = new AnchorPlacement(anchor.immutable(), base, hit, explosion, score);
                    }
                }
            }
        }
        return best;
    }

    private LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        return CombatTargetSelector.bestPlayerOrDummyTarget(player, level, CombatSettings.anchorAuraRange(), 0.75D);
    }

    private static boolean validDamage(LocalPlayer player, LivingEntity target, Vec3 explosion) {
        double targetDamage = estimatedDamage(target, explosion);
        double selfDamage = estimatedDamage(player, explosion);
        double selfHealth = player.getHealth() + player.getAbsorptionAmount();
        return CombatExplosionRules.validExplosionDamage(
                targetDamage,
                selfDamage,
                selfHealth,
                CombatSettings.anchorAuraMinDamage(),
                CombatSettings.anchorAuraMaxSelfDamage(),
                CombatSettings.anchorAuraAntiSuicide()
        );
    }

    private static double estimatedDamage(LivingEntity entity, Vec3 explosion) {
        double distance = Math.sqrt(entity.getBoundingBox().getCenter().distanceToSqr(explosion));
        return CombatExplosionRules.estimatedDamage(
                distance,
                CombatExplosionRules.ANCHOR_EXPLOSION_RADIUS,
                15.0D,
                entity.getArmorValue()
        );
    }

    private static boolean visible(LocalPlayer player, ClientLevel level, Vec3 point) {
        if (!CombatSettings.anchorAuraRayTrace()) {
            return true;
        }
        return WorldGeometry.visible(player, level, point);
    }

    private static UseHand findAnchorHand(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.RESPAWN_ANCHOR)) {
            return new UseHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.RESPAWN_ANCHOR)) {
            return new UseHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).is(Items.RESPAWN_ANCHOR)) {
                return new UseHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static UseHand findGlowstoneHand(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.GLOWSTONE)) {
            return new UseHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.GLOWSTONE)) {
            return new UseHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).is(Items.GLOWSTONE)) {
                return new UseHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static UseHand findDetonateHand(LocalPlayer player) {
        if (!player.getOffhandItem().is(Items.GLOWSTONE)) {
            return new UseHand(InteractionHand.OFF_HAND, -1);
        }
        if (!player.getMainHandItem().is(Items.GLOWSTONE)) {
            return new UseHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(Items.GLOWSTONE)) {
                return new UseHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static void rotateTo(LocalPlayer player, Vec3 point) {
        if (!CombatSettings.anchorAuraRotate()) {
            return;
        }
        RotationPacketBridge.requestPacketLookAt(player, point);
    }

    private void applyFailCooldown() {
        placeCooldownTicks = CombatActionRules.raiseCooldown(placeCooldownTicks, FAIL_COOLDOWN_TICKS);
        breakCooldownTicks = CombatActionRules.raiseCooldown(breakCooldownTicks, FAIL_COOLDOWN_TICKS);
    }

    private record UseHand(InteractionHand hand, int slot) {
    }

    private record AnchorTarget(BlockPos pos, Vec3 hit, int charge, double score) {
    }

    private record AnchorPlacement(BlockPos pos, BlockPos base, Vec3 hit, Vec3 look, double score) {
    }

}
