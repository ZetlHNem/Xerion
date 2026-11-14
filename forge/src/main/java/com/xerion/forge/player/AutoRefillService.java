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
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public final class AutoRefillService {
    private static final Minecraft MC = Minecraft.getInstance();

    private final ItemStack[] lastHotbarStacks = new ItemStack[9];
    private int cooldownTicks;

    public AutoRefillService() {
        for (int i = 0; i < lastHotbarStacks.length; i++) {
            lastHotbarStacks[i] = ItemStack.EMPTY;
        }
    }

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null) {
            reset();
            return;
        }
        rememberHotbar(player);

        if (!VisualSettings.autoRefillEnabled()) {
            cooldownTicks = 0;
            return;
        }
        if (MC.screen != null && !(MC.screen instanceof InventoryScreen)) {
            return;
        }
        if (player.containerMenu != player.inventoryMenu || !player.inventoryMenu.getCarried().isEmpty() || cooldownTicks > 0) {
            return;
        }

        RefillPlan plan = findRefillPlan(player);
        if (plan == null) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                InventorySlotMapping.inventoryToMenuSlot(plan.sourceSlot()),
                plan.hotbarSlot(),
                ClickType.SWAP,
                player
        );
        cooldownTicks = PlayerItemRules.roundedPositiveDelayTicks(VisualSettings.autoRefillDelay());
    }

    private void reset() {
        cooldownTicks = 0;
        for (int i = 0; i < lastHotbarStacks.length; i++) {
            lastHotbarStacks[i] = ItemStack.EMPTY;
        }
    }

    private void rememberHotbar(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                lastHotbarStacks[slot] = stack.copyWithCount(1);
            }
        }
    }

    private RefillPlan findRefillPlan(LocalPlayer player) {
        int selected = player.getInventory().selected;
        int start = PlayerItemRules.hotbarScanStart(VisualSettings.autoRefillCurrentSlotOnly(), selected);
        int end = PlayerItemRules.hotbarScanEnd(VisualSettings.autoRefillCurrentSlotOnly(), selected);
        int threshold = PlayerItemRules.refillThreshold(VisualSettings.autoRefillThreshold());

        for (int hotbarSlot = start; hotbarSlot < end; hotbarSlot++) {
            ItemStack current = player.getInventory().getItem(hotbarSlot);
            ItemStack target = current.isEmpty() ? lastHotbarStacks[hotbarSlot] : current;
            if (target.isEmpty()) {
                continue;
            }
            if (PlayerItemRules.shouldSkipRefill(current.isEmpty(), current.getCount(), current.getMaxStackSize(), threshold)) {
                continue;
            }
            int sourceSlot = findBestSourceSlot(player, target, current.getCount());
            if (sourceSlot >= 0) {
                return new RefillPlan(hotbarSlot, sourceSlot);
            }
        }
        return null;
    }

    private static int findBestSourceSlot(LocalPlayer player, ItemStack target, int currentCount) {
        int bestSlot = -1;
        int bestCount = currentCount;
        for (int slot = 9; slot < 36; slot++) {
            ItemStack candidate = player.getInventory().getItem(slot);
            if (candidate.isEmpty() || !ItemStack.isSameItemSameTags(candidate, target)) {
                continue;
            }
            if (PlayerItemRules.betterRefillSource(candidate.getCount(), bestCount)) {
                bestSlot = slot;
                bestCount = candidate.getCount();
            }
        }
        return bestSlot;
    }

    private record RefillPlan(int hotbarSlot, int sourceSlot) {
    }
}
