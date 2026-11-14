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
import com.xerion.forge.gui.dashboard.ShapeRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;

public final class GearButton {
    private GearButton() {
    }

    public static void render(GuiGraphics graphics, int x, int y, int size, boolean active,
                              int fill, int border, int iconColor, float progress) {
        ShapeRenderer.roundRect(graphics, x, y, size, size, 6, fill);
        ShapeRenderer.roundRectBorder(graphics, x, y, size, size, 6, 1, border);
        float iconSize = 9.0f;
        float iconW = MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_GEAR, iconSize);
        float rotation = progress * 0.55f;
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(x + size * 0.5f, y + size * 0.5f, 0.0f);
            graphics.pose().mulPose(new Quaternionf().rotateZ(rotation));
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_GEAR,
                    -iconW * 0.5f, -iconSize * 0.5f, iconSize, iconColor);
        } finally {
            graphics.pose().popPose();
        }
    }
}
