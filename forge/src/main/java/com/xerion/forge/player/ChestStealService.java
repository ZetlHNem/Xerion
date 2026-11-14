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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

public final class ChestStealService {
    private static final Minecraft MC = Minecraft.getInstance();

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!VisualSettings.chestStealEnabled()) {
            cooldownTicks = 0;
            return;
        }

        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || !(MC.screen instanceof AbstractContainerScreen<?>)) {
            cooldownTicks = 0;
            return;
        }

        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || menu instanceof InventoryMenu || menu == player.inventoryMenu) {
            return;
        }
        if (cooldownTicks > 0) {
            return;
        }

        int slot = firstContainerItemSlot(menu);
        if (PlayerItemRules.chestStealHasMove(slot)) {
            MC.gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, player);
            cooldownTicks = PlayerItemRules.nonNegativeDelayTicks(VisualSettings.chestStealDelayTicks());
            return;
        }

        if (PlayerItemRules.chestStealShouldAutoClose(VisualSettings.chestStealAutoClose(), slot)) {
            player.closeContainer();
            MC.setScreen(null);
        }
    }

    private static int firstContainerItemSlot(AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            if (PlayerItemRules.chestStealSlotCandidate(
                    slot.container instanceof net.minecraft.world.entity.player.Inventory,
                    slot.hasItem())) {
                return slot.index;
            }
        }
        return -1;
    }
}
