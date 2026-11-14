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

import com.xerion.forge.gui.dashboard.WidgetSurface;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

public final class InputEventDispatcher {
    private InputEventDispatcher() {
    }

    public static void handleKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }
        if (ForgeModEventBridge.isOpenClickGuiKey(event.getKey(), event.getScanCode())) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (QuickSwitchService.handleKeyPress(event.getKey(), event.getScanCode())) {
                return;
            }
            if (PearlThrowService.handleKey(event.getKey(), event.getScanCode())) {
                return;
            }
            WidgetSurface.globalKeyPressed(event.getKey(), event.getScanCode());
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            WidgetSurface.globalKeyReleased(event.getKey(), event.getScanCode());
        }
    }

    public static void handleMouseButton(InputEvent.MouseButton event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS) {
            if (QuickSwitchService.handleMousePress(event.getButton())) {
                event.setCanceled(true);
                return;
            }
            if (PearlThrowService.handleMouse(event.getButton())) {
                event.setCanceled(true);
                return;
            }
            WidgetSurface.globalMousePressed(event.getButton());
            handleLeftClickEntityHit(minecraft, event.getButton());
        } else if (event.getAction() == GLFW.GLFW_RELEASE) {
            WidgetSurface.globalMouseReleased(event.getButton());
        }
    }

    private static void handleLeftClickEntityHit(Minecraft minecraft, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !(minecraft.hitResult instanceof EntityHitResult hit)) {
            return;
        }
        if (FakePlayerRegistry.hurtIfDummy(hit.getEntity()) && hit.getEntity() instanceof LivingEntity target) {
            HitSoundService.play(target);
        }
    }
}

