package com.xerion.forge.gui.dashboard;

import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

final class XerionMenuActionButton extends AbstractButton {
    enum Style {
        NORMAL,
        PRIMARY,
        DANGER
    }

    private final int icon;
    private final Style style;
    private final Runnable action;
    private float hoverProgress;

    XerionMenuActionButton(int x, int y, int width, int height, Component message,
                           int icon, Style style, Runnable action) {
        super(x, y, width, height, message);
        this.icon = icon;
        this.style = style;
        this.action = action;
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        hoverProgress = UiMath.lerp(hoverProgress,
                isHoveredOrFocused() && active ? 1.0f : 0.0f,
                UiAnimationTimings.CONTROL);
        float widgetAlpha = UiMath.clamp01(alpha) * (active ? 1.0f : 0.42f);
        int accent = style == Style.DANGER ? UiPalette.statusRed() : UiPalette.accent();
        int normalFill = UiColor.alpha(UiPalette.surface(), widgetAlpha * 0.78f);
        int activeFill = UiColor.alpha(accent,
                widgetAlpha * (style == Style.PRIMARY ? 0.94f : 0.72f));
        float mix = style == Style.PRIMARY
                ? 0.62f + hoverProgress * 0.38f
                : hoverProgress;

        ShapeRenderer.roundRect(graphics, getX(), getY(), getWidth(), getHeight(), 6,
                UiColor.lerp(normalFill, activeFill, mix));
        ShapeRenderer.roundRectBorder(graphics, getX(), getY(), getWidth(), getHeight(),
                6, 1, UiColor.alpha(UiColor.lerp(UiPalette.border(), accent, hoverProgress), widgetAlpha));
        drawContent(graphics, widgetAlpha);
    }

    private void drawContent(GuiGraphics graphics, float widgetAlpha) {
        String label = getMessage().getString();
        int color = style == Style.PRIMARY ? 0xFFFFFFFF : UiPalette.text();
        float iconSize = 8.5f;
        float textSize = 8.0f;
        float iconWidth = MSDFRenderer.iconWidth(icon, iconSize);
        float textWidth = MSDFRenderer.width(label, textSize);
        float startX = getX() + (getWidth() - iconWidth - 5.0f - textWidth) * 0.5f;
        MSDFRenderer.drawIconCodepoint(graphics, icon, startX,
                MSDFRenderer.iconCenteredY(icon, getY(), getHeight(), iconSize),
                iconSize, UiColor.alpha(color, widgetAlpha));
        MSDFRenderer.draw(graphics, label, startX + iconWidth + 5.0f,
                MSDFRenderer.textCenteredY(getY(), getHeight(), textSize),
                textSize, UiColor.alpha(color, widgetAlpha));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
