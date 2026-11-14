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

import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

final class HotkeyBinding {
    final int id;
    int keyCode = GLFW.GLFW_KEY_UNKNOWN;
    int scanCode;
    int mouseButton = -1;
    String keyName = "-";
    String mode = "Toggle";
    boolean visible = true;
    String valueLabel = "-";
    Boolean boolValue;
    String selectedValue = "-";
    String[] options;
    int multiMask;
    float sliderValue;
    Function<Float, String> sliderFormatter;
    Function<String, Float> sliderParser;
    int colorValue;
    boolean toggleActive;
    boolean holdActive;
    boolean deleting;

    HotkeyBinding(int id) {
        this.id = id;
    }

    boolean bound() {
        return keyCode != GLFW.GLFW_KEY_UNKNOWN || mouseButton >= 0;
    }

    boolean active() {
        return toggleActive || holdActive;
    }

    boolean matches(int pressedKeyCode, int pressedScanCode) {
        return mouseButton < 0 && keyCode != GLFW.GLFW_KEY_UNKNOWN
                && keyCode == pressedKeyCode
                && (scanCode == 0 || scanCode == pressedScanCode);
    }

    boolean matchesMouse(int pressedButton) {
        return mouseButton >= 0 && mouseButton == pressedButton;
    }

    void bindKeyboard(int nextKeyCode, int nextScanCode) {
        keyCode = nextKeyCode;
        scanCode = nextScanCode;
        mouseButton = -1;
        keyName = InputDisplayNames.keyName(nextKeyCode, nextScanCode);
    }

    void bindMouse(int button) {
        keyCode = GLFW.GLFW_KEY_UNKNOWN;
        scanCode = 0;
        mouseButton = button;
        keyName = InputDisplayNames.mouseButtonName(button);
    }

    void clearKey() {
        keyCode = GLFW.GLFW_KEY_UNKNOWN;
        scanCode = 0;
        mouseButton = -1;
        keyName = "-";
    }
}
