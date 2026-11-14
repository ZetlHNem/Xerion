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

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;

import java.util.Locale;

public final class SpeedHudRenderer {
    private static final int W = 90;
    private static final int H = 42;
    private static final int PAD = 5;
    private static final int HISTORY_SIZE = 42;
    private static final int CHART_X = 6;
    private static final int CHART_Y = 19;
    private static final int CHART_W = W - 12;
    private static final int CHART_H = 18;
    private static final float ICON_SIZE = 6.6f;
    private static final float TITLE_SIZE = 5.2f;
    private static final float VALUE_SIZE = 7.0f;
    private static final float UNIT_SIZE = 4.9f;
    private static final float BLUR_STRENGTH = 16.0f;
    private static final float BG_ALPHA = 0.78f;
    private static final float[] history = new float[HISTORY_SIZE];
    private static float animatedX;
    private static float animatedY;
    private static float alpha;
    private static float displayedSpeed;
    private static float displayedMax = 8.0f;
    private static boolean animationInitialized;
    private static long lastSampleTime;

    private SpeedHudRenderer() {
    }

    public static void render(GuiGraphics graphics, boolean editMode) {
        boolean enabled = VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_SPEED);
        boolean visibleTarget = enabled && (Minecraft.getInstance().player != null || editMode);
        if (!visibleTarget && alpha <= 0.01f) {
            return;
        }
        clampToScreen();
        int targetX = VisualSettings.speedHudX();
        int targetY = VisualSettings.speedHudY();
        if (!animationInitialized) {
            animatedX = targetX;
            animatedY = targetY;
            displayedSpeed = currentSpeedBps(editMode);
            displayedMax = Math.max(8.0f, displayedSpeed * 1.35f);
            resetHistory(displayedSpeed);
            animationInitialized = true;
        }
        float currentSpeed = currentSpeedBps(editMode);
        sampleSpeed(currentSpeed);
        animatedX = UiMath.lerp(animatedX, targetX, UiAnimationTimings.HUD_MOVE);
        animatedY = UiMath.lerp(animatedY, targetY, UiAnimationTimings.HUD_MOVE);
        alpha = UiMath.lerp(alpha, visibleTarget ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        displayedSpeed = UiMath.lerp(displayedSpeed, currentSpeed, 0.30f);
        displayedMax = UiMath.lerp(displayedMax, targetMax(), 0.08f);
        float hudAlpha = UiMath.clamp01(alpha);
        if (hudAlpha <= 0.01f) {
            return;
        }

        int x = Math.round(animatedX);
        int y = Math.round(animatedY);
        Window window = Minecraft.getInstance().getWindow();
        if (VisualSettings.speedHudBlur()) {
            KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                    x, y, W, H, 6.0f, hudAlpha, BLUR_STRENGTH);
        }
        ShadowRenderer.shadow(graphics, x, y, W, H, 6, ShadowStyle.card(), hudAlpha * BG_ALPHA);
        ShapeRenderer.roundRect(graphics, x, y, W, H, 6,
                UiColor.alpha(UiPalette.surface(), hudAlpha * BG_ALPHA));
        if (editMode) {
            ShapeRenderer.roundRectBorder(graphics, x, y, W, H, 6, 1,
                    UiColor.alpha(UiPalette.accentSoft(), hudAlpha));
        }

