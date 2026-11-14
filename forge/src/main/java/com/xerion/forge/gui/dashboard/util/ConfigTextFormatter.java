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

import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.Minecraft;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

final class ConfigTextFormatter {
    private static final DateTimeFormatter CONFIG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MM.dd HH:mm").withZone(ZoneId.systemDefault());

    private ConfigTextFormatter() {
    }

    static String modified(long millis) {
        if (millis <= 0L) {
            return "Unknown";
        }
        return CONFIG_TIME_FORMAT.format(Instant.ofEpochMilli(millis));
    }

    static String author() {
        Minecraft minecraft = Minecraft.getInstance();
        try {
            if (minecraft != null && minecraft.getUser() != null) {
                return minecraft.getUser().getName();
            }
        } catch (RuntimeException ignored) {
        }
        return "Local";
    }

    static String ellipsize(String value, float size, int maxWidth) {
        if (value == null || value.isEmpty() || maxWidth <= 0 || MSDFRenderer.width(value, size) <= maxWidth) {
            return value == null ? "" : value;
        }
        String suffix = "...";
        int end = value.length();
        while (end > 0 && MSDFRenderer.width(value.substring(0, end) + suffix, size) > maxWidth) {
            end--;
        }
        return end <= 0 ? suffix : value.substring(0, end) + suffix;
    }
}
