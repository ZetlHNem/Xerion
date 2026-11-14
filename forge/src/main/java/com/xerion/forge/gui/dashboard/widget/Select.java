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

import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.gui.dashboard.UiColor;
import com.xerion.forge.gui.dashboard.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphics;

public final class Select {
    private Select() {
    }

    public static void renderField(GuiGraphics graphics, int x, int y, int w, int h, int field, int border,
                                   int arrowColor, float alpha) {
        ShapeRenderer.roundRect(graphics, x, y, w, h, 6, field);
        ShapeRenderer.roundRectBorder(graphics, x, y, w, h, 6, 1, border);
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_ANGLE_DOWN, x + w - 16,
                y + (h - 8) * 0.5f, 8.0f, UiColor.alpha(arrowColor, alpha));
    }

    public static void renderOptionBackground(GuiGraphics graphics, int x, int y, int w, int h,
                                              int color, float active) {
        if (active <= 0.01f) {
            return;
        }
        ShapeRenderer.roundRect(graphics, x, y, w, h, 5, color);
    }

}
