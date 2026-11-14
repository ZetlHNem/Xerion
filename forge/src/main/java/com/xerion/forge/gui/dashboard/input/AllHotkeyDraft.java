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

final class AllHotkeyDraft {
    final String controlId;
    final int bindingId;
    final HotkeyMeta meta;
    final String section;
    final String label;
    int keyCode;
    int scanCode;
    int mouseButton;
    String keyName;
    String mode;
    AllHotkeyVisibility visibility;
    Boolean boolValue;
    String selectedValue;
    int multiMask;
    float sliderValue;
    int colorValue;
    boolean deleted;

    AllHotkeyDraft(String controlId, HotkeyMeta meta, HotkeyBinding binding) {
        this.controlId = controlId;
        this.bindingId = binding.id;
        this.meta = meta;
        this.section = sectionFromHotkey(controlId, meta);
        this.label = HotkeyTextFormatter.displayLabel(controlId, meta.label);
        this.keyCode = binding.keyCode;
        this.scanCode = binding.scanCode;
        this.mouseButton = binding.mouseButton;
        this.keyName = binding.keyName;
        this.mode = binding.mode == null || binding.mode.isBlank() ? "Toggle" : binding.mode;
        this.visibility = binding.visible ? AllHotkeyVisibility.ON : AllHotkeyVisibility.OFF;
        this.boolValue = binding.boolValue;
        this.selectedValue = binding.selectedValue;
        this.multiMask = binding.multiMask;
        this.sliderValue = binding.sliderValue;
        this.colorValue = binding.colorValue;
    }

    String cellId(String cell) {
        return "all-hotkeys/" + controlId + "/" + bindingId + "/" + cell;
    }

    String animationId() {
        return controlId + "/" + bindingId;
    }

    String valueLabel() {
        return switch (meta.valueType) {
            case BOOL -> boolValue == null ? "-" : onOff(boolValue);
            case SELECT -> selectedValue == null || selectedValue.isBlank() ? "-" : selectedValue;
            case MULTI_SELECT -> WidgetSurfaceValueTools.multiSelectSummary(multiMask,
                    meta.options == null ? new String[0] : meta.options,
                    WidgetSurfaceValueTools.optionMask(meta.options == null ? 0 : meta.options.length));
            case SLIDER -> meta.sliderFormatter == null ? Integer.toString(Math.round(sliderValue * 100.0f))
                    : meta.sliderFormatter.apply(sliderValue);
            case COLOR -> colorHex(colorValue);
            case LABEL -> meta.valueLabel == null || meta.valueLabel.isBlank() ? "-" : meta.valueLabel;
        };
    }

    private static String sectionFromHotkey(String controlId, HotkeyMeta meta) {
        String group = meta.group == null || meta.group.isBlank() ? HotkeyTextFormatter.groupFromId(controlId) : meta.group;
        return group.replace('.', ' ').replace('/', ' ').replace("  ", " ").trim().toUpperCase();
    }

    private static String onOff(boolean value) {
        return value ? "On" : "Off";
    }

    private static String colorHex(int color) {
        return String.format("#%08X", color);
    }
}
