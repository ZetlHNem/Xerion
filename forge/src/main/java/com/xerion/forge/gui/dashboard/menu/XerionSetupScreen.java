package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

abstract class XerionSetupScreen extends Screen {
    protected static final int PANEL_MAX_W = 620;
    protected static final int PANEL_MARGIN = 16;
    protected static final int HEADER_H = 58;
    protected static final int FOOTER_H = 54;

    protected int panelX;
    protected int panelY;
    protected int panelW;
    protected int panelH;
    protected float screenAlpha;

    protected XerionSetupScreen(Component title) {
        super(title);
    }

    protected void layoutPanel(int minimumHeight, int maximumHeight) {
        panelW = Math.min(PANEL_MAX_W, Math.max(320, width - PANEL_MARGIN * 2));
        panelH = Math.min(maximumHeight, Math.max(minimumHeight, height - PANEL_MARGIN * 2));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
    }

    protected float beginXerionRender(GuiGraphics graphics) {
        AnimationClock.beginFrame();
        KawaseBlurPipeline.beginFrame();
        screenAlpha = UiMath.lerp(screenAlpha, 1.0f, UiAnimationTimings.PANEL_FADE);
        float alpha = UiMath.clamp01(screenAlpha);

        XerionTitleScreen.drawBackgroundImage(graphics, width, height, alpha);
        graphics.fill(0, 0, width, height, UiColor.alpha(0xFF070A10, alpha * 0.48f));
        graphics.flush();
        KawaseBlurPipeline.renderSurfaceBackdrop(graphics, width, height,
                panelX, panelY, panelW, panelH, 10.0f, alpha, 24.0f);
        ShadowRenderer.shadow(graphics, panelX, panelY, panelW, panelH, 10,
                ShadowStyle.popup(), alpha);
        ShapeRenderer.roundRect(graphics, panelX, panelY, panelW, panelH, 10,
                UiColor.alpha(UiPalette.shell(), alpha * 0.82f));
        ShapeRenderer.roundRectBorder(graphics, panelX, panelY, panelW, panelH,
                10, 1, UiColor.alpha(UiPalette.shellBorder(), alpha));
        return alpha;
    }

    protected void drawHeader(GuiGraphics graphics, Component subtitle, float alpha) {
        drawText(graphics, title.getString(), panelX + 20, panelY + 16,
                16.0f, UiPalette.text(), alpha);
        drawText(graphics, subtitle.getString(), panelX + 20, panelY + 39,
                7.5f, UiPalette.muted(), alpha);
        graphics.fill(panelX + 16, panelY + HEADER_H,
                panelX + panelW - 16, panelY + HEADER_H + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
    }

    protected void drawFooterDivider(GuiGraphics graphics, float alpha) {
        graphics.fill(panelX + 16, panelY + panelH - FOOTER_H,
                panelX + panelW - 16, panelY + panelH - FOOTER_H + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
    }

    protected void prepareWidgets(float alpha) {
        for (var renderable : renderables) {
            if (renderable instanceof AbstractWidget widget) {
                widget.setAlpha(alpha);
            }
        }
    }

    protected void finishXerionRender() {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected void drawText(GuiGraphics graphics, String value, float x, float y,
                            float size, int color, float alpha) {
        int resolved = UiColor.alpha(color, alpha);
        MSDFRenderer.draw(graphics, value, x, y, size, resolved);
    }

    protected float textWidth(String value, float size) {
        return MSDFRenderer.width(value, size);
    }
}
