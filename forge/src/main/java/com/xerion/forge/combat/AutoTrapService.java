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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class AutoTrapService {
    private static final Minecraft MC = Minecraft.getInstance();

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!CombatSettings.autoTrapEnabled() || cooldownTicks > 0) {
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            return;
        }

        TrapHand hand = findTrapHand(player);
        if (hand == null) {
            return;
        }

        LivingEntity target = findTarget(player, level);
        if (target == null) {
            return;
        }

        PlacementTarget placement = findPlacement(level, player, target);
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
            cooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.autoTrapDelayTicks(), 1);
        }

        if (CombatSettings.autoTrapRestoreSlot() && hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, previousSlot);
        }
    }

    private static LivingEntity findTarget(LocalPlayer player, ClientLevel level) {
        return CombatTargetSelector.bestPlayerOrDummyTarget(player, level, CombatSettings.autoTrapRange(), 0.25D);
    }

    private static PlacementTarget findPlacement(ClientLevel level, LocalPlayer player, LivingEntity target) {
        BlockPos base = target.blockPosition();
        for (CombatPlacementRules.ScanOffset offset : CombatPlacementRules.autoTrapPlacementOffsets(CombatSettings.autoTrapMode())) {
            PlacementTarget placement = placementFor(level, player, base.offset(offset.dx(), offset.dy(), offset.dz()));
            if (placement != null) {
                return placement;
            }
        }
        return null;
    }

    private static PlacementTarget placementFor(ClientLevel level, LocalPlayer player, BlockPos placePos) {
        return PlacementSupport.supportedPlacement(level, player, placePos, CombatSettings.autoTrapRayTrace());
    }

    private static TrapHand findTrapHand(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.OBSIDIAN)) {
            return new TrapHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.OBSIDIAN)) {
            return new TrapHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.OBSIDIAN)) {
                return new TrapHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static void rotateTo(LocalPlayer player, Vec3 point) {
        if (!CombatSettings.autoTrapRotate()) {
            return;
        }
        RotationPacketBridge.requestPacketLookAt(player, point);
    }


    private record TrapHand(InteractionHand hand, int slot) {
    }

}
