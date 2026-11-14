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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class BedAuraService {
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
        if (!CombatSettings.bedAuraEnabled()) {
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            return;
        }
        if (level.dimensionType().bedWorks()) {
            return;
        }

        boolean canBreak = CombatSettings.bedAuraBreak() && breakCooldownTicks <= 0;
        boolean canPlace = CombatSettings.bedAuraPlace() && placeCooldownTicks <= 0;
        if (!canBreak && !canPlace) {
            return;
        }

        LivingEntity target = findTarget(player, level);
        if (target == null) {
            applyFailCooldown();
            return;
        }

        if (canBreak && breakBed(player, level, target)) {
            breakCooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.bedAuraBreakDelayTicks(), 0);
            return;
        }
        if (canPlace && placeBed(player, level, target)) {
            placeCooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.bedAuraPlaceDelayTicks(), 0);
        }
    }

    private boolean breakBed(LocalPlayer player, ClientLevel level, LivingEntity target) {
        double range = CombatSettings.bedAuraRange();
        double rangeSq = range * range;
        BedTarget best = null;
        BlockPos targetPos = target.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -SCAN_DOWN; dy <= SCAN_UP; dy++) {
            for (int dx = -SCAN_HORIZONTAL; dx <= SCAN_HORIZONTAL; dx++) {
                for (int dz = -SCAN_HORIZONTAL; dz <= SCAN_HORIZONTAL; dz++) {
                    pos.set(targetPos.getX() + dx, targetPos.getY() + dy, targetPos.getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof BedBlock) || player.distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) {
                        continue;
                    }
                    BlockPos foot = bedFoot(pos.immutable(), state);
                    Vec3 explosion = bedExplosionPoint(foot, state.getValue(BedBlock.FACING));
                    if (!visible(player, level, explosion) || !validDamage(player, target, explosion)) {
                        continue;
                    }
                    double damage = estimatedDamage(target, explosion);
                    double selfDamage = estimatedDamage(player, explosion);
                    double score = CombatExplosionRules.bedBreakScore(damage, selfDamage, player.distanceToSqr(explosion));
                    if (best == null || score > best.score()) {
                        best = new BedTarget(pos.immutable(), explosion, score);
                    }
                }
            }
        }
        if (best == null) {
            return false;
        }

        rotateTo(player, best.hit());
        InteractionResult result = MC.gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(best.hit(), Direction.UP, best.pos(), false));
        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
        return result.consumesAction();
    }

    private boolean placeBed(LocalPlayer player, ClientLevel level, LivingEntity target) {
        BedHand hand = findBedHand(player);
        if (hand == null) {
            applyFailCooldown();
            return false;
        }

        BedPlacement placement = findBestPlacement(player, level, target);
        if (placement == null) {
            applyFailCooldown();
            return false;
        }

        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }

        rotateTo(player, placement.look());
        BlockHitResult hit = new BlockHitResult(placement.hit(), Direction.UP, placement.base(), false);
        InteractionResult result = MC.gameMode.useItemOn(player, hand.hand(), hit);
        if (result.consumesAction()) {
            player.swing(hand.hand());
        }

        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
        return result.consumesAction();
    }

    private BedPlacement findBestPlacement(LocalPlayer player, ClientLevel level, LivingEntity target) {
        double range = CombatSettings.bedAuraRange();
        double rangeSq = range * range;
        BedPlacement best = null;
        BlockPos targetPos = target.blockPosition();
        BlockPos.MutableBlockPos foot = new BlockPos.MutableBlockPos();
        for (int dy = -SCAN_DOWN; dy <= SCAN_UP; dy++) {
            for (int dx = -SCAN_HORIZONTAL; dx <= SCAN_HORIZONTAL; dx++) {
                for (int dz = -SCAN_HORIZONTAL; dz <= SCAN_HORIZONTAL; dz++) {
                    foot.set(targetPos.getX() + dx, targetPos.getY() + dy, targetPos.getZ() + dz);
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos head = foot.relative(direction);
                        BlockPos base = foot.below();
                        Vec3 hit = WorldGeometry.topCenter(base);
                        if (player.distanceToSqr(hit) > rangeSq || !canPlaceBed(level, foot, head)) {
                            continue;
                        }
                        Vec3 explosion = bedExplosionPoint(foot, direction);
                        if (!visible(player, level, hit) || !validDamage(player, target, explosion)) {
                            continue;
                        }
                        double damage = estimatedDamage(target, explosion);
                        double selfDamage = estimatedDamage(player, explosion);
                        double score = CombatExplosionRules.bedPlaceScore(damage, selfDamage, player.distanceToSqr(hit));
                        if (best == null || score > best.score()) {
                            best = new BedPlacement(foot.immutable(), base, hit, explosion, direction, score);
                        }
                    }
                }
            }
        }
        return best;
    }

    private static boolean canPlaceBed(ClientLevel level, BlockPos foot, BlockPos head) {
        return level.isEmptyBlock(foot)
                && level.isEmptyBlock(head)
                && sturdyTop(level, foot.below())
                && sturdyTop(level, head.below());
    }

    private static boolean sturdyTop(ClientLevel level, BlockPos pos) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP);
    }

    private LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        return CombatTargetSelector.bestPlayerOrDummyTarget(player, level, CombatSettings.bedAuraRange(), 0.75D);
    }

    private static boolean validDamage(LocalPlayer player, LivingEntity target, Vec3 explosion) {
        double targetDamage = estimatedDamage(target, explosion);
        double selfDamage = estimatedDamage(player, explosion);
        double selfHealth = player.getHealth() + player.getAbsorptionAmount();
        return CombatExplosionRules.validExplosionDamage(
                targetDamage,
                selfDamage,
                selfHealth,
                CombatSettings.bedAuraMinDamage(),
                CombatSettings.bedAuraMaxSelfDamage(),
                CombatSettings.bedAuraAntiSuicide()
        );
    }

    private static double estimatedDamage(LivingEntity entity, Vec3 explosion) {
        double distance = Math.sqrt(entity.getBoundingBox().getCenter().distanceToSqr(explosion));
        return CombatExplosionRules.estimatedDamage(
                distance,
                CombatExplosionRules.BED_EXPLOSION_RADIUS,
                15.0D,
                entity.getArmorValue()
        );
    }

    private static boolean visible(LocalPlayer player, ClientLevel level, Vec3 point) {
        if (!CombatSettings.bedAuraRayTrace()) {
            return true;
        }
        return WorldGeometry.visible(player, level, point);
    }

    private static BlockPos bedFoot(BlockPos pos, BlockState state) {
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos;
        }
        return pos.relative(state.getValue(BedBlock.FACING).getOpposite());
    }

    private static Vec3 bedExplosionPoint(BlockPos foot, Direction direction) {
        return Vec3.atCenterOf(foot).add(direction.getStepX() * 0.5D, 0.0D, direction.getStepZ() * 0.5D);
    }

    private static BedHand findBedHand(LocalPlayer player) {
        if (player.getOffhandItem().getItem() instanceof BedItem) {
            return new BedHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().getItem() instanceof BedItem) {
            return new BedHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof BedItem) {
                return new BedHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static void rotateTo(LocalPlayer player, Vec3 point) {
        if (!CombatSettings.bedAuraRotate()) {
            return;
        }
        RotationPacketBridge.requestPacketLookAt(player, point);
    }


    private void applyFailCooldown() {
        placeCooldownTicks = CombatActionRules.raiseCooldown(placeCooldownTicks, FAIL_COOLDOWN_TICKS);
        breakCooldownTicks = CombatActionRules.raiseCooldown(breakCooldownTicks, FAIL_COOLDOWN_TICKS);
    }

    private record BedHand(InteractionHand hand, int slot) {
    }

    private record BedTarget(BlockPos pos, Vec3 hit, double score) {
    }

    private record BedPlacement(BlockPos foot, BlockPos base, Vec3 hit, Vec3 look, Direction direction, double score) {
    }

}
