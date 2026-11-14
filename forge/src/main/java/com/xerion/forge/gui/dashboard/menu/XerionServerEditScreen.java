package com.xerion.forge.gui.dashboard;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

public final class XerionServerEditScreen extends XerionSetupScreen {
    private final Screen parent;
    private final BooleanConsumer callback;
    private final ServerData serverData;
    private final boolean direct;
    private AlignedEditBox nameBox;
    private AlignedEditBox ipBox;
    private XerionMenuActionButton saveButton;
    private XerionMenuActionButton resourceButton;

    public XerionServerEditScreen(Screen parent, BooleanConsumer callback,
                                  ServerData serverData, boolean direct) {
        super(Component.translatable(direct ? "selectServer.direct" : "addServer.title"));
        this.parent = parent;
        this.callback = callback;
        this.serverData = serverData;
        this.direct = direct;
    }

    @Override
    protected void init() {
        layoutPanel(direct ? 238 : 292, direct ? 270 : 320);
        int fieldX = panelX + 20;
        int fieldW = panelW - 40;
        int fieldY = panelY + HEADER_H + 16;

        if (!direct) {
            nameBox = createBox(fieldX, fieldY, fieldW, Component.translatable("addServer.enterName"),
                    serverData.name);
            addWidget(nameBox);
            fieldY += 52;
        }
        ipBox = createBox(fieldX, fieldY, fieldW, Component.translatable("addServer.enterIp"), serverData.ip);
        ipBox.setMaxLength(128);
        addWidget(ipBox);

        if (!direct) {
            int resourceY = fieldY + 52;
            resourceButton = addRenderableWidget(new XerionMenuActionButton(
                    fieldX, resourceY, fieldW, 26, serverData.getResourcePackStatus().getName().copy(),
                    FontAwesomeIcons.ICON_FA_BOX, XerionMenuActionButton.Style.NORMAL,
                    this::cycleResourcePack));
        }

        int buttonY = panelY + panelH - 40;
        int buttonW = Math.min(132, (panelW - 50) / 2);
        int right = panelX + panelW - 18;
        saveButton = addRenderableWidget(new XerionMenuActionButton(
                right - buttonW, buttonY, buttonW, 26,
                Component.translatable(direct ? "selectServer.select" : "addServer.add"),
                FontAwesomeIcons.ICON_FA_CHECK, XerionMenuActionButton.Style.PRIMARY,
                this::save));
        addRenderableWidget(new XerionMenuActionButton(
                right - buttonW * 2 - 6, buttonY, buttonW, 26,
                Component.translatable("gui.cancel"), FontAwesomeIcons.ICON_FA_XMARK,
                XerionMenuActionButton.Style.NORMAL, this::cancel));
        updateSaveState();
        setInitialFocus(direct ? ipBox : nameBox);
    }

    private AlignedEditBox createBox(int x, int y, int width, Component hint, String value) {
        AlignedEditBox box = new AlignedEditBox(font, x + 10, y, Math.max(40, width - 10), 28, hint, 8);
        box.setBordered(false);
        box.setTextColor(UiPalette.inputText());
        box.setTextColorUneditable(UiPalette.muted());
        box.setHint(hint);
        box.setValue(value == null ? "" : value);
        box.setResponder(ignored -> updateSaveState());
        return box;
    }

    private void cycleResourcePack() {
        ServerData.ServerPackStatus[] values = ServerData.ServerPackStatus.values();
        int next = (serverData.getResourcePackStatus().ordinal() + 1) % values.length;
        serverData.setResourcePackStatus(values[next]);
        resourceButton.setMessage(values[next].getName());
    }

    private void updateSaveState() {
        if (saveButton == null) {
            return;
        }
        String address = ipBox == null ? "" : ipBox.getValue();
        String name = direct || nameBox == null ? "Xerion" : nameBox.getValue();
        saveButton.active = ServerAddress.isValidAddress(address) && !name.isEmpty();
    }

    private void save() {
        serverData.ip = ipBox.getValue();
        if (!direct) {
            serverData.name = nameBox.getValue();
        }
        if (direct) {
            minecraft.options.lastMpIp = serverData.ip;
            minecraft.options.save();
        }
        callback.accept(true);
    }

    private void cancel() {
        callback.accept(false);
    }

    @Override
    public void tick() {
        if (nameBox != null) {
            nameBox.tick();
        }
        if (ipBox != null) {
            ipBox.tick();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = beginXerionRender(graphics);
        drawHeader(graphics, Component.empty(), alpha);
        int fieldX = panelX + 20;
        int fieldY = panelY + HEADER_H + 16;
        if (!direct) {
            drawField(graphics, fieldX, fieldY, panelW - 40,
                    Component.translatable("addServer.enterName"), nameBox, alpha);
            fieldY += 52;
        }
        drawField(graphics, fieldX, fieldY, panelW - 40,
                Component.translatable("addServer.enterIp"), ipBox, alpha);
        if (!direct) {
            int resourceY = fieldY + 52;
            ShapeRenderer.roundRect(graphics, fieldX, resourceY, panelW - 40, 26, 6,
                    UiColor.alpha(UiPalette.field(), alpha * 0.74f));
            resourceButton.setAlpha(alpha);
        }
        drawFooterDivider(graphics, alpha);
        if (nameBox != null) {
            nameBox.setAlpha(alpha);
            nameBox.render(graphics, mouseX, mouseY, partialTick);
        }
        ipBox.setAlpha(alpha);
        ipBox.render(graphics, mouseX, mouseY, partialTick);
        for (var renderable : renderables) {
            if (renderable instanceof XerionMenuActionButton button) {
                button.setAlpha(alpha);
                button.render(graphics, mouseX, mouseY, partialTick);
            }
        }
        finishXerionRender();
    }

    private void drawField(GuiGraphics graphics, int x, int y, int width, Component label,
                           AlignedEditBox box, float alpha) {
        MSDFRenderer.draw(graphics, label.getString(), x, y - 13, 7.0f,
                UiColor.alpha(UiPalette.muted(), alpha));
        int border = box.isFocused() ? UiPalette.accent() : UiPalette.border();
        ShapeRenderer.roundRect(graphics, x, y, width, 28, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.88f));
        ShapeRenderer.roundRectBorder(graphics, x, y, width, 28, 6, 1,
                UiColor.alpha(border, alpha));
    }

    @Override
    public void onClose() {
        callback.accept(false);
    }
}
