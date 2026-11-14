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
import com.xerion.common.combat.CombatPlacementRules;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class HoleFillService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Direction[] HOLE_SIDES = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!CombatSettings.holeFillEnabled() || cooldownTicks > 0) {
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            return;
        }

        FillHand hand = findFillHand(player);
        if (hand == null) {
            return;
        }

        PlacementTarget placement = findPlacement(level, player);
        if (placement == null) {
            return;
        }

        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }

        rotateTo(player, placement.hit());
        InteractionResult result = MC.gameMode.useItemOn(player, hand.hand(),
                new BlockHitResult(placement.hit(), placement.face(), placement.clicked(), false));
        if (result.consumesAction()) {
            player.swing(hand.hand());
            cooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.holeFillDelayTicks(), 1);
        }

        if (CombatSettings.holeFillRestoreSlot() && hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
    }

    private static PlacementTarget findPlacement(ClientLevel level, LocalPlayer player) {
        String mode = CombatSettings.holeFillMode();
        LivingEntity target = CombatSettings.HOLE_FILL_MODE_SELF.equals(mode) ? null : findTarget(player, level);
        if (CombatSettings.HOLE_FILL_MODE_TARGET.equals(mode) && target == null) {
            return null;
        }

        double range = CombatSettings.holeFillRange();
        double rangeSq = range * range;
        int radius = CombatPlacementRules.scanRadius(range);
        BlockPos base = player.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        PlacementTarget best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        for (int dy = -2; dy <= 1; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    mutable.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);
                    Vec3 center = Vec3.atCenterOf(mutable);
                    if (player.distanceToSqr(center) > rangeSq || !isHole(level, mutable)) {
                        continue;
                    }

                    PlacementTarget placement = placementFor(level, player, mutable);
                    if (placement == null) {
                        continue;
                    }

                    double score = scoreHole(player, target, center, mode);
                    if (score < bestScore) {
                        bestScore = score;
                        best = placement;
                    }
                }
            }
        }
        return best;
    }

    private static double scoreHole(LocalPlayer player, LivingEntity target, Vec3 center, String mode) {
        return CombatPlacementRules.holeFillScore(
                target != null,
                CombatSettings.HOLE_FILL_MODE_SELF.equals(mode),
                target == null ? 0.0D : target.distanceToSqr(center),
                player.distanceToSqr(center)
        );
    }

    private static boolean isHole(ClientLevel level, BlockPos pos) {
        if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) {
            return false;
        }
        if (!level.noCollision(new AABB(pos)) || !level.noCollision(new AABB(pos.above()))) {
            return false;
        }
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        for (Direction side : HOLE_SIDES) {
            BlockPos wall = pos.relative(side);
            Direction face = side.getOpposite();
            if (!level.getBlockState(wall).isFaceSturdy(level, wall, face)) {
                return false;
            }
        }
        return true;
    }

    private static PlacementTarget placementFor(ClientLevel level, LocalPlayer player, BlockPos placePos) {
        return PlacementSupport.supportedPlacement(level, player, placePos, CombatSettings.holeFillRayTrace());
    }

    private static LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        return CombatTargetSelector.bestPlayerOrDummyTarget(player, level, CombatSettings.holeFillRange(), 0.25D);
    }

    private static FillHand findFillHand(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.OBSIDIAN)) {
            return new FillHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.OBSIDIAN)) {
            return new FillHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.OBSIDIAN)) {
                return new FillHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static void rotateTo(LocalPlayer player, Vec3 point) {
        if (!CombatSettings.holeFillRotate()) {
            return;
        }
        RotationPacketBridge.requestPacketLookAt(player, point);
    }


    private record FillHand(InteractionHand hand, int slot) {
    }

}
