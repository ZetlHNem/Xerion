package com.xerion.forge.gui.dashboard;

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

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

final class InputDisplayNames {
    private InputDisplayNames() {
    }

    static String keyName(int keyCode, int scanCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "-";
        }
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        String name = key.getDisplayName().getString();
        return name == null || name.isBlank() ? Integer.toString(keyCode) : name.toUpperCase();
    }

    static String mouseButtonName(int button) {
        return switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "M1";
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "M2";
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "M3";
            default -> "M" + (button + 1);
        };
    }
}
