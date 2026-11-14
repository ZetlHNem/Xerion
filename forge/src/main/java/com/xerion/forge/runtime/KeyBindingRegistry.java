package com.xerion.forge.runtime;

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

import com.xerion.common.XerionCore;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.gui.ClickGuiScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class KeyBindingRegistry {
    private static final KeyMapping OPEN_CLICK_GUI = new KeyMapping(
            "key.xerion.click_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories.xerion"
    );

    private KeyBindingRegistry() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CLICK_GUI);
    }

    public static boolean isOpenClickGuiKey(int key, int scanCode) {
        return OPEN_CLICK_GUI.matches(key, scanCode);
    }

    public static void handleOpenClickGuiToggle(Minecraft minecraft, XerionCore core) {
        if (!OPEN_CLICK_GUI.consumeClick()) {
            return;
        }
        while (OPEN_CLICK_GUI.consumeClick()) {
            // Drop repeated key events so screen/cursor state only changes once per client tick.
        }
        if (minecraft.screen instanceof ClickGuiScreen) {
            minecraft.setScreen(null);
        } else {
            minecraft.setScreen(new ClickGuiScreen(core));
        }
    }

    public static boolean isVeinMinerKeyDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return false;
        }
        if (minecraft.screen instanceof ClickGuiScreen) {
            return false;
        }
        long window = minecraft.getWindow().getWindow();
        int mouseButton = VisualSettings.veinMinerMouseButton();
        if (mouseButton >= 0) {
            return GLFW.glfwGetMouseButton(window, mouseButton) == GLFW.GLFW_PRESS;
        }
        int keyCode = VisualSettings.veinMinerKeyCode();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return false;
        }
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }

    public static String veinMinerKeyName() {
        int mouseButton = VisualSettings.veinMinerMouseButton();
        if (mouseButton >= 0) {
            return mouseButtonName(mouseButton);
        }
        int keyCode = VisualSettings.veinMinerKeyCode();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "-";
        }
        return InputConstants.getKey(keyCode, VisualSettings.veinMinerScanCode()).getDisplayName().getString();
    }

    public static boolean isAutoSortKeyDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.screen instanceof ClickGuiScreen) {
            return false;
        }
        int keyCode = VisualSettings.autoSortKeyCode();
        return keyCode != GLFW.GLFW_KEY_UNKNOWN
                && GLFW.glfwGetKey(minecraft.getWindow().getWindow(), keyCode) == GLFW.GLFW_PRESS;
    }

    public static String autoSortKeyName() {
        int keyCode = VisualSettings.autoSortKeyCode();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "-";
        }
        return InputConstants.getKey(keyCode, VisualSettings.autoSortScanCode()).getDisplayName().getString();
    }

    public static String pearlThrowKeyName() {
        int mouseButton = VisualSettings.pearlThrowMouseButton();
        if (mouseButton >= 0) {
            return mouseButtonName(mouseButton);
        }
        int keyCode = VisualSettings.pearlThrowKeyCode();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "-";
        }
        return InputConstants.getKey(keyCode, VisualSettings.pearlThrowScanCode()).getDisplayName().getString();
    }

    public static boolean isQuickSwitchKeyDown() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.screen instanceof ClickGuiScreen) {
            return false;
        }
        long window = minecraft.getWindow().getWindow();
        int mouseButton = VisualSettings.quickSwitchMouseButton();
        if (mouseButton >= 0) {
            return GLFW.glfwGetMouseButton(window, mouseButton) == GLFW.GLFW_PRESS;
        }
        int keyCode = VisualSettings.quickSwitchKeyCode();
        return keyCode != GLFW.GLFW_KEY_UNKNOWN
                && GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }

    public static String quickSwitchKeyName() {
        int mouseButton = VisualSettings.quickSwitchMouseButton();
        if (mouseButton >= 0) {
            return mouseButtonName(mouseButton);
        }
        int keyCode = VisualSettings.quickSwitchKeyCode();
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "-";
        }
        return InputConstants.getKey(keyCode, VisualSettings.quickSwitchScanCode()).getDisplayName().getString();
    }

    private static String mouseButtonName(int mouseButton) {
        return switch (mouseButton) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "Mouse Left";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "Mouse Right";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "Mouse Middle";
            default -> "Mouse " + (mouseButton + 1);
        };
    }
}

