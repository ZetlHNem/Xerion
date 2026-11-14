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

import java.util.function.Consumer;
import java.util.function.Function;

final class SliderValueAdapter {
    final Function<Float, String> formatter;
    final Function<String, Float> parser;
    final Consumer<Float> applier;

    SliderValueAdapter(Function<Float, String> formatter, Function<String, Float> parser,
                       Consumer<Float> applier) {
        this.formatter = formatter;
        this.parser = parser;
        this.applier = applier;
    }

    String format(float value) {
        String text = formatter == null ? null : formatter.apply(UiMath.clamp(value, 0.0f, 1.0f));
        return text == null || text.isBlank() ? WidgetSurfaceValueTools.sliderValueLabel(value) : text;
    }

    Float parse(String text) {
        return parser == null ? null : parser.apply(text);
    }
}
