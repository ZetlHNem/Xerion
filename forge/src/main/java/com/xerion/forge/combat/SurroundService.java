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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class SurroundService {
    private static final Minecraft MC = Minecraft.getInstance();

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!CombatSettings.surroundEnabled() || cooldownTicks > 0) {
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            return;
        }

        SurroundHand hand = findSurroundHand(player);
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
            cooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.surroundDelayTicks(), 1);
        }

        if (CombatSettings.surroundRestoreSlot() && hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
    }

    private static PlacementTarget findPlacement(ClientLevel level, LocalPlayer player) {
        BlockPos base = player.blockPosition();
        for (CombatPlacementRules.ScanOffset offset : CombatPlacementRules.surroundPlacementOffsets(
                CombatSettings.surroundFloor(),
                CombatSettings.surroundMode())) {
            PlacementTarget placement = placementFor(level, player, base.offset(offset.dx(), offset.dy(), offset.dz()));
            if (placement != null) {
                return placement;
            }
        }
        return null;
    }

    private static PlacementTarget placementFor(ClientLevel level, LocalPlayer player, BlockPos placePos) {
        return PlacementSupport.supportedPlacement(level, player, placePos, CombatSettings.surroundRayTrace());
    }

    private static SurroundHand findSurroundHand(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.OBSIDIAN)) {
            return new SurroundHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.OBSIDIAN)) {
            return new SurroundHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.OBSIDIAN)) {
                return new SurroundHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static void rotateTo(LocalPlayer player, Vec3 point) {
        if (!CombatSettings.surroundRotate()) {
            return;
        }
        RotationPacketBridge.requestPacketLookAt(player, point);
    }


    private record SurroundHand(InteractionHand hand, int slot) {
    }

}
