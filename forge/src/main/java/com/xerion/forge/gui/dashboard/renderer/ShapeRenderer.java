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

import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

public final class ShapeRenderer {
    private static final int ARC_SEGMENTS = 24;
    private static final float FEATHER = 1.25f;
    private static float globalAlpha = 1.0f;

    private ShapeRenderer() {
    }

    static void roundRect(GuiGraphics graphics, DashboardLayout.Rect rect, int color) {
        roundRect(graphics, rect.x(), rect.y(), rect.width(), rect.height(), color);
    }

    public static void roundRect(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        roundRect(graphics, x, y, w, h, UiTheme.CARD_RADIUS, color);
    }

    public static void roundRect(GuiGraphics graphics, int x, int y, int w, int h, int radius, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        color = applyGlobalAlpha(color);

        int clampedRadius = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (SdfShapeRenderer.roundRect(graphics, x, y, w, h, clampedRadius, color)) {
            return;
        }

        if (clampedRadius == 0) {
            graphics.fill(x, y, x + w, y + h, color);
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        float alpha = ((color >>> 24) & 0xFF) / 255.0f;
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        float centerX = x + w / 2.0f;
        float centerY = y + h / 2.0f;

        try {
            vertex(buffer, centerX, centerY, red, green, blue, alpha);
            roundedRectPath(buffer, x, y, w, h, clampedRadius, red, green, blue, alpha);

            Tesselator.getInstance().end();

            BufferBuilder featherBuffer = Tesselator.getInstance().getBuilder();
            featherBuffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            roundedRectFeatherPath(featherBuffer, x, y, w, h, clampedRadius, FEATHER, red, green, blue, alpha * 0.45f);
            Tesselator.getInstance().end();
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    public static void roundRectBorder(GuiGraphics graphics, int x, int y, int w, int h, int radius, int strokeWidth,
                                int color) {
        if (w <= 0 || h <= 0 || strokeWidth <= 0) {
            return;
        }
        color = applyGlobalAlpha(color);

        int clampedRadius = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (SdfShapeRenderer.roundRectBorder(graphics, x, y, w, h, clampedRadius, strokeWidth, color)) {
            return;
        }

        outline(graphics, x, y, w, h, color);
    }

    private static void roundedRectPath(BufferBuilder buffer, int x, int y, int w, int h, int radius,
                                        float red, float green, float blue, float alpha) {
        arc(buffer, x + w - radius, y + radius, radius, -90, 0, red, green, blue, alpha, false);
        arc(buffer, x + w - radius, y + h - radius, radius, 0, 90, red, green, blue, alpha, true);
        arc(buffer, x + radius, y + h - radius, radius, 90, 180, red, green, blue, alpha, true);
        arc(buffer, x + radius, y + radius, radius, 180, 270, red, green, blue, alpha, true);
        vertex(buffer, x + w - radius, y, red, green, blue, alpha);
    }

    private static void roundedRectFeatherPath(BufferBuilder buffer, int x, int y, int w, int h, int radius, float feather,
                                               float red, float green, float blue, float alpha) {
        featherArc(buffer, x + w - radius, y + radius, radius, feather, -90, 0, red, green, blue, alpha, false);
        featherArc(buffer, x + w - radius, y + h - radius, radius, feather, 0, 90, red, green, blue, alpha, true);
        featherArc(buffer, x + radius, y + h - radius, radius, feather, 90, 180, red, green, blue, alpha, true);
        featherArc(buffer, x + radius, y + radius, radius, feather, 180, 270, red, green, blue, alpha, true);
        featherPair(buffer, x + w - radius, y, x + w - radius, y - feather, red, green, blue, alpha);
    }

    private static void arc(BufferBuilder buffer, float centerX, float centerY, float radius,
                            int startDegrees, int endDegrees,
                            float red, float green, float blue, float alpha, boolean skipFirst) {
        int delta = endDegrees - startDegrees;
        int steps = Math.max(1, Math.abs(delta) * ARC_SEGMENTS / 90);
        int start = skipFirst ? 1 : 0;
        for (int i = start; i <= steps; i++) {
            float angle = (float) Math.toRadians(startDegrees + delta * (i / (float) steps));
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius;
            vertex(buffer, x, y, red, green, blue, alpha);
        }
    }

    private static void featherArc(BufferBuilder buffer, float centerX, float centerY, float radius, float feather,
                                   int startDegrees, int endDegrees,
                                   float red, float green, float blue, float alpha, boolean skipFirst) {
        int delta = endDegrees - startDegrees;
        int steps = Math.max(1, Math.abs(delta) * ARC_SEGMENTS / 90);
        int start = skipFirst ? 1 : 0;
        for (int i = start; i <= steps; i++) {
            float angle = (float) Math.toRadians(startDegrees + delta * (i / (float) steps));
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float innerX = centerX + cos * radius;
            float innerY = centerY + sin * radius;
            float outerX = centerX + cos * (radius + feather);
            float outerY = centerY + sin * (radius + feather);
            featherPair(buffer, innerX, innerY, outerX, outerY, red, green, blue, alpha);
        }
    }

    private static void featherPair(BufferBuilder buffer, float innerX, float innerY, float outerX, float outerY,
                                    float red, float green, float blue, float alpha) {
        vertex(buffer, innerX, innerY, red, green, blue, alpha);
        vertex(buffer, outerX, outerY, red, green, blue, 0.0f);
    }

    private static void vertex(BufferBuilder buffer, float x, float y, float red, float green, float blue, float alpha) {
        buffer.vertex(x, y, 0.0f).color(red, green, blue, alpha).endVertex();
    }

    static void outline(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        color = applyGlobalAlpha(color);
        graphics.hLine(x + 5, x + w - 5, y, color);
        graphics.hLine(x + 5, x + w - 5, y + h, color);
        graphics.vLine(x, y + 5, y + h - 5, color);
        graphics.vLine(x + w, y + 5, y + h - 5, color);
    }

    static void text(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        MSDFRenderer.draw(graphics, text, x, y, color);
    }

    static void switchControl(GuiGraphics graphics, int x, int y, boolean enabled) {
        int track = enabled ? UiTheme.PRIMARY : UiTheme.SURFACE_CONTAINER_HIGHEST;
        int knob = enabled ? UiTheme.ON_PRIMARY : UiTheme.TEXT_MUTED;
        roundRect(graphics, x, y, 28, 16, track);
        outline(graphics, x, y, 28, 16, enabled ? UiTheme.PRIMARY : UiTheme.OUTLINE_SOFT);
        int knobX = enabled ? x + 14 : x + 4;
        roundRect(graphics, knobX, y + 4, 8, 8, knob);
    }

    static void setGlobalAlpha(float alpha) {
        globalAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
    }

    static void resetGlobalAlpha() {
        globalAlpha = 1.0f;
    }

    private static int applyGlobalAlpha(int color) {
        if (globalAlpha >= 0.999f) {
            return color;
        }
        int alpha = Math.round(((color >>> 24) & 0xFF) * globalAlpha);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
