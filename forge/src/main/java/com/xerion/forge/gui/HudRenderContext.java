package com.xerion.forge.gui;

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

import com.xerion.common.render.Color;
import com.xerion.common.render.RenderContext;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class HudRenderContext implements RenderContext {
    private final GuiGraphics guiGraphics;
    private final float partialTick;

    public HudRenderContext(GuiGraphics guiGraphics, float partialTick) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
    }

    @Override
    public int screenWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @Override
    public int screenHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    @Override
    public float tickDelta() {
        return partialTick;
    }

    @Override
    public void drawText(String text, int x, int y, Color color, boolean shadow) {
        if (shadow) {
            MSDFRenderer.drawScreenTextWithShadow(guiGraphics, text, x, y, 9.0f, color.argb());
        } else {
            MSDFRenderer.draw(guiGraphics, text, x, y, 9.0f, color.argb());
        }
    }
}
