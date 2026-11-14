package com.xerion.forge.movement;

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

import com.xerion.forge.gui.ClickGuiScreen;
import com.xerion.forge.gui.dashboard.WidgetSurface;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public final class MenuMovementService {
    private MenuMovementService() {
    }

    public static boolean movementAllowed(Minecraft minecraft) {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        Screen screen = minecraft.screen;
        if (screen == null) {
            return false;
        }
        if (screen instanceof ClickGuiScreen) {
            return !WidgetSurface.blocksGameMovementInput();
        }
        if (!VisualSettings.inventoryMoveEnabled() || !isInventoryMoveScreen(screen)) {
            return false;
        }
        if ("Heypixel".equals(VisualSettings.inventoryMoveMode())) {
            // OpenOpal additionally requires its InventoryManager to be performing
            // an action; Xerion has no such manager, so the player inventory is used
            // as the closest equivalent context.
            return screen instanceof InventoryScreen;
        }
        return true;
    }

    public static void updateKeyMappings(Minecraft minecraft) {
        if (minecraft == null || minecraft.screen == null) {
            return;
        }
        boolean active = movementAllowed(minecraft);
        setScreenKeyState(minecraft, minecraft.options.keyUp, active);
        setScreenKeyState(minecraft, minecraft.options.keyDown, active);
        setScreenKeyState(minecraft, minecraft.options.keyLeft, active);
        setScreenKeyState(minecraft, minecraft.options.keyRight, active);
        setScreenKeyState(minecraft, minecraft.options.keyJump, active);
        setScreenKeyState(minecraft, minecraft.options.keyShift, active);
        setScreenKeyState(minecraft, minecraft.options.keySprint, active);
    }

    public static boolean applyToInput(Minecraft minecraft, Input input) {
        if (!movementAllowed(minecraft) || input == null) {
            return false;
        }
        input.up = keyDown(minecraft, minecraft.options.keyUp);
        input.down = keyDown(minecraft, minecraft.options.keyDown);
        input.left = keyDown(minecraft, minecraft.options.keyLeft);
        input.right = keyDown(minecraft, minecraft.options.keyRight);
        input.jumping = keyDown(minecraft, minecraft.options.keyJump);
        input.shiftKeyDown = keyDown(minecraft, minecraft.options.keyShift);
        input.forwardImpulse = input.up == input.down ? 0.0F : input.up ? 1.0F : -1.0F;
        input.leftImpulse = input.left == input.right ? 0.0F : input.left ? 1.0F : -1.0F;
        applySprint(minecraft, input);
        return true;
    }

    public static boolean keyDown(Minecraft minecraft, KeyMapping mapping) {
        if (minecraft == null || mapping == null) {
            return false;
        }
        InputConstants.Key key = mapping.getKey();
        long window = minecraft.getWindow().getWindow();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        }
        if (key.getType() == InputConstants.Type.KEYSYM) {
            return GLFW.glfwGetKey(window, key.getValue()) == GLFW.GLFW_PRESS;
        }
        return mapping.isDown();
    }

    private static void setScreenKeyState(Minecraft minecraft, KeyMapping mapping, boolean active) {
        mapping.setDown(active && keyDown(minecraft, mapping));
    }

    private static boolean isInventoryMoveScreen(Screen screen) {
        return screen instanceof AbstractContainerScreen<?>;
    }

    private static void applySprint(Minecraft minecraft, Input input) {
        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger() || player.isSpectator() || input.shiftKeyDown) {
            return;
        }
        if (keyDown(minecraft, minecraft.options.keySprint)
                && input.forwardImpulse > 0.0F
                && !player.horizontalCollision) {
            player.setSprinting(true);
        }
    }
}
