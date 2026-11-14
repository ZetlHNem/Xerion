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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AutoHealService {
    private static final Minecraft MC = Minecraft.getInstance();

    private int cooldownTicks;
    private int previousSlot = -1;
    private boolean healing;
    private boolean forcedUseKey;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        LocalPlayer player = MC.player;
        if (!CombatSettings.autoHealEnabled()) {
            reset(player, true);
            return;
        }
        if (player == null || MC.gameMode == null || MC.getConnection() == null || player.isSpectator()) {
            reset(player, true);
            return;
        }
        if (MC.screen instanceof AbstractContainerScreen<?>) {
            reset(player, true);
            return;
        }

        if (healing) {
            if (player.isUsingItem() && isAllowedHealItem(player.getUseItem())) {
                forceUseKey();
                return;
            }
            finishHealing(player);
        }

        if (cooldownTicks > 0 || !needsHeal(player) || player.isUsingItem()) {
            return;
        }

        HealHand hand = findHealItem(player);
        if (hand == null) {
            return;
        }
        startHealing(player, hand);
    }

    private void startHealing(LocalPlayer player, HealHand hand) {
        if (hand.slot() >= 0 && hand.slot() != player.getInventory().selected) {
            previousSlot = player.getInventory().selected;
            HotbarSlotActions.select(player, hand.slot());
        } else {
            previousSlot = -1;
        }

        InteractionResult result = MC.gameMode.useItem(player, hand.hand());
        if (result.consumesAction() || player.isUsingItem()) {
            healing = true;
            forceUseKey();
        } else {
            finishHealing(player);
        }
    }

    private void finishHealing(LocalPlayer player) {
        releaseUseKey();
        if (CombatSettings.autoHealRestoreSlot() && previousSlot >= 0 && player != null) {
            HotbarSlotActions.select(player, previousSlot);
        }
        previousSlot = -1;
        healing = false;
        cooldownTicks = CombatActionRules.cooldownTicks(CombatSettings.autoHealDelayTicks(), 1);
    }

    private void reset(LocalPlayer player, boolean restoreSlot) {
        releaseUseKey();
        if (restoreSlot && CombatSettings.autoHealRestoreSlot() && previousSlot >= 0 && player != null) {
            HotbarSlotActions.select(player, previousSlot);
        }
        previousSlot = -1;
        healing = false;
    }

    private static boolean needsHeal(LocalPlayer player) {
        return CombatActionRules.shouldAutoHeal(
                player.getHealth(),
                player.getAbsorptionAmount(),
                CombatSettings.autoHealIncludeAbsorption(),
                CombatSettings.autoHealHealth()
        );
    }

    private static HealHand findHealItem(LocalPlayer player) {
        if (isAllowedHealItem(player.getOffhandItem())) {
            return new HealHand(InteractionHand.OFF_HAND, -1);
        }
        if (isAllowedHealItem(player.getMainHandItem())) {
            return new HealHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            if (isAllowedHealItem(player.getInventory().getItem(slot))) {
                return new HealHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private static boolean isAllowedHealItem(ItemStack stack) {
        String mode = CombatSettings.autoHealMode();
        if (CombatSettings.AUTO_HEAL_MODE_GOLDEN.equals(mode)) {
            return stack.is(Items.GOLDEN_APPLE);
        }
        if (CombatSettings.AUTO_HEAL_MODE_ENCHANTED.equals(mode)) {
            return stack.is(Items.ENCHANTED_GOLDEN_APPLE);
        }
        return stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    private void forceUseKey() {
        MC.options.keyUse.setDown(true);
        forcedUseKey = true;
    }

    private void releaseUseKey() {
        if (!forcedUseKey) {
            return;
        }
        MC.options.keyUse.setDown(false);
        forcedUseKey = false;
    }


    private record HealHand(InteractionHand hand, int slot) {
    }
}
