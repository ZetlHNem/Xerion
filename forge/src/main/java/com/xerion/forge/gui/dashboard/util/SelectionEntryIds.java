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

import com.xerion.forge.config.VisualSettings;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

final class SelectionEntryIds {
    private SelectionEntryIds() {
    }

    static String normalize(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    static String join(Set<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return ids.stream()
                .map(SelectionEntryIds::normalize)
                .filter(id -> !id.isBlank())
                .distinct()
                .sorted()
                .reduce((left, right) -> left + "|" + right)
                .orElse("");
    }

    static Set<String> inventoryCleanerTrashSet() {
        Set<String> ids = new HashSet<>();
        String raw = VisualSettings.inventoryCleanerTrashItems();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        for (String part : raw.split("\\|")) {
            if (!part.isBlank()) {
                ids.add(part);
            }
        }
        return ids;
    }

    static String oreLabelFromId(String id) {
        String path = id == null ? "" : id;
        int colon = path.indexOf(':');
        if (colon >= 0 && colon + 1 < path.length()) {
            path = path.substring(colon + 1);
        }
        if (path.endsWith("_ore")) {
            path = path.substring(0, path.length() - 4);
        } else if (path.endsWith("ore") && path.length() > 3) {
            path = path.substring(0, path.length() - 3);
        }
        if (path.isBlank()) {
            return "Ore";
        }
        String[] parts = path.split("[_\\-/]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.isEmpty() ? "Ore" : builder.toString();
    }

    static String blockLabelFromId(String id) {
        String path = registryPath(id);
        if (path.isBlank()) {
            return "Unknown";
        }
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.isEmpty() ? path : builder.toString();
    }

    static String itemLabelFromId(String id) {
        return blockLabelFromId(id);
    }

    static String registryPath(String id) {
        String path = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        int colon = path.indexOf(':');
        if (colon >= 0 && colon + 1 < path.length()) {
            path = path.substring(colon + 1);
        }
        return path;
    }
}
