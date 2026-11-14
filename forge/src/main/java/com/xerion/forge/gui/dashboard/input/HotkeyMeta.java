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

import java.util.function.Function;

final class HotkeyMeta {
    static final HotkeyMeta EMPTY = new HotkeyMeta("", "", "", "-", null,
            HotkeyValueType.LABEL, "-", null, 0, 0.0f, 0, null, null, null);

    final String group;
    final String label;
    final String kind;
    final String valueLabel;
    final Boolean boolValue;
    final HotkeyValueType valueType;
    final String selectedValue;
    final String[] options;
    final int multiMask;
    final float sliderValue;
    final int colorValue;
    final HotkeyApplier applier;
    final Function<Float, String> sliderFormatter;
    final Function<String, Float> sliderParser;

    HotkeyMeta(String group, String label, String kind, String valueLabel, Boolean boolValue,
               HotkeyValueType valueType, String selectedValue, String[] options,
               int multiMask, float sliderValue, int colorValue, HotkeyApplier applier,
               Function<Float, String> sliderFormatter, Function<String, Float> sliderParser) {
        this.group = group;
        this.label = label;
        this.kind = kind;
        this.valueLabel = valueLabel;
        this.boolValue = boolValue;
        this.valueType = valueType;
        this.selectedValue = selectedValue;
        this.options = options;
        this.multiMask = multiMask;
        this.sliderValue = sliderValue;
        this.colorValue = colorValue;
        this.applier = applier;
        this.sliderFormatter = sliderFormatter;
        this.sliderParser = sliderParser;
    }
}
