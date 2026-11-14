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

public final class Button {
    private Button() {
    }

    public static void render(GuiGraphics graphics, int x, int y, int w, int h, int radius, int fill, int border) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, radius, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, radius, 1, border);
    }
}
