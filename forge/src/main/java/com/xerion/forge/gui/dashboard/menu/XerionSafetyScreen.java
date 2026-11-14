package com.xerion.forge.gui.dashboard;

import com.xerion.forge.gui.dashboard.widget.Checkbox;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class XerionSafetyScreen extends XerionSetupScreen {
    private final Screen parent;
    private final List<String> messageLines = new ArrayList<>();
    private boolean stopShowing;
    private int checkX;
    private int checkY;
    private int checkW;
    private int checkH;

    public XerionSafetyScreen(Screen parent) {
        super(Component.translatable("multiplayerWarning.header"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        layoutPanel(248, 290);
        messageLines.clear();
        buildMessageLines(panelW - 44);
        checkX = panelX + 22;
        checkY = panelY + HEADER_H + 82;
        checkW = panelW - 44;
        checkH = 24;
        int right = panelX + panelW - 18;
        int buttonW = Math.min(132, (panelW - 50) / 2);
        addRenderableWidget(new XerionMenuActionButton(right - buttonW, panelY + panelH - 40,
                buttonW, 26, Component.translatable("gui.proceed"), FontAwesomeIcons.ICON_FA_CHECK,
                XerionMenuActionButton.Style.PRIMARY, this::proceed));
        addRenderableWidget(new XerionMenuActionButton(right - buttonW * 2 - 6, panelY + panelH - 40,
                buttonW, 26, Component.translatable("gui.back"), FontAwesomeIcons.ICON_FA_ARROW_LEFT,
                XerionMenuActionButton.Style.NORMAL, this::onClose));
    }

    private void buildMessageLines(int maxWidth) {
        String message = Component.translatable("multiplayerWarning.message").getString();
        StringBuilder line = new StringBuilder();
        for (int offset = 0; offset < message.length(); ) {
            int codePoint = message.codePointAt(offset);
            offset += Character.charCount(codePoint);
            String next = new String(Character.toChars(codePoint));
            String candidate = line + next;
            if (!line.isEmpty() && MSDFRenderer.width(candidate, 7.4f) > maxWidth) {
                messageLines.add(line.toString());
                line.setLength(0);
                line.append(next);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (!line.isEmpty()) {
            messageLines.add(line.toString());
        }
    }

    private void proceed() {
        if (stopShowing) {
            minecraft.options.skipMultiplayerWarning = true;
            minecraft.options.save();
        }
        minecraft.setScreen(new XerionMultiplayerScreen(parent));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = beginXerionRender(graphics);
        drawHeader(graphics, Component.translatable("multiplayerWarning.check"), alpha);
        int textY = panelY + HEADER_H + 12;
        for (String line : messageLines) {
            TextPainter.drawClipped(graphics, line, panelX + 22, textY,
                    panelW - 44, 7.4f, UiPalette.muted(), alpha);
            textY += 12;
        }
        float progress = stopShowing ? 1.0f : 0.0f;
        Checkbox.render(graphics, checkX, checkY, stopShowing, progress,
                UiPalette.field(), UiPalette.accent(), UiPalette.border(), alpha);
        MSDFRenderer.draw(graphics, Component.translatable("multiplayerWarning.check").getString(),
                checkX + 26, checkY + 4, 7.4f, UiColor.alpha(UiPalette.text(), alpha));
        drawFooterDivider(graphics, alpha);
        for (var renderable : renderables) {
            if (renderable instanceof XerionMenuActionButton button) {
                button.setAlpha(alpha);
                button.render(graphics, mouseX, mouseY, partialTick);
            }
        }
        finishXerionRender();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= checkX && mouseX <= checkX + checkW
                && mouseY >= checkY && mouseY <= checkY + checkH) {
            stopShowing = !stopShowing;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
