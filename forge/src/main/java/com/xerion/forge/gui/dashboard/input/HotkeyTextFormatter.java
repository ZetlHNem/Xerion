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

final class HotkeyTextFormatter {
    private HotkeyTextFormatter() {
    }

    static String cleanGroup(String group) {
        if (group == null) {
            return "";
        }
        return group.trim();
    }

    static String groupFromId(String id) {
        if (id == null || id.isBlank()) {
            return "";
        }
        if (id.startsWith("module/")) {
            return titleFromIdentifier(id.substring("module/".length()));
        }
        int slash = id.indexOf('/');
        if (slash <= 0) {
            return cleanGroup(id);
        }
        int secondSlash = id.indexOf('/', slash + 1);
        String group = secondSlash <= slash ? id.substring(0, slash) : id.substring(slash + 1, secondSlash);
        return cleanGroup(group);
    }

    static String hudName(String group, String label) {
        String cleanLabel = label == null ? "" : label.trim();
        String cleanGroup = cleanGroup(group);
        if (cleanGroup.isEmpty()) {
            return cleanLabel;
        }
        if (cleanLabel.isEmpty()) {
            return cleanGroup;
        }
        return cleanGroup + "." + cleanLabel;
    }

    static String displayLabel(String controlId, String label) {
        String cleanLabel = label == null ? "" : label.trim();
        if (isNumericLabel(cleanLabel)) {
            return fallbackLabel(controlId);
        }
        if (!"Enabled".equalsIgnoreCase(cleanLabel)) {
            return cleanLabel.isEmpty() ? controlId : cleanLabel;
        }
        return "Enable";
    }

    static String fallbackLabel(String controlId) {
        if (controlId == null || controlId.isBlank()) {
            return "Hotkey";
        }
        if (controlId.startsWith("module/")) {
            return "Enable";
        }
        String[] parts = controlId.split("/");
        if (parts.length >= 5 && isNumericLabel(parts[parts.length - 1])
                && isNumericLabel(parts[parts.length - 2])) {
            return titleFromIdentifier(parts[parts.length - 3]);
        }
        int last = Math.max(controlId.lastIndexOf('/'), controlId.lastIndexOf('.'));
        String raw = last >= 0 && last + 1 < controlId.length() ? controlId.substring(last + 1) : controlId;
        return titleFromIdentifier(raw);
    }

    static boolean isNumericLabel(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static String titleFromIdentifier(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash + 1 < normalized.length()) {
            normalized = normalized.substring(lastSlash + 1);
        }
        StringBuilder builder = new StringBuilder();
        boolean newWord = true;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch == '-' || ch == '_' || ch == '.' || Character.isWhitespace(ch)) {
                if (!newWord && builder.length() > 0) {
                    builder.append(' ');
                }
                newWord = true;
                continue;
            }
            if (Character.isUpperCase(ch) && i > 0 && !newWord) {
                builder.append(' ');
            }
            builder.append(newWord ? Character.toUpperCase(ch) : ch);
            newWord = false;
        }
        return builder.toString().trim();
    }

    static String cardFromId(String controlId) {
        if (controlId == null || controlId.isBlank()) {
            return "";
        }
        int first = controlId.indexOf('/');
        if (first < 0 || first + 1 >= controlId.length()) {
            return "";
        }
        int second = controlId.indexOf('/', first + 1);
        if (second < 0 || second <= first + 1) {
            return "";
        }
        return controlId.substring(first + 1, second).trim();
    }
}
