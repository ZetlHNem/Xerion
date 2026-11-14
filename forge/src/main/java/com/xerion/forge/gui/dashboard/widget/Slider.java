package com.xerion.forge.gui.dashboard.widget;

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

import com.xerion.forge.gui.dashboard.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphics;

public final class Slider {
    private Slider() {
    }

    public static void renderTrack(GuiGraphics graphics, int x, int y, int w, float value,
                                   int trackColor, int fillColor, int knobColor) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        ShapeRenderer.roundRect(graphics, x, y, w, 3, 2, trackColor);
        int filled = Math.round(w * clamped);
        ShapeRenderer.roundRect(graphics, x, y, filled, 3, 2, fillColor);
        ShapeRenderer.roundRect(graphics, x + filled - 6, y - 6, 13, 13, 7, 0xFFFFFFFF);
        ShapeRenderer.roundRect(graphics, x + filled - 4, y - 4, 9, 9, 5, knobColor);
    }

    public static void renderCompactTrack(GuiGraphics graphics, int x, int y, int w, float value,
                                          int trackColor, int fillColor, int knobColor) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        ShapeRenderer.roundRect(graphics, x, y, w, 3, 2, trackColor);
        int filled = Math.round(w * clamped);
        ShapeRenderer.roundRect(graphics, x, y, filled, 3, 2, fillColor);
        ShapeRenderer.roundRect(graphics, x + filled - 4, y - 3, 9, 9, 5, 0xFFFFFFFF);
        ShapeRenderer.roundRect(graphics, x + filled - 3, y - 2, 7, 7, 4, knobColor);
    }

    public static void renderValueField(GuiGraphics graphics, int x, int y, int w, int h, int field,
                                        int border, int radius) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, radius, field);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, radius, 1, border);
    }
}
