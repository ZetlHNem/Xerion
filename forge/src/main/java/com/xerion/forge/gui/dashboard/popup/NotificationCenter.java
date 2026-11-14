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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NotificationCenter {
    private static final int MAX_QUEUE = 24;
    private static final List<Entry> entries = new ArrayList<>();
    private static long nextId;

    private NotificationCenter() {
    }

    public static void success(String title, String message) {
        push(Kind.SUCCESS, title, message);
    }

    public static void info(String title, String message) {
        push(Kind.INFO, title, message);
    }

    public static void warning(String title, String message) {
        push(Kind.WARNING, title, message);
    }

    public static void error(String title, String message) {
        push(Kind.ERROR, title, message);
    }

    public static void module(String name, boolean enabled) {
        if (!VisualSettings.notificationsModuleToggles()) {
            return;
        }
        push(enabled ? Kind.SUCCESS : Kind.INFO, name, enabled ? "Enabled" : "Disabled");
    }

    public static void hotkey(String name, String value, String keyName) {
        if (!VisualSettings.notificationsEnabled() || !VisualSettings.notificationsLinkHotkeys()) {
            return;
        }
        String cleanValue = clean(value, "Triggered");
        String cleanKey = clean(keyName, "");
        String message = cleanKey.isBlank() || "-".equals(cleanKey)
                ? "Hotkey -> " + cleanValue
                : "Hotkey " + cleanKey + " -> " + cleanValue;
        push(Kind.INFO, clean(name, "Hotkey"), message);
    }

    public static List<Entry> entries() {
        return entries;
    }

    public static void trimExpired(long now) {
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.expired(now) && entry.alpha <= 0.02f) {
                iterator.remove();
            }
        }
    }

    private static void push(Kind kind, String title, String message) {
        if (!VisualSettings.notificationsEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        entries.add(new Entry(++nextId, kind, clean(title, "Notification"), clean(message, ""), now,
                Math.round(Math.max(1.0f, VisualSettings.notificationsDuration()) * 1000.0f), false));
        while (entries.size() > MAX_QUEUE) {
            entries.remove(0);
        }
    }

    static Entry preview(Kind kind, String title, String message) {
        return new Entry(Long.MIN_VALUE, kind, clean(title, "Notification"), clean(message, ""),
                System.currentTimeMillis(), 1L, true);
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    public enum Kind {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    public static final class Entry {
        public final long id;
        public final Kind kind;
        public final String title;
        public final String message;
        public final long createdAt;
        public final long durationMs;
        public final boolean preview;
        public float x;
        public float y;
        public float alpha;
        public boolean initialized;

        private Entry(long id, Kind kind, String title, String message, long createdAt, long durationMs, boolean preview) {
            this.id = id;
            this.kind = kind;
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
            this.durationMs = durationMs;
            this.preview = preview;
        }

        public boolean expired(long now) {
            if (preview) {
                return false;
            }
            return now - createdAt > durationMs;
        }

        public float progress(long now) {
            if (preview) {
                return 1.0f;
            }
            if (durationMs <= 0L) {
                return 0.0f;
            }
            return UiMath.clamp01(1.0f - (now - createdAt) / (float) durationMs);
        }
    }
}
