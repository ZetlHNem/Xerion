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

final class WidgetSurfaceValueTools {
    private WidgetSurfaceValueTools() {
    }

    static String[] selectOptions(String value) {
        String[] raw = value.split(",");
        int count = 0;
        for (String option : raw) {
            if (!option.trim().isEmpty()) {
                count++;
            }
        }
        if (count == 0) {
            return new String[] { value };
        }
        String[] options = new String[count];
        int index = 0;
        for (String option : raw) {
            String trimmed = option.trim();
            if (!trimmed.isEmpty()) {
                options[index++] = trimmed;
            }
        }
        return options;
    }

    static String selectDefault(String defaultValue, String[] options) {
        if (containsOption(defaultValue, options)) {
            return defaultValue;
        }
        return options.length == 0 ? defaultValue : options[0];
    }

    static boolean containsOption(String value, String[] options) {
        for (String option : options) {
            if (option.equals(value)) {
                return true;
            }
        }
        return false;
    }

    static int optionMask(int optionCount) {
        if (optionCount <= 0) {
            return 0;
        }
        if (optionCount >= Integer.SIZE - 1) {
            return Integer.MAX_VALUE;
        }
        return (1 << optionCount) - 1;
    }

    static String nextOption(String current, String[] options) {
        if (options == null || options.length == 0) {
            return current == null || current.isEmpty() ? "-" : current;
        }
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(current)) {
                return options[(i + 1) % options.length];
            }
        }
        return options[0];
    }

    static int nextMultiMask(int current, int allMask) {
        if (allMask == 0) {
            return 0;
        }
        if (current == 0) {
            return allMask;
        }
        if (current == allMask) {
            return 1;
        }
        int next = current << 1;
        return (next & allMask) == 0 ? 0 : next & allMask;
    }

    static String sliderValueLabel(float value) {
        return Math.round(UiMath.clamp01(value) * 100.0f) + "%";
    }

    static Function<Float, String> fallbackSliderFormatter(float defaultValue, String suffix) {
        String safeSuffix = suffix == null ? "" : suffix;
        ParsedSliderSuffix parsed = ParsedSliderSuffix.from(defaultValue, safeSuffix);
        if (parsed.percent) {
            return value -> Math.round(UiMath.clamp01(value) * 100.0f) + "%";
        }
        if (parsed.scale > 0.0f) {
            return value -> formatSliderNumber(UiMath.clamp01(value) * parsed.scale) + parsed.unit;
        }
        return value -> safeSuffix;
    }

    static Function<String, Float> fallbackSliderParser(float defaultValue, String suffix) {
        String safeSuffix = suffix == null ? "" : suffix;
        ParsedSliderSuffix parsed = ParsedSliderSuffix.from(defaultValue, safeSuffix);
        return text -> {
            Float number = parseSliderNumber(text);
            if (number == null) {
                return null;
            }
            if (parsed.percent) {
                return UiMath.clamp01(number / 100.0f);
            }
            if (parsed.scale > 0.0f) {
                return UiMath.clamp01(number / parsed.scale);
            }
            return UiMath.clamp01(number);
        };
    }

    static Float parseSliderNumber(String text) {
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

    static int parseInt(String text, int fallback) {
        if (text == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    static float parseFloat(String text, float fallback) {
        if (text == null) {
            return fallback;
        }
        try {
            return Float.parseFloat(text.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    static String formatSliderNumber(float value) {
        float rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.001f) {
            return Integer.toString((int) rounded);
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    static int nextHotkeyColor(int color) {
        int rgb = color & 0x00FFFFFF;
        if (rgb == 0x6E72FF) {
            return 0xFFFF5F67;
        }
        if (rgb == 0xFF5F67) {
            return 0xFF2FCB8B;
        }
        if (rgb == 0x2FCB8B) {
            return 0xFFFFB02E;
        }
        return 0xFF6E72FF;
    }

    static String multiSelectSummary(int mask, String[] options, int allMask) {
        int selected = mask & allMask;
        if (selected == 0) {
            return "None";
        }
        if (selected == allMask) {
            return "All";
        }

        int count = 0;
        int singleIndex = -1;
        for (int i = 0; i < options.length; i++) {
            if ((selected & (1 << i)) != 0) {
                count++;
                singleIndex = i;
            }
        }
        if (count == 1 && singleIndex >= 0) {
            return options[singleIndex];
        }
        return count + " selected";
    }

    private record ParsedSliderSuffix(float scale, String unit, boolean percent) {
        private static ParsedSliderSuffix from(float defaultValue, String suffix) {
            boolean percent = suffix.contains("%");
            Float number = parseSliderNumber(suffix);
            if (percent) {
                return new ParsedSliderSuffix(100.0f, "%", true);
            }
            if (number == null || number <= 0.0f || defaultValue <= 0.0f) {
                return new ParsedSliderSuffix(0.0f, suffix, false);
            }
            String unit = suffix.replaceAll("[+\\-0-9.\\s]", "");
            return new ParsedSliderSuffix(number / defaultValue, unit, false);
        }
    }
}
