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

import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public final class AutoTotemService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final int OFFHAND_SWAP_BUTTON = 40;
    private static final int CLICK_COOLDOWN_TICKS = 4;

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!CombatSettings.autoTotemEnabled() || cooldownTicks > 0) {
            return;
        }

        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || player.isSpectator()) {
            return;
        }
        if (MC.screen instanceof AbstractContainerScreen<?>) {
            return;
        }
        if (player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            return;
        }
        if (player.getHealth() + player.getAbsorptionAmount() > CombatSettings.autoTotemHealth()) {
            return;
        }

        int inventorySlot = findTotemSlot(player);
        if (inventorySlot < 0) {
            return;
        }

        int menuSlot = InventorySlotMapping.inventoryToMenuSlot(inventorySlot);
        MC.gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                menuSlot,
                OFFHAND_SWAP_BUTTON,
                ClickType.SWAP,
                player
        );
        cooldownTicks = CLICK_COOLDOWN_TICKS;
    }

    private static int findTotemSlot(LocalPlayer player) {
        for (int slot = 0; slot < 36; slot++) {
            if (player.getInventory().getItem(slot).is(Items.TOTEM_OF_UNDYING)) {
                return slot;
            }
        }
        return -1;
    }

}
