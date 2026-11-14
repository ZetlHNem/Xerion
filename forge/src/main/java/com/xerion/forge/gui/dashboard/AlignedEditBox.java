package com.xerion.forge.gui.dashboard;

import com.xerion.forge.mixin.EditBoxAccessor;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public final class AlignedEditBox extends EditBox {
    private final int textOffset;

    public AlignedEditBox(Font font, int x, int y, int width, int height,
                          Component message, int textOffset) {
        super(font, x, y, width, height, message);
        this.textOffset = textOffset;
    }

    public AlignedEditBox(Font font, int x, int y, int width, int height,
                          @Nullable EditBox previous, Component message, int textOffset) {
        super(font, x, y, width, height, previous, message);
        this.textOffset = textOffset;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!isVisible()) {
            return;
        }
        EditBoxAccessor accessor = (EditBoxAccessor) (Object) this;
        String value = getValue();
        int displayPos = accessor.xerion$getDisplayPos();
        int cursor = accessor.xerion$getCursorPos() - displayPos;
        int highlight = accessor.xerion$getHighlightPos() - displayPos;
        String visible = value.substring(Math.min(displayPos, value.length()));
        while (!visible.isEmpty() && MSDFRenderer.width(visible, 9.0f) > getInnerWidth()) {
            visible = visible.substring(0, Character.offsetByCodePoints(visible, visible.length(), -1));
        }
        cursor = Math.max(0, Math.min(cursor, visible.length()));
        highlight = Math.max(0, Math.min(highlight, visible.length()));

        int textX = getX();
        int textY = getY() + textOffset;
        int baseColor = accessor.xerion$isEditable()
                ? accessor.xerion$getTextColor()
                : accessor.xerion$getTextColorUneditable();
        int textColor = UiColor.alpha(baseColor, alpha);
        boolean cursorVisible = isFocused() && accessor.xerion$getFrame() / 6 % 2 == 0;

        graphics.enableScissor(getX(), getY(), getX() + width, getY() + height);
        String beforeCursor = visible.substring(0, cursor);
        int cursorX = Math.round(textX + MSDFRenderer.width(beforeCursor, 9.0f));
        int highlightX = Math.round(textX
                + MSDFRenderer.width(visible.substring(0, highlight), 9.0f));
        if (highlight != cursor) {
            int left = Math.min(cursorX, highlightX);
            int right = Math.max(cursorX, highlightX);
            graphics.fill(RenderType.guiTextHighlight(), left, textY - 1,
                    right, textY + 10, UiColor.alpha(UiPalette.accent(), alpha * 0.28f));
        }

        if (!visible.isEmpty()) {
            MSDFRenderer.draw(graphics, visible, textX, textY, 9.0f, textColor);
        } else {
            Component hint = accessor.xerion$getHint();
            if (hint != null && !isFocused()) {
                MSDFRenderer.draw(graphics, hint.getString(), textX, textY, 9.0f,
                        UiColor.alpha(UiPalette.muted(), alpha));
            }
        }

        if (cursorVisible) {
            boolean insertCursor = accessor.xerion$getCursorPos() < value.length()
                    || value.length() >= accessor.xerion$getMaxLength();
            if (insertCursor) {
                graphics.fill(RenderType.guiOverlay(), cursorX, textY - 1,
                        cursorX + 1, textY + 10,
                        UiColor.alpha(UiPalette.accent(), alpha));
            } else {
                MSDFRenderer.draw(graphics, "_", cursorX, textY, 9.0f,
                        UiColor.alpha(UiPalette.accent(), alpha));
            }
        }
        graphics.disableScissor();
    }
}
