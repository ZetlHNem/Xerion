package com.xerion.common.config;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public final class ConfigValueCodec {
    private ConfigValueCodec() {
    }

    public static float clampOffset(float value) {
        return Math.max(-96.0f, Math.min(96.0f, value));
    }

    public static float inverse(float value, float min, float max) {
        if (max <= min) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }

    public static float range(float slider, float min, float max) {
        float normalized = Math.max(0.0f, Math.min(1.0f, slider));
        return min + (max - min) * normalized;
    }

    public static boolean idListContains(String list, String id) {
        String normalizedId = normalizeId(id);
        if (normalizedId.isBlank() || list == null || list.isBlank()) {
            return false;
        }
        return ("|" + normalizeIdList(list) + "|").contains("|" + normalizedId + "|");
    }

    public static String normalizeIdList(String ids) {
        if (ids == null || ids.isBlank()) {
            return "";
        }
        Set<String> normalized = new TreeSet<>();
        for (String part : ids.split("\\|")) {
            String id = normalizeId(part);
            if (!id.isBlank()) {
                normalized.add(id);
            }
        }
        return String.join("|", normalized);
    }

    public static String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }
}
