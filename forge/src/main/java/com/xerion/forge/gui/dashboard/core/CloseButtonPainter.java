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

import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.gui.GuiGraphics;

final class CloseButtonPainter {
    private CloseButtonPainter() {
    }

    static void render(GuiGraphics graphics, int x, int y, int size, float hover, float active, float alpha) {
        int danger = UiPalette.danger();
        int normalIcon = UiPalette.dark() ? 0xFFFF6B78 : 0xFFE05A64;
        int activeIcon = UiPalette.dark() ? 0xFFFF4455 : 0xFFC82434;
        int fill = UiColor.lerp(0x00000000, UiColor.alpha(danger, 0.14f), hover);
        fill = UiColor.lerp(fill, UiColor.alpha(danger, 0.23f), active);
        int border = UiColor.lerp(0x00000000, UiColor.alpha(danger, 0.28f), hover);
        border = UiColor.lerp(border, UiColor.alpha(danger, 0.43f), active);
        int iconColor = UiColor.lerp(normalIcon, danger, hover);
        iconColor = UiColor.lerp(iconColor, activeIcon, active);

        ShapeRenderer.roundRect(graphics, x, y, size, size, 6, UiColor.alpha(fill, alpha));
        ShapeRenderer.roundRectBorder(graphics, x, y, size, size, 6, 1, UiColor.alpha(border, alpha));
        float iconSize = Math.max(9.0f, size * 0.46f);
        float iconW = MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_XMARK, iconSize);
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_XMARK,
                x + (size - iconW) * 0.5f,
                y + (size - iconSize) * 0.5f,
                iconSize, UiColor.alpha(iconColor, alpha));
    }
}
