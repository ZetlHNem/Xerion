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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public final class InventoryCleanerService {
    private static final Minecraft MC = Minecraft.getInstance();

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!VisualSettings.inventoryCleanerEnabled()) {
            cooldownTicks = 0;
            return;
        }

        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || !(MC.screen instanceof InventoryScreen) || player.containerMenu != player.inventoryMenu) {
            cooldownTicks = 0;
            return;
        }
        if (!player.inventoryMenu.getCarried().isEmpty() || cooldownTicks > 0) {
            return;
        }

        int slot = findTrashSlot(player);
        if (slot < 0) {
            return;
        }

        MC.gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                InventorySlotMapping.inventoryToMenuSlot(slot),
                1,
                ClickType.THROW,
                player
        );
        cooldownTicks = PlayerItemRules.roundedPositiveDelayTicks(VisualSettings.inventoryCleanerDelay());
    }

    private static int findTrashSlot(LocalPlayer player) {
        int start = PlayerItemRules.inventoryCleanerStartSlot(VisualSettings.inventoryCleanerCleanHotbar());
        int end = PlayerItemRules.inventoryCleanerEndSlot();
        for (int slot = start; slot < end; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isTrash(stack)) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean isTrash(ItemStack stack) {
        if (!PlayerItemRules.inventoryCleanerCandidate(stack.isEmpty())) {
            return false;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
        return PlayerItemRules.containsConfigured(VisualSettings.inventoryCleanerTrashItems(), id);
    }

}
