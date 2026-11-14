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

public final class ToggleSwitch {
    private ToggleSwitch() {
    }

    public static void render(GuiGraphics graphics, int x, int y, float value, int trackColor) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        ShapeRenderer.roundRect(graphics, x, y + 4, 22, 10, 5, trackColor);
        ShapeRenderer.roundRect(graphics, Math.round(x + 2 + 12 * clamped), y + 5, 8, 8, 4, 0xFFFFFFFF);
    }
}