        float iconX = x + PAD;
        float iconY = y + 6.0f;
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_GAUGE_HIGH, iconX, iconY, ICON_SIZE,
                UiColor.alpha(UiPalette.accent(), hudAlpha));
        float titleX = iconX + MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_GAUGE_HIGH, ICON_SIZE) + 4.0f;
        MSDFRenderer.draw(graphics, "Speed", titleX, y + 6.0f, TITLE_SIZE,
                UiColor.alpha(UiPalette.muted(), hudAlpha * 0.94f));

        if (VisualSettings.speedHudShowValue()) {
            String value = String.format(Locale.ROOT, "%.2f", Math.max(0.0f, displayedSpeed));
            String unit = "bps";
            float valueW = MSDFRenderer.width(value, VALUE_SIZE);
            float unitW = MSDFRenderer.width(unit, UNIT_SIZE);
            float unitX = x + W - PAD - unitW;
            float valueX = unitX - valueW - 4.0f;
            MSDFRenderer.draw(graphics, value, valueX, y + 4.8f, VALUE_SIZE,
                    UiColor.alpha(UiPalette.text(), hudAlpha));
            MSDFRenderer.draw(graphics, unit, unitX, y + 7.0f, UNIT_SIZE,
                    UiColor.alpha(UiPalette.muted(), hudAlpha * 0.95f));
        }

        if (VisualSettings.speedHudShowChart()) {
            drawChart(graphics, x + CHART_X, y + CHART_Y, CHART_W, CHART_H, hudAlpha);
        }
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_SPEED)) {
            return false;
        }
        int x = VisualSettings.speedHudX();
        int y = VisualSettings.speedHudY();
        return mouseX >= x && mouseX <= x + W && mouseY >= y && mouseY <= y + H;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Minecraft minecraft = Minecraft.getInstance();
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = (int) Math.round(mouseY - offsetY);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, W, H,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        VisualSettings.setSpeedHudPosition(snapped.x(), snapped.y());
        animatedX = snapped.x();
        animatedY = snapped.y();
        animationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.speedHudX();
    }

    public static double dragOffsetY(double mouseY) {
        return mouseY - VisualSettings.speedHudY();
    }

    public static int width() {
        return W;
    }

    public static int height() {
        return H;
    }

    private static float currentSpeedBps(boolean editMode) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return editMode ? 7.42f : 0.0f;
        }
        double dx = player.getX() - player.xOld;
        double dz = player.getZ() - player.zOld;
        return (float) (Math.sqrt(dx * dx + dz * dz) * 20.0D);
    }

    private static void resetHistory(float value) {
        for (int i = 0; i < history.length; i++) {
            history[i] = value;
        }
        lastSampleTime = 0L;
    }

    private static void sampleSpeed(float value) {
        long now = System.currentTimeMillis();
        if (lastSampleTime != 0L && now - lastSampleTime < 50L) {
            return;
        }
        lastSampleTime = now;
        System.arraycopy(history, 1, history, 0, history.length - 1);
        history[history.length - 1] = Math.max(0.0f, value);
    }

    private static float targetMax() {
        float max = 8.0f;
        for (float value : history) {
            max = Math.max(max, value * 1.25f);
        }
        return max;
    }

    private static void drawChart(GuiGraphics graphics, int x, int y, int w, int h, float alpha) {
        int track = UiColor.alpha(UiPalette.field(), alpha * 0.55f);
        int subtle = UiColor.alpha(UiPalette.border(), alpha * 0.30f);
        ShapeRenderer.roundRect(graphics, x, y, w, h, 4, track);
        graphics.fill(x + 4, y + h / 2, x + w - 4, y + h / 2 + 1, subtle);
        graphics.fill(x + 4, y + h - 3, x + w - 4, y + h - 2, subtle);

        float max = Math.max(1.0f, displayedMax);
        float[] pointsX = new float[history.length];
        float[] pointsY = new float[history.length];
        for (int i = 0; i < history.length; i++) {
            float t = i / (float) (history.length - 1);
            float value = UiMath.clamp01(history[i] / max);
            pointsX[i] = x + 3.0f + t * (w - 6.0f);
            pointsY[i] = y + h - 3.0f - value * (h - 6.0f);
        }

        int accent = UiColor.alpha(UiPalette.accent(), alpha);
        int fillTop = UiColor.alpha(UiPalette.accent(), alpha * 0.24f);
        int fillBottom = UiColor.alpha(UiPalette.accent(), alpha * 0.05f);
        drawArea(pointsX, pointsY, y + h - 3.0f, fillTop, fillBottom);
        drawPolyline(pointsX, pointsY, 1.1f, accent);

        int dotX = Math.round(pointsX[pointsX.length - 1]);
        int dotY = Math.round(pointsY[pointsY.length - 1]);
        ShapeRenderer.roundRect(graphics, dotX - 2, dotY - 2, 4, 4, 2,
                UiColor.alpha(UiPalette.text(), alpha));
    }

    private static void drawArea(float[] xs, float[] ys, float baseY, int topColor, int bottomColor) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        try {
            for (int i = 0; i < xs.length; i++) {
                vertex(buffer, xs[i], ys[i], topColor);
                vertex(buffer, xs[i], baseY, bottomColor);
            }
            Tesselator.getInstance().end();
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private static void drawPolyline(float[] xs, float[] ys, float thickness, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        try {
            for (int i = 1; i < xs.length; i++) {
                addThickSegment(buffer, xs[i - 1], ys[i - 1], xs[i], ys[i], thickness, color);
            }
            Tesselator.getInstance().end();
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private static void addThickSegment(BufferBuilder buffer, float x1, float y1, float x2, float y2,
                                        float thickness, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len <= 0.001f) {
            return;
        }
        float nx = -dy / len * thickness * 0.5f;
        float ny = dx / len * thickness * 0.5f;
        vertex(buffer, x1 + nx, y1 + ny, color);
        vertex(buffer, x1 - nx, y1 - ny, color);
        vertex(buffer, x2 - nx, y2 - ny, color);
        vertex(buffer, x2 + nx, y2 + ny, color);
    }

    private static void vertex(BufferBuilder buffer, float x, float y, int color) {
        float alpha = ((color >>> 24) & 0xFF) / 255.0f;
        float red = ((color >>> 16) & 0xFF) / 255.0f;
        float green = ((color >>> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        buffer.vertex(x, y, 0.0f).color(red, green, blue, alpha).endVertex();
    }

    private static void clampToScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int x = UiMath.clamp(VisualSettings.speedHudX(), 0, Math.max(0, screenW - W));
        int y = UiMath.clamp(VisualSettings.speedHudY(), 0, Math.max(0, screenH - H));
        if (x != VisualSettings.speedHudX() || y != VisualSettings.speedHudY()) {
            VisualSettings.setSpeedHudPosition(x, y);
        }
    }
}
