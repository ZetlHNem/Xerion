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
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public final class QuickSwitchService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static boolean holdActive;
    private static boolean toggleActive;
    private static int previousSlot = -1;

    private QuickSwitchService() {
    }

    public static void tick() {
        if (!VisualSettings.quickSwitchEnabled()) {
            reset(true);
            return;
        }
        LocalPlayer player = MC.player;
        if (player == null || MC.getConnection() == null || MC.screen != null || player.isSpectator()) {
            reset(true);
            return;
        }
        if (PlayerItemRules.quickSwitchHoldMode(VisualSettings.quickSwitchMode())) {
            boolean down = ForgeModEventBridge.isQuickSwitchKeyDown();
            if (down && !holdActive) {
                activate(player);
                holdActive = true;
            } else if (!down && holdActive) {
                restore(player);
                holdActive = false;
            }
        }
    }

    public static boolean handleKeyPress(int keyCode, int scanCode) {
        if (VisualSettings.quickSwitchMouseButton() >= 0
                || keyCode != VisualSettings.quickSwitchKeyCode()
                || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        return handlePress();
    }

    public static boolean handleMousePress(int button) {
        if (button != VisualSettings.quickSwitchMouseButton()) {
            return false;
        }
        return handlePress();
    }

    private static boolean handlePress() {
        if (!VisualSettings.quickSwitchEnabled()) {
            return false;
        }
        if (!PlayerItemRules.quickSwitchToggleMode(VisualSettings.quickSwitchMode())) {
            return false;
        }
        LocalPlayer player = MC.player;
        if (player == null || MC.getConnection() == null || MC.screen != null || player.isSpectator()) {
            return false;
        }
        if (toggleActive) {
            restore(player);
            toggleActive = false;
        } else {
            activate(player);
            toggleActive = true;
        }
        return true;
    }

    private static void activate(LocalPlayer player) {
        int targetSlot = VisualSettings.quickSwitchSlotIndex();
        if (player.getInventory().selected == targetSlot) {
            return;
        }
        if (previousSlot < 0) {
            previousSlot = player.getInventory().selected;
        }
        HotbarSlotActions.select(player, targetSlot);
    }

    private static void restore(LocalPlayer player) {
        if (VisualSettings.quickSwitchRestoreSlot() && previousSlot >= 0) {
            HotbarSlotActions.select(player, previousSlot);
        }
        previousSlot = -1;
    }

    private static void reset(boolean restore) {
        LocalPlayer player = MC.player;
        if (restore && player != null && MC.getConnection() != null) {
            restore(player);
        } else {
            previousSlot = -1;
        }
        holdActive = false;
        toggleActive = false;
    }

}

