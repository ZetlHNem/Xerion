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

import com.xerion.forge.config.VisualSettings;
import com.xerion.common.player.PlayerItemRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;

public final class AutoEatService {
    private static final Minecraft MC = Minecraft.getInstance();

    private int cooldownTicks;
    private int previousSlot = -1;
    private int swappedInventorySlot = -1;
    private int swappedHotbarSlot = -1;
    private boolean eating;
    private boolean forcedUseKey;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        LocalPlayer player = MC.player;
        if (!VisualSettings.autoEatEnabled()) {
            reset(player, true);
            return;
        }
        if (player == null || MC.gameMode == null || MC.getConnection() == null || player.isSpectator()
                || player.isCreative()) {
            reset(player, true);
            return;
        }
        if (MC.screen instanceof AbstractContainerScreen<?>) {
            reset(player, true);
            return;
        }

        if (eating) {
            if (player.isUsingItem() && isAllowedFood(player.getUseItem(), player)) {
                forceUseKey();
                return;
            }
            finishEating(player);
        }

        if (cooldownTicks > 0 || !needsFood(player) || player.isUsingItem()) {
            return;
        }

        EatHand hand = findFood(player);
        if (hand == null) {
            return;
        }
        startEating(player, hand);
    }

    private void startEating(LocalPlayer player, EatHand hand) {
        if (hand.slot() >= 0 && hand.slot() != player.getInventory().selected) {
            previousSlot = player.getInventory().selected;
            HotbarSlotActions.select(player, hand.slot());
        } else {
            previousSlot = -1;
        }

        if (hand.swapInventorySlot() >= 0) {
            swapInventoryFoodToHotbar(player, hand.swapInventorySlot());
        }

        InteractionResult result = MC.gameMode.useItem(player, hand.hand());
        if (result.consumesAction() || player.isUsingItem()) {
            eating = true;
            forceUseKey();
        } else {
            finishEating(player);
        }
    }

    private void finishEating(LocalPlayer player) {
        releaseUseKey();
        if (VisualSettings.autoEatRestoreSlot() && previousSlot >= 0 && player != null) {
            HotbarSlotActions.select(player, previousSlot);
        }
        restoreInventorySwap(player);
        previousSlot = -1;
        eating = false;
        cooldownTicks = PlayerItemRules.positiveDelayTicks(VisualSettings.autoEatDelayTicks());
    }

    private void reset(LocalPlayer player, boolean restoreSlot) {
        releaseUseKey();
        if (restoreSlot && VisualSettings.autoEatRestoreSlot() && previousSlot >= 0 && player != null) {
            HotbarSlotActions.select(player, previousSlot);
        }
        restoreInventorySwap(player);
        previousSlot = -1;
        eating = false;
    }

    private static boolean needsFood(LocalPlayer player) {
        return PlayerItemRules.needsFood(
                player.getFoodData().getFoodLevel(),
                player.getFoodData().needsFood(),
                VisualSettings.autoEatFoodLevel()
        );
    }

    private EatHand findFood(LocalPlayer player) {
        if (isAllowedFood(player.getOffhandItem(), player)) {
            return new EatHand(InteractionHand.OFF_HAND, -1, -1);
        }
        if (isAllowedFood(player.getMainHandItem(), player)) {
            return new EatHand(InteractionHand.MAIN_HAND, player.getInventory().selected, -1);
        }

        int bestSlot = -1;
        float bestScore = -1.0f;
        int maxSlot = VisualSettings.autoEatInventory() ? 36 : 9;
        for (int slot = 0; slot < maxSlot; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            FoodProperties food = stack.getFoodProperties(player);
            if (!isAllowedFood(stack, player) || food == null) {
                continue;
            }
            float score = PlayerItemRules.foodScore(food.getNutrition(), food.getSaturationModifier());
            if (PlayerItemRules.betterFoodCandidate(score, bestScore)) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        if (bestSlot < 0) {
            return null;
        }
        if (bestSlot < 9) {
            return new EatHand(InteractionHand.MAIN_HAND, bestSlot, -1);
        }
        return new EatHand(InteractionHand.MAIN_HAND, player.getInventory().selected, bestSlot);
    }

    private static boolean isAllowedFood(ItemStack stack, LocalPlayer player) {
        if (stack.isEmpty() || !stack.isEdible()) {
            return false;
        }
        if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            return false;
        }
        if (stack.is(Items.ROTTEN_FLESH) || stack.is(Items.PUFFERFISH) || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.POISONOUS_POTATO) || stack.is(Items.SUSPICIOUS_STEW)) {
            return false;
        }
        return stack.getFoodProperties(player) != null;
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


    private void swapInventoryFoodToHotbar(LocalPlayer player, int inventorySlot) {
        if (MC.gameMode == null || inventorySlot < 9 || inventorySlot >= 36) {
            return;
        }
        restoreInventorySwap(player);
        int hotbarSlot = player.getInventory().selected;
        MC.gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                InventorySlotMapping.inventoryToMenuSlot(inventorySlot),
                hotbarSlot,
                ClickType.SWAP,
                player
        );
        swappedInventorySlot = inventorySlot;
        swappedHotbarSlot = hotbarSlot;
    }

    private void restoreInventorySwap(LocalPlayer player) {
        if (player == null || MC.gameMode == null || swappedInventorySlot < 0 || swappedHotbarSlot < 0) {
            swappedInventorySlot = -1;
            swappedHotbarSlot = -1;
            return;
        }
        MC.gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                InventorySlotMapping.inventoryToMenuSlot(swappedInventorySlot),
                swappedHotbarSlot,
                ClickType.SWAP,
                player
        );
        swappedInventorySlot = -1;
        swappedHotbarSlot = -1;
    }

    private record EatHand(InteractionHand hand, int slot, int swapInventorySlot) {
    }
}
