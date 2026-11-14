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

public final class Checkbox {
    private static final int SIZE = 18;

    private Checkbox() {
    }

    public static void render(GuiGraphics graphics, int x, int y, boolean checked, float progress,
                              int uncheckedFill, int accent, int border, float alpha) {
        float clamped = Math.max(0.0f, Math.min(1.0f, progress));
        ShapeRenderer.roundRect(graphics, x, y, SIZE, SIZE, 6, UiColor.lerp(uncheckedFill, accent, clamped));
        ShapeRenderer.roundRectBorder(graphics, x, y, SIZE, SIZE, 6, 1, border);
        if (!checked && clamped <= 0.02f) {
            return;
        }

        float iconSize = 10.0f;
        float iconW = MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_CHECK, iconSize);
        float iconX = x + SIZE * 0.5f - iconW * 0.5f;
        float iconY = y + SIZE * 0.5f - iconSize * 0.5f;
        int iconAlpha = Math.round(255.0f * clamped * alpha);
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_CHECK, iconX, iconY, iconSize,
                (iconAlpha << 24) | 0x00FFFFFF);
    }

}
