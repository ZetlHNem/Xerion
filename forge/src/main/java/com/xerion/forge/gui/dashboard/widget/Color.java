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

public final class Color {
    private Color() {
    }

    public static void renderButton(GuiGraphics graphics, int x, int y, int w, int h, int color, int border) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, 6, color);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 6, 1, border);
    }

    public static void renderPreview(GuiGraphics graphics, int x, int y, int w, int h, int color, int border) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, 6, color);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 6, 1, border);
    }

    public static void renderSwatch(GuiGraphics graphics, int x, int y, int w, int h, int color, int border) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, 4, color);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 4, 1, border);
    }
}
