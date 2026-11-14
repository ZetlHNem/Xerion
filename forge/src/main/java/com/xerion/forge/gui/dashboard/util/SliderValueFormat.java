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

final class SliderValueFormat {
    private SliderValueFormat() {
    }

    static String intRangeLabel(float slider, float min, float max, String suffix) {
        float value = min + (max - min) * UiMath.clamp(slider, 0.0f, 1.0f);
        return Math.round(value) + suffix;
    }

    static String decimalLabel(float slider, float min, float max) {
        float value = min + (max - min) * UiMath.clamp(slider, 0.0f, 1.0f);
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    static String decimalRangeLabel(float slider, float min, float max, int decimals) {
        float value = min + (max - min) * UiMath.clamp(slider, 0.0f, 1.0f);
        return String.format(java.util.Locale.ROOT, "%." + decimals + "f", value);
    }

    static Float parseRangeSlider(String text, float min, float max) {
        Float value = parseNumber(text);
        if (value == null || max <= min) {
            return null;
        }
        return UiMath.clamp((value - min) / (max - min), 0.0f, 1.0f);
    }

    private static Float parseNumber(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean seenDigit = false;
        boolean seenDot = false;
        boolean seenSign = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '-' || c == '+') && !seenDigit && !seenDot && !seenSign) {
                builder.append(c);
                seenSign = true;
            } else if (c >= '0' && c <= '9') {
                builder.append(c);
                seenDigit = true;
            } else if (c == '.' && !seenDot) {
                builder.append(c);
                seenDot = true;
            }
        }
        if (!seenDigit) {
            return null;
        }
        try {
            return Float.parseFloat(builder.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
