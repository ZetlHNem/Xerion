package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.ServerListOnlineEntryAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public final class XerionMultiplayerScreen extends JoinMultiplayerScreen {
    private static final int PANEL_MAX_W = 620;
    private static final int PANEL_MAX_H = 330;
    private static final int PANEL_MARGIN = 16;
    private static final int HEADER_H = 64;
    private static final int FOOTER_H = 86;
    private static final int ROW_H = 42;

    private final Screen parent;
    private final java.util.List<XerionActionButton> actionButtons = new java.util.ArrayList<>();
    private XerionServerList themedList;
    private XerionActionButton joinButton;
    private XerionActionButton editButton;
    private XerionActionButton deleteButton;
    private ServerData editingServer;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private float screenAlpha;

    public XerionMultiplayerScreen(Screen parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        actionButtons.clear();
        panelW = Math.min(PANEL_MAX_W, Math.max(300, width - PANEL_MARGIN * 2));
        panelH = Math.min(PANEL_MAX_H, Math.max(260, height - PANEL_MARGIN * 2));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        int listTop = panelY + HEADER_H;
        int listBottom = panelY + panelH - FOOTER_H;
        int rowWidth = Math.max(270, panelW - 40);

        if (!(serverSelectionList instanceof XerionServerList)) {
            serverSelectionList.children().forEach(ServerSelectionList.Entry::close);
            themedList = new XerionServerList(this, minecraft, width, height,
                    listTop, listBottom, ROW_H, rowWidth);
            themedList.updateOnlineServers(getServers());
            serverSelectionList = themedList;
        } else {
            themedList = (XerionServerList) serverSelectionList;
            themedList.updateLayout(width, height, listTop, listBottom, rowWidth);
        }

        clearWidgets();
        addWidget(themedList);
        buildActionButtons();
        onSelectedChange();
        setInitialFocus(themedList);
    }

    private void buildActionButtons() {
        int left = panelX + 18;
        int right = panelX + panelW - 18;
        int gap = 6;
        int topY = panelY + panelH - 72;
        int topW = (right - left - gap * 3) / 4;
        int x = left;
        joinButton = addActionButton(x, topY, topW, 26,
                Component.translatable("selectServer.select"), FontAwesomeIcons.ICON_FA_PLAY,
                ButtonStyle.PRIMARY, this::joinSelectedServer);
        x += topW + gap;
        addActionButton(x, topY, topW, 26, Component.translatable("selectServer.direct"),
                FontAwesomeIcons.ICON_FA_PLUG, ButtonStyle.NORMAL, this::openDirectConnect);
        x += topW + gap;
        addActionButton(x, topY, topW, 26, Component.translatable("selectServer.add"),
                FontAwesomeIcons.ICON_FA_PLUS, ButtonStyle.NORMAL, this::openAddServer);
        x += topW + gap;
        addActionButton(x, topY, right - x, 26, Component.translatable("selectServer.refresh"),
                FontAwesomeIcons.ICON_FA_ARROWS_ROTATE, ButtonStyle.NORMAL, this::refreshServers);

        int bottomY = panelY + panelH - 40;
        int bottomW = (right - left - gap * 2) / 3;
        x = left;
        editButton = addActionButton(x, bottomY, bottomW, 26, Component.translatable("selectServer.edit"),
                FontAwesomeIcons.ICON_FA_PENCIL, ButtonStyle.NORMAL, this::openEditServer);
        x += bottomW + gap;
        deleteButton = addActionButton(x, bottomY, bottomW, 26, Component.translatable("selectServer.delete"),
                FontAwesomeIcons.ICON_FA_TRASH_CAN, ButtonStyle.DANGER, this::confirmDeleteServer);
        x += bottomW + gap;
        addActionButton(x, bottomY, right - x, 26, CommonComponents.GUI_BACK,
                FontAwesomeIcons.ICON_FA_ARROW_LEFT, ButtonStyle.NORMAL, this::onClose);
    }

    private XerionActionButton addActionButton(int x, int y, int width, int height,
                                                Component label, int icon, ButtonStyle style,
                                                Runnable action) {
        XerionActionButton button = new XerionActionButton(x, y, width, height, label, icon, style, action);
        actionButtons.add(button);
        return addRenderableWidget(button);
    }

    private void openDirectConnect() {
        editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
        minecraft.setScreen(new XerionServerEditScreen(this, accepted -> {
            if (!accepted) {
                minecraft.setScreen(this);
                return;
            }
            ServerData known = getServers().get(editingServer.ip);
            if (known == null) {
                getServers().add(editingServer, true);
                getServers().save();
                connect(editingServer);
            } else {
                connect(known);
            }
        }, editingServer, true));
    }

    private void openAddServer() {
        editingServer = new ServerData(I18n.get("selectServer.defaultName"), "", false);
        minecraft.setScreen(new XerionServerEditScreen(this, accepted -> {
            if (accepted) {
                ServerData known = getServers().unhide(editingServer.ip);
                if (known != null) {
                    known.copyNameIconFrom(editingServer);
                } else {
                    getServers().add(editingServer, false);
                }
                getServers().save();
                themedList.setSelected(null);
                themedList.updateOnlineServers(getServers());
            }
            minecraft.setScreen(this);
        }, editingServer, false));
    }

    private void openEditServer() {
        selectedOnlineServer().ifPresent(server -> {
            editingServer = new ServerData(server.name, server.ip, false);
            editingServer.copyFrom(server);
            minecraft.setScreen(new XerionServerEditScreen(this, accepted -> {
                if (accepted) {
                    server.name = editingServer.name;
                    server.ip = editingServer.ip;
                    server.copyFrom(editingServer);
                    getServers().save();
                    themedList.updateOnlineServers(getServers());
                }
                minecraft.setScreen(this);
            }, editingServer, false));
        });
    }

    private void confirmDeleteServer() {
        selectedOnlineServer().ifPresent(server -> minecraft.setScreen(new ConfirmScreen(accepted -> {
            if (accepted) {
                getServers().remove(server);
                getServers().save();
                themedList.setSelected(null);
                themedList.updateOnlineServers(getServers());
            }
            minecraft.setScreen(this);
        }, Component.translatable("selectServer.deleteQuestion"),
                Component.translatable("selectServer.deleteWarning", server.name),
                Component.translatable("selectServer.deleteButton"), CommonComponents.GUI_CANCEL)));
    }

    private Optional<ServerData> selectedOnlineServer() {
        ServerSelectionList.Entry selected = themedList == null ? null : themedList.getSelected();
        if (selected instanceof ServerSelectionList.OnlineServerEntry online) {
            return Optional.of(online.getServerData());
        }
        return Optional.empty();
    }

    private void refreshServers() {
        minecraft.setScreen(new XerionMultiplayerScreen(parent));
    }

    private void connect(ServerData server) {
        ConnectScreen.startConnecting(this, minecraft, ServerAddress.parseString(server.ip), server, false);
    }

    @Override
    public void onClose() {
        Screen target = parent;
        if (target == null || target instanceof GenericDirtMessageScreen
                || target instanceof XerionLoadingScreen) {
            target = new XerionTitleScreen();
        }
        minecraft.setScreen(target);
    }

    @Override
    protected void onSelectedChange() {
        if (joinButton == null || themedList == null) {
            return;
        }
        ServerSelectionList.Entry selected = themedList.getSelected();
        joinButton.active = selected != null && !(selected instanceof ServerSelectionList.LANHeader);
        boolean editable = selected instanceof ServerSelectionList.OnlineServerEntry;
        editButton.active = editable;
        deleteButton.active = editable;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        AnimationClock.beginFrame();
        KawaseBlurPipeline.beginFrame();
        screenAlpha = UiMath.lerp(screenAlpha, 1.0f, UiAnimationTimings.PANEL_FADE);
        float alpha = UiMath.clamp01(screenAlpha);
        XerionTitleScreen.drawBackgroundImage(graphics, width, height, alpha);
        graphics.fill(0, 0, width, height, UiColor.alpha(0xFF070A10, alpha * 0.48f));
        graphics.flush();
        KawaseBlurPipeline.renderSurfaceBackdrop(graphics, width, height,
                panelX, panelY, panelW, panelH, 10.0f, alpha, 24.0f);
        ShadowRenderer.shadow(graphics, panelX, panelY, panelW, panelH, 10, ShadowStyle.popup(), alpha);
        ShapeRenderer.roundRect(graphics, panelX, panelY, panelW, panelH, 10,
                UiColor.alpha(UiPalette.shell(), alpha * 0.82f));
        ShapeRenderer.roundRectBorder(graphics, panelX, panelY, panelW, panelH, 10, 1,
                UiColor.alpha(UiPalette.shellBorder(), alpha));
        drawHeader(graphics, alpha);
        themedList.setRenderAlpha(alpha);
        themedList.render(graphics, mouseX, mouseY, partialTick);
        for (XerionActionButton button : actionButtons) {
            button.setAlpha(alpha);
            button.render(graphics, mouseX, mouseY, partialTick);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawHeader(GuiGraphics graphics, float alpha) {
        int titleX = panelX + 20;
        MSDFRenderer.draw(graphics, Component.translatable("multiplayer.title").getString(),
                titleX, panelY + 17, 16.0f, UiColor.alpha(UiPalette.text(), alpha));
        MSDFRenderer.draw(graphics, Component.translatable("xerion.multiplayer.servers").getString(),
                titleX, panelY + 40, 7.5f, UiColor.alpha(UiPalette.muted(), alpha));
        graphics.fill(panelX + 16, panelY + HEADER_H - 1, panelX + panelW - 16, panelY + HEADER_H,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
        graphics.fill(panelX + 16, themedList.listBottom(), panelX + panelW - 16, themedList.listBottom() + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
    }

    private enum ButtonStyle { NORMAL, PRIMARY, DANGER }

    private static final class XerionActionButton extends AbstractButton {
        private final int icon;
        private final ButtonStyle style;
        private final Runnable action;
        private float hoverProgress;

        private XerionActionButton(int x, int y, int width, int height, Component message,
                                   int icon, ButtonStyle style, Runnable action) {
            super(x, y, width, height, message);
            this.icon = icon;
            this.style = style;
            this.action = action;
        }

        @Override
        public void onPress() { action.run(); }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            hoverProgress = UiMath.lerp(hoverProgress,
                    isHoveredOrFocused() && active ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
            float widgetAlpha = UiMath.clamp01(alpha) * (active ? 1.0f : 0.42f);
            int accent = style == ButtonStyle.DANGER ? UiPalette.statusRed() : UiPalette.accent();
            int normalFill = UiColor.alpha(UiPalette.surface(), widgetAlpha * 0.78f);
            int activeFill = UiColor.alpha(accent, widgetAlpha * (style == ButtonStyle.PRIMARY ? 0.94f : 0.72f));
            float mix = style == ButtonStyle.PRIMARY ? 0.62f + hoverProgress * 0.38f : hoverProgress;
            ShapeRenderer.roundRect(graphics, getX(), getY(), getWidth(), getHeight(), 6,
                    UiColor.lerp(normalFill, activeFill, mix));
            ShapeRenderer.roundRectBorder(graphics, getX(), getY(), getWidth(), getHeight(), 6, 1,
                    UiColor.alpha(UiColor.lerp(UiPalette.border(), accent, hoverProgress), widgetAlpha));
            String label = getMessage().getString();
            float iconSize = 8.5f;
            float textSize = 8.0f;
            float iconW = MSDFRenderer.iconWidth(icon, iconSize);
            float textW = MSDFRenderer.width(label, textSize);
            float groupW = iconW + 5.0f + textW;
            float iconX = getX() + (getWidth() - groupW) * 0.5f;
            int color = style == ButtonStyle.PRIMARY ? 0xFFFFFFFF : UiPalette.text();
            MSDFRenderer.drawIconCodepoint(graphics, icon, iconX,
                    MSDFRenderer.iconCenteredY(icon, getY(), getHeight(), iconSize), iconSize,
                    UiColor.alpha(color, widgetAlpha));
            MSDFRenderer.draw(graphics, label, iconX + iconW + 5.0f,
                    MSDFRenderer.textCenteredY(getY(), getHeight(), textSize), textSize,
                    UiColor.alpha(color, widgetAlpha));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static final class XerionServerList extends ServerSelectionList {
        private static final int OFFSCREEN_X = -10_000;
        private final Minecraft minecraft;
        private final Map<Entry, Float> hoverAnimations = new IdentityHashMap<>();
        private int screenWidth;
        private int rowWidth;
        private float renderAlpha = 1.0f;
        private float scrollbarHover;
        private boolean suppressVanillaScrollbar;

        private XerionServerList(JoinMultiplayerScreen screen, Minecraft minecraft, int width, int height,
                                 int top, int bottom, int itemHeight, int rowWidth) {
            super(screen, minecraft, width, height, top, bottom, itemHeight);
            this.minecraft = minecraft;
            this.screenWidth = width;
            this.rowWidth = rowWidth;
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            setRenderSelection(false);
        }

        private void updateLayout(int width, int height, int top, int bottom, int rowWidth) {
            this.screenWidth = width;
            this.rowWidth = rowWidth;
            updateSize(width, height, top, bottom);
        }

        private void setRenderAlpha(float alpha) { renderAlpha = alpha; }
        private int listBottom() { return y1; }

        @Override public int getRowWidth() { return rowWidth; }

        @Override
        protected int getScrollbarPosition() {
            return suppressVanillaScrollbar ? screenWidth + 32 : screenWidth / 2 + rowWidth / 2 + 12;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            suppressVanillaScrollbar = true;
            try { super.render(graphics, mouseX, mouseY, partialTick); }
            finally { suppressVanillaScrollbar = false; }
            renderThemedScrollbar(graphics, mouseX, mouseY);
        }

        private void renderThemedScrollbar(GuiGraphics graphics, int mouseX, int mouseY) {
            int maxScroll = getMaxScroll();
            if (maxScroll <= 0) {
                scrollbarHover = UiMath.lerp(scrollbarHover, 0.0f, UiAnimationTimings.CONTROL);
                return;
            }
            int scrollbarX = getScrollbarPosition();
            int viewportH = y1 - y0;
            int thumbH = UiMath.clamp((int) ((float) viewportH * viewportH / getMaxPosition()), 32, viewportH - 8);
            int thumbY = (int) getScrollAmount() * (viewportH - thumbH) / maxScroll + y0;
            thumbY = Math.max(y0, thumbY);
            boolean hovered = mouseX >= scrollbarX - 2 && mouseX <= scrollbarX + 8
                    && mouseY >= y0 && mouseY <= y1;
            scrollbarHover = UiMath.lerp(scrollbarHover, hovered ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
            ShapeRenderer.roundRect(graphics, scrollbarX + 2, y0 + 2, 2, viewportH - 4, 1,
                    UiColor.alpha(UiPalette.track(), renderAlpha * 0.72f));
            ShapeRenderer.roundRect(graphics, scrollbarX, thumbY, 5, thumbH, 2,
                    UiColor.alpha(UiColor.lerp(UiPalette.accentSoft(), UiPalette.accent(), scrollbarHover),
                            renderAlpha * (0.82f + scrollbarHover * 0.18f)));
        }

        @Override
        protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                  int index, int rowLeft, int rowTop, int rowWidth, int rowHeight) {
            Entry entry = getEntry(index);
            if (entry instanceof OnlineServerEntry) {
                entry.render(graphics, index, rowTop, OFFSCREEN_X, rowWidth, rowHeight,
                        OFFSCREEN_X, OFFSCREEN_X, false, partialTick);
            }
            int cardH = Math.max(34, rowHeight);
            boolean hovered = mouseX >= rowLeft - 5 && mouseX <= rowLeft + rowWidth + 5
                    && mouseY >= rowTop && mouseY <= rowTop + cardH;
            float hover = UiMath.lerp(hoverAnimations.getOrDefault(entry, 0.0f),
                    hovered ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
            hoverAnimations.put(entry, hover);
            boolean selected = getSelected() == entry;
            int fill = UiColor.lerp(UiColor.alpha(UiPalette.surface(), renderAlpha * 0.48f),
                    UiColor.alpha(UiPalette.accentSoft(), renderAlpha * 0.72f),
                    selected ? 0.72f : hover * 0.34f);
            int border = selected ? UiPalette.accent()
                    : UiColor.lerp(UiPalette.border(), UiPalette.accent(), hover * 0.48f);
            ShapeRenderer.roundRect(graphics, rowLeft - 5, rowTop, rowWidth + 10, cardH, 6, fill);
            ShapeRenderer.roundRectBorder(graphics, rowLeft - 5, rowTop, rowWidth + 10, cardH, 6, 1,
                    UiColor.alpha(border, renderAlpha));
            if (selected) {
                ShapeRenderer.roundRect(graphics, rowLeft - 5, rowTop + 5, 3, Math.max(1, cardH - 10), 2,
                        UiColor.alpha(UiPalette.accent(), renderAlpha));
            }
            if (entry instanceof OnlineServerEntry online) {
                renderOnlineServer(graphics, online, rowLeft, rowTop, rowWidth, cardH, hover, selected);
            } else if (entry instanceof NetworkServerEntry network) {
                renderLanServer(graphics, network.getServerData(), rowLeft, rowTop, rowWidth, cardH);
            } else if (entry instanceof LANHeader) {
                renderLanHeader(graphics, rowLeft, rowTop, rowWidth, cardH);
            }
        }

        private void renderOnlineServer(GuiGraphics graphics, OnlineServerEntry entry, int rowLeft, int rowTop,
                                        int rowWidth, int rowHeight, float hover, boolean selected) {
            ServerData server = entry.getServerData();
            FaviconTexture favicon = ((ServerListOnlineEntryAccessor) (Object) entry).xerion$getIcon();
            int iconSize = Math.min(32, rowHeight - 8);
            int iconX = rowLeft;
            int iconY = rowTop + (rowHeight - iconSize) / 2;
            ShapeRenderer.roundRect(graphics, iconX - 1, iconY - 1, iconSize + 2, iconSize + 2, 6,
                    UiColor.alpha(selected ? UiPalette.accent() : UiPalette.border(), renderAlpha));
            ResourceLocation texture = favicon.textureLocation();
            int textureId = minecraft.getTextureManager().getTexture(texture).getId();
            if (!TextureRenderer.roundedTexture(graphics, textureId, iconX, iconY, iconSize, iconSize, 5, renderAlpha)) {
                graphics.blit(texture, iconX, iconY, iconSize, iconSize, 0.0f, 0.0f, 64, 64, 64, 64);
            }
            float playAmount = Math.max(hover, selected ? 0.38f : 0.0f);
            if (playAmount > 0.01f) {
                ShapeRenderer.roundRect(graphics, iconX, iconY, iconSize, iconSize, 5,
                        UiColor.alpha(UiPalette.accentDark(), renderAlpha * playAmount * 0.58f));
                float playSize = 10.0f;
                MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_PLAY,
                        iconX + iconSize * 0.5f - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_PLAY, playSize) * 0.5f,
                        MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_PLAY, iconY, iconSize, playSize),
                        playSize, UiColor.alpha(0xFFFFFFFF, renderAlpha * playAmount));
            }

            boolean compatible = server.protocol == SharedConstants.getCurrentVersion().getProtocolVersion();
            String pingText = !server.pinged || server.ping == -2L
                    ? Component.translatable("multiplayer.status.pinging").getString()
                    : server.ping < 0L ? Component.translatable("multiplayer.status.no_connection").getString()
                    : server.ping + " ms";
            int pingColor = !server.pinged || server.ping == -2L ? UiPalette.muted()
                    : server.ping < 0L ? UiPalette.statusRed()
                    : server.ping < 150L ? UiPalette.statusGreen()
                    : server.ping < 350L ? UiPalette.accent() : UiPalette.statusRed();
            Component status = compatible ? server.status : server.version;
            String playerCount = server.players == null
                    ? null
                    : server.players.online() + " / " + server.players.max();
            float statusWidth = playerCount == null
                    ? TextPainter.formattedWidth(status, 6.4f)
                    : MSDFRenderer.width(playerCount, 6.4f);
            int rightReserve = Math.max(74, Math.round(Math.max(statusWidth,
                    MSDFRenderer.width(pingText, 6.2f))) + 8);
            int textX = iconX + iconSize + 8;
            int textW = Math.max(32, rowLeft + rowWidth - textX - rightReserve);
            TextPainter.drawFormattedClipped(graphics, server.name, textX, rowTop + 4, textW, 9.0f,
                    compatible ? UiPalette.text() : UiPalette.statusRed(), renderAlpha);
            TextPainter.drawFormattedClipped(graphics, server.motd, textX, rowTop + 17, textW, 6.8f,
                    UiPalette.muted(), renderAlpha);
            String address = minecraft.options.hideServerAddress
                    ? Component.translatable("selectServer.hiddenAddress").getString() : server.ip;
            TextPainter.drawClipped(graphics, address, textX, rowTop + 26, textW, 6.8f,
                    UiPalette.muted(), renderAlpha * 0.76f);
            int rightX = rowLeft + rowWidth;
            if (playerCount != null) {
                MSDFRenderer.draw(graphics, playerCount, rightX - statusWidth, rowTop + 7, 6.4f,
                        UiColor.alpha(UiPalette.statusGreen(), renderAlpha));
            } else {
                TextPainter.drawFormatted(graphics, status, rightX - statusWidth, rowTop + 7, 6.4f,
                        compatible ? UiPalette.text() : UiPalette.statusRed(), renderAlpha);
            }
            float pingW = MSDFRenderer.width(pingText, 6.2f);
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_SIGNAL,
                    rightX - pingW - 13, MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_SIGNAL,
                            rowTop + 24, 12, 6.2f), 6.2f, UiColor.alpha(pingColor, renderAlpha));
            MSDFRenderer.draw(graphics, pingText, rightX - pingW, rowTop + 26, 6.2f,
                    UiColor.alpha(pingColor, renderAlpha));
        }

        private void renderLanServer(GuiGraphics graphics, LanServer server, int rowLeft, int rowTop,
                                     int rowWidth, int rowHeight) {
            int iconSize = Math.min(32, rowHeight - 8);
            int iconX = rowLeft;
            int iconY = rowTop + (rowHeight - iconSize) / 2;
            ShapeRenderer.roundRect(graphics, iconX, iconY, iconSize, iconSize, 6,
                    UiColor.alpha(UiPalette.field(), renderAlpha));
            float iconSizeText = 11.0f;
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_NETWORK_WIRED,
                    iconX + (iconSize - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_NETWORK_WIRED, iconSizeText)) * 0.5f,
                    MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_NETWORK_WIRED, iconY, iconSize, iconSizeText),
                    iconSizeText, UiColor.alpha(UiPalette.accent(), renderAlpha));
            int textX = iconX + iconSize + 9;
            int textW = rowLeft + rowWidth - textX;
            TextPainter.drawClipped(graphics, Component.translatable("lanServer.title").getString(),
                    textX, rowTop + 5, textW, 8.6f, UiPalette.text(), renderAlpha);
            TextPainter.drawFormattedClipped(graphics, server.getMotd(), textX, rowTop + 19, textW, 6.8f,
                    UiPalette.muted(), renderAlpha);
            String address = minecraft.options.hideServerAddress
                    ? Component.translatable("selectServer.hiddenAddress").getString() : server.getAddress();
            TextPainter.drawClipped(graphics, address, textX, rowTop + 26, textW, 6.2f,
                    UiPalette.muted(), renderAlpha * 0.76f);
        }

        private void renderLanHeader(GuiGraphics graphics, int rowLeft, int rowTop, int rowWidth, int rowHeight) {
            String label = Component.translatable("lanServer.scanning").getString()
                    + " " + LoadingDotsText.get(Util.getMillis());
            float labelW = MSDFRenderer.width(label, 7.0f);
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_SATELLITE_DISH,
                    rowLeft + (rowWidth - labelW) * 0.5f - 15,
                    MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_SATELLITE_DISH, rowTop, rowHeight, 7.5f),
                    7.5f, UiColor.alpha(UiPalette.accent(), renderAlpha));
            MSDFRenderer.draw(graphics, label, rowLeft + (rowWidth - labelW) * 0.5f,
                    MSDFRenderer.textCenteredY(rowTop, rowHeight, 7.0f), 7.0f,
                    UiColor.alpha(UiPalette.muted(), renderAlpha));
        }
    }
}
