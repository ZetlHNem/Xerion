package com.xerion.forge.combat;

import com.xerion.common.combat.CombatActionRules;

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public final class PearlThrowService {
    private static final Minecraft MC = Minecraft.getInstance();

    private static int throwDelayTicks;
    private static int restoreDelayTicks;
    private static PendingThrow pendingThrow;
    private static PendingRestore pendingRestore;

    private PearlThrowService() {
    }

    public static void tick() {
        if (!VisualSettings.pearlThrowEnabled()) {
            clearPending();
            return;
        }
        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            clearPending();
            return;
        }

        if (pendingThrow != null) {
            if (throwDelayTicks > 0) {
                throwDelayTicks--;
                return;
            }
            finishThrow(player, pendingThrow);
            pendingThrow = null;
            return;
        }

        if (pendingRestore != null) {
            if (restoreDelayTicks > 0) {
                restoreDelayTicks--;
                return;
            }
            restore(player, pendingRestore);
            pendingRestore = null;
        }
    }

    public static boolean handleKey(int keyCode, int scanCode) {
        if (VisualSettings.pearlThrowMouseButton() >= 0
                || keyCode != VisualSettings.pearlThrowKeyCode()
                || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        return startThrow();
    }

    public static boolean handleMouse(int button) {
        if (button != VisualSettings.pearlThrowMouseButton()) {
            return false;
        }
        return startThrow();
    }

    private static boolean startThrow() {
        if (!VisualSettings.pearlThrowEnabled()) {
            return false;
        }
        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            return false;
        }
        if (pendingThrow != null || pendingRestore != null || player.getCooldowns().isOnCooldown(Items.ENDER_PEARL)) {
            return true;
        }

        PearlSlot pearl = findPearl(player);
        if (pearl == null) {
            return false;
        }

        int previousSlot = player.getInventory().selected;
        boolean switchMode = CombatActionRules.isSwitchMode(VisualSettings.pearlThrowMode());
        int throwDelay = CombatActionRules.roundedDelayTicks(VisualSettings.pearlThrowDelay());

        if (pearl.offhand()) {
            queueOrThrow(player, new PendingThrow(InteractionHand.OFF_HAND, previousSlot, -1, false), throwDelay);
            return true;
        }

        if (pearl.hotbarSlot() >= 0) {
            if (switchMode && pearl.hotbarSlot() != previousSlot) {
                HotbarSlotActions.select(player, pearl.hotbarSlot());
            }
            queueOrThrow(player, new PendingThrow(InteractionHand.MAIN_HAND, previousSlot, -1,
                    !switchMode && pearl.hotbarSlot() != previousSlot, pearl.hotbarSlot()), throwDelay);
            return true;
        }

        if (pearl.inventorySlot() >= 0) {
            if (switchMode) {
                swapInventorySlotToSelected(player, pearl.inventorySlot(), previousSlot);
            }
            queueOrThrow(player, new PendingThrow(InteractionHand.MAIN_HAND, previousSlot, pearl.inventorySlot(),
                    !switchMode, -1), throwDelay);
            return true;
        }
        return false;
    }

    private static void queueOrThrow(LocalPlayer player, PendingThrow pending, int delay) {
        if (CombatActionRules.shouldRunImmediately(delay)) {
            finishThrow(player, pending);
            return;
        }
        pendingThrow = pending;
        throwDelayTicks = delay;
    }

    private static void finishThrow(LocalPlayer player, PendingThrow pending) {
        if (pending.hotbarSlot() >= 0 && player.getInventory().selected != pending.hotbarSlot()) {
            HotbarSlotActions.select(player, pending.hotbarSlot());
        }
        boolean swappedInventoryNow = false;
        if (pending.inventorySlot() >= 0 && !alreadyPreparedInventorySwap(pending)) {
            swapInventorySlotToSelected(player, pending.inventorySlot(), pending.previousHotbarSlot());
            swappedInventoryNow = true;
        }

        usePearl(player, pending.hand());

        if (pending.restoreImmediately()) {
            if (pending.inventorySlot() >= 0 && swappedInventoryNow) {
                swapInventorySlotToSelected(player, pending.inventorySlot(), pending.previousHotbarSlot());
            }
            if (pending.hotbarSlot() >= 0) {
                HotbarSlotActions.select(player, pending.previousHotbarSlot());
            }
            return;
        }

        if (CombatActionRules.isSwitchMode(VisualSettings.pearlThrowMode())) {
            scheduleRestore(player, new PendingRestore(pending.previousHotbarSlot(), pending.inventorySlot()));
        }
    }

    private static boolean alreadyPreparedInventorySwap(PendingThrow pending) {
        return CombatActionRules.pearlInventorySwapAlreadyPrepared(VisualSettings.pearlThrowMode(), pending.inventorySlot());
    }

    private static void scheduleRestore(LocalPlayer player, PendingRestore restore) {
        int delay = CombatActionRules.roundedDelayTicks(VisualSettings.pearlThrowSwitchDelay());
        if (CombatActionRules.shouldRunImmediately(delay)) {
            restore(player, restore);
            return;
        }
        pendingRestore = restore;
        restoreDelayTicks = delay;
    }

    private static void restore(LocalPlayer player, PendingRestore restore) {
        if (restore.inventorySlot() >= 0) {
            swapInventorySlotToSelected(player, restore.inventorySlot(), restore.previousHotbarSlot());
        }
        HotbarSlotActions.select(player, restore.previousHotbarSlot());
    }

    private static void usePearl(LocalPlayer player, InteractionHand hand) {
        InteractionResult result = MC.gameMode.useItem(player, hand);
        if (result.consumesAction() && VisualSettings.pearlThrowSwing()) {
            player.swing(hand);
        }
    }

    private static void clearPending() {
        pendingThrow = null;
        pendingRestore = null;
        throwDelayTicks = 0;
        restoreDelayTicks = 0;
    }

    public static void clearRuntimeState() {
        clearPending();
    }

    private static PearlSlot findPearl(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.ENDER_PEARL)) {
            return PearlSlot.offhandSlot();
        }
        if (player.getMainHandItem().is(Items.ENDER_PEARL)) {
            return PearlSlot.hotbar(player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.ENDER_PEARL)) {
                return PearlSlot.hotbar(slot);
            }
        }
        for (int slot = 9; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.ENDER_PEARL)) {
                return PearlSlot.inventory(slot);
            }
        }
        return null;
    }

    private static void swapInventorySlotToSelected(LocalPlayer player, int inventorySlot, int hotbarSlot) {
        if (MC.gameMode == null || inventorySlot < 9 || inventorySlot >= 36 || hotbarSlot < 0 || hotbarSlot > 8) {
            return;
        }
        MC.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId,
                InventorySlotMapping.inventoryToMenuSlot(inventorySlot), hotbarSlot, ClickType.SWAP, player);
    }

    private record PearlSlot(boolean offhand, int hotbarSlot, int inventorySlot) {
        static PearlSlot offhandSlot() {
            return new PearlSlot(true, -1, -1);
        }

        static PearlSlot hotbar(int slot) {
            return new PearlSlot(false, slot, -1);
        }

        static PearlSlot inventory(int slot) {
            return new PearlSlot(false, -1, slot);
        }
    }

    private record PendingThrow(InteractionHand hand, int previousHotbarSlot, int inventorySlot,
                                boolean restoreImmediately, int hotbarSlot) {
        PendingThrow(InteractionHand hand, int previousHotbarSlot, int inventorySlot, boolean restoreImmediately) {
            this(hand, previousHotbarSlot, inventorySlot, restoreImmediately, -1);
        }
    }

    private record PendingRestore(int previousHotbarSlot, int inventorySlot) {
    }
}
