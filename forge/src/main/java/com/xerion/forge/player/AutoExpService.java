package com.xerion.forge.player;

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
import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.config.CombatSettings;
import com.xerion.common.player.PlayerItemRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class AutoExpService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!CombatSettings.autoExpEnabled() || cooldownTicks > 0
                || BypassProtocolService.correctionRecoveryActive()) {
            return;
        }

        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || MC.getConnection() == null || MC.screen != null
                || player.isSpectator() || player.isUsingItem()) {
            return;
        }
        if (!needsRepair(player)) {
            return;
        }

        ExpHand hand = findExperienceBottle(player);
        if (hand == null) {
            return;
        }

        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }

        InteractionResult result = useBottleDown(player, hand.hand());
        if (result.consumesAction()) {
            player.swing(hand.hand());
            cooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.autoExpDelayTicks(), 1);
        }

        if (PlayerItemRules.autoExpShouldRestoreSlot(
                CombatSettings.autoExpRestoreSlot(),
                hand.slot(),
                previousSlot)) {
            HotbarSlotActions.select(player, previousSlot);
        }
    }

    private static boolean needsRepair(LocalPlayer player) {
        String mode = CombatSettings.autoExpMode();
        if (PlayerItemRules.autoExpChecksArmor(mode)) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                if (needsRepair(player.getItemBySlot(slot))) {
                    return true;
                }
            }
        }
        if (PlayerItemRules.autoExpChecksHeld(mode)) {
            return needsRepair(player.getMainHandItem()) || needsRepair(player.getOffhandItem());
        }
        return false;
    }

    private static boolean needsRepair(ItemStack stack) {
        return PlayerItemRules.needsRepair(
                stack.isEmpty(),
                stack.isDamageableItem(),
                stack.getDamageValue(),
                CombatSettings.autoExpMendingOnly(),
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stack),
                stack.getMaxDamage(),
                CombatSettings.autoExpDurability()
        );
    }

    private static ExpHand findExperienceBottle(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE)) {
            return new ExpHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)) {
            return new ExpHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).is(Items.EXPERIENCE_BOTTLE)) {
                return new ExpHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static InteractionResult useBottleDown(LocalPlayer player, InteractionHand hand) {
        if (MC.gameMode == null) {
            return InteractionResult.PASS;
        }

        if (CombatSettings.autoExpSilentRotate()) {
            float throwPitch = PlayerItemRules.autoExpThrowPitch();
            float previousPitch = player.getXRot();
            player.setXRot(throwPitch);
            if (MC.getConnection() != null) {
                MC.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                        player.getYRot(),
                        throwPitch,
                        player.onGround()
                ));
            }
            try {
                return MC.gameMode.useItem(player, hand);
            } finally {
                player.setXRot(previousPitch);
                if (MC.getConnection() != null) {
                    MC.getConnection().send(new ServerboundMovePlayerPacket.Rot(
                            player.getYRot(),
                            previousPitch,
                            player.onGround()
                    ));
                }
            }
        }
        player.setXRot(CombatAimRules.clamp(PlayerItemRules.autoExpThrowPitch(), -90.0f, 90.0f));
        return MC.gameMode.useItem(player, hand);
    }


    private record ExpHand(InteractionHand hand, int slot) {
    }
}
