package com.xerion.forge.combat;

import com.xerion.common.config.CombatSettings;
import com.xerion.forge.util.HotbarSlotActions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

/**
 * Port of OpenOpal's Auto Head: eats a golden head from the hotbar when health
 * drops below the configured percentage.
 */
public final class AutoHeadService {
    private static long lastUse;
    private static int previousSlot = -1;

    private AutoHeadService() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (previousSlot >= 0) {
            restoreSlot(player);
            return;
        }
        if (!CombatSettings.autoHeadEnabled() || player == null || minecraft.gameMode == null
                || minecraft.screen != null || player.isUsingItem() || player.isSpectator()) {
            return;
        }
        float percent = player.getHealth() / player.getMaxHealth() * 100.0F;
        if (percent > CombatSettings.autoHeadHealthPercent()
                || player.getAbsorptionAmount() > 2.0F
                || System.currentTimeMillis() - lastUse < CombatSettings.autoHeadDelayMs()) {
            return;
        }
        int slot = findHeadSlot(player);
        if (slot < 0) {
            return;
        }
        previousSlot = player.getInventory().selected;
        HotbarSlotActions.select(player, slot);
        minecraft.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        lastUse = System.currentTimeMillis();
    }

    public static void clearRuntimeState() {
        previousSlot = -1;
        lastUse = 0L;
    }

    private static void restoreSlot(LocalPlayer player) {
        if (player != null && previousSlot >= 0) {
            HotbarSlotActions.select(player, previousSlot);
        }
        previousSlot = -1;
    }

    private static int findHeadSlot(LocalPlayer player) {
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.PLAYER_HEAD)
                    && stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains("head")) {
                return slot;
            }
        }
        return -1;
    }
}
