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

public final class Input {
    private Input() {
    }

    public static void renderField(GuiGraphics graphics, int x, int y, int w, int h, int field, int border) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, 6, field);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 6, 1, border);
    }

    public static void renderSmallField(GuiGraphics graphics, int x, int y, int w, int h, int field, int border) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, 5, field);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 5, 1, border);
    }
}
