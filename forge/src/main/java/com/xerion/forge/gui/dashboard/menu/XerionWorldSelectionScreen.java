package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.WorldListEntryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelSummary;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class XerionWorldSelectionScreen extends SelectWorldScreen {
    private static final int PANEL_MAX_W = 620;
    private static final int PANEL_MAX_H = 330;
    private static final int PANEL_MARGIN = 16;
    private static final int HEADER_H = 64;
    private static final int FOOTER_H = 54;
    private static final int COMPACT_FOOTER_H = 86;
    private static final int ROW_H = 42;
    private static final DateFormat WORLD_DATE_FORMAT = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT);

    private final List<XerionActionButton> actionButtons = new ArrayList<>();
    private XerionWorldList worldList;
    private XerionActionButton playButton;
    private XerionActionButton editButton;
    private XerionActionButton deleteButton;
    private XerionActionButton recreateButton;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int searchX;
    private int searchY;
    private int searchW;
    private float screenAlpha;

    public XerionWorldSelectionScreen(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    protected void init() {
        EditBox previousSearch = searchBox;
        XerionWorldList previousList = worldList;
        actionButtons.clear();

        panelW = Math.min(PANEL_MAX_W, Math.max(300, width - PANEL_MARGIN * 2));
        panelH = Math.min(PANEL_MAX_H, Math.max(260, height - PANEL_MARGIN * 2));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        boolean compactActions = panelW < 590;
        int footerH = compactActions ? COMPACT_FOOTER_H : FOOTER_H;
        int listTop = panelY + HEADER_H;
        int listBottom = panelY + panelH - footerH;
        int rowWidth = Math.max(252, panelW - 40);

        searchW = Math.min(220, Math.max(150, panelW / 3));
        searchX = panelX + panelW - searchW - 18;
        searchY = panelY + 18;
        searchBox = new AlignedEditBox(font, searchX + 28, searchY,
                searchW - 38, 28, previousSearch, Component.translatable("selectWorld.search"), 9);
        searchBox.setBordered(false);
        searchBox.setTextColor(UiPalette.inputText());
        searchBox.setTextColorUneditable(UiPalette.muted());
        searchBox.setHint(Component.translatable("selectWorld.search"));

        worldList = new XerionWorldList(this, minecraft, width, height,
                listTop, listBottom, ROW_H, searchBox.getValue(), previousList, rowWidth);
        searchBox.setResponder(worldList::updateFilter);
        addWidget(searchBox);
        addWidget(worldList);

        buildActionButtons(compactActions);
        updateButtonStatus(false, false);
        setInitialFocus(searchBox);
    }

    private void buildActionButtons(boolean compact) {
        int left = panelX + 18;
        int right = panelX + panelW - 18;
        int buttonH = 26;
        int gap = 6;

        if (!compact) {
            int y = panelY + panelH - 40;
            addActionButton(left, y, 112, buttonH, Component.translatable("selectWorld.create"), FontAwesomeIcons.ICON_FA_PLUS,
                    ButtonStyle.NORMAL, () -> XerionCreateWorldScreen.openFresh(minecraft, this));

            int groupW = 112 + 68 + 76 + 86 + 70 + gap * 4;
            int x = right - groupW;
            playButton = addActionButton(x, y, 112, buttonH, Component.translatable("selectWorld.select"), FontAwesomeIcons.ICON_FA_PLAY,
                    ButtonStyle.PRIMARY, this::playSelected);
            x += 112 + gap;
            editButton = addActionButton(x, y, 68, buttonH, Component.translatable("selectWorld.edit"), FontAwesomeIcons.ICON_FA_PENCIL,
                    ButtonStyle.NORMAL, this::editSelected);
            x += 68 + gap;
            deleteButton = addActionButton(x, y, 76, buttonH, Component.translatable("selectWorld.delete"), FontAwesomeIcons.ICON_FA_TRASH_CAN,
                    ButtonStyle.DANGER, this::deleteSelected);
            x += 76 + gap;
            recreateButton = addActionButton(x, y, 86, buttonH, Component.translatable("selectWorld.recreate"), FontAwesomeIcons.ICON_FA_CLONE,
                    ButtonStyle.NORMAL, this::recreateSelected);
            x += 86 + gap;
            addActionButton(x, y, 70, buttonH, Component.translatable("gui.back"), FontAwesomeIcons.ICON_FA_ARROW_LEFT,
                    ButtonStyle.NORMAL, this::onClose);
            return;
        }

        int innerW = right - left;
        int topY = panelY + panelH - 72;
        int halfW = (innerW - gap) / 2;
        addActionButton(left, topY, halfW, buttonH, Component.translatable("selectWorld.create"), FontAwesomeIcons.ICON_FA_PLUS,
                ButtonStyle.NORMAL, () -> XerionCreateWorldScreen.openFresh(minecraft, this));
        playButton = addActionButton(left + halfW + gap, topY, halfW, buttonH,
                Component.translatable("selectWorld.select"), FontAwesomeIcons.ICON_FA_PLAY, ButtonStyle.PRIMARY, this::playSelected);

        int bottomY = panelY + panelH - 40;
        int quarterW = (innerW - gap * 3) / 4;
        int x = left;
        editButton = addActionButton(x, bottomY, quarterW, buttonH,
                Component.translatable("selectWorld.edit"), FontAwesomeIcons.ICON_FA_PENCIL, ButtonStyle.NORMAL, this::editSelected);
        x += quarterW + gap;
        deleteButton = addActionButton(x, bottomY, quarterW, buttonH,
                Component.translatable("selectWorld.delete"), FontAwesomeIcons.ICON_FA_TRASH_CAN, ButtonStyle.DANGER, this::deleteSelected);
        x += quarterW + gap;
        recreateButton = addActionButton(x, bottomY, quarterW, buttonH,
                Component.translatable("selectWorld.recreate"), FontAwesomeIcons.ICON_FA_CLONE, ButtonStyle.NORMAL, this::recreateSelected);
        x += quarterW + gap;
        addActionButton(x, bottomY, right - x, buttonH,
                Component.translatable("gui.back"), FontAwesomeIcons.ICON_FA_ARROW_LEFT, ButtonStyle.NORMAL, this::onClose);
    }

    private XerionActionButton addActionButton(int x, int y, int w, int h, Component label, int icon,
                                                ButtonStyle style, Runnable action) {
        XerionActionButton button = new XerionActionButton(x, y, w, h,
                label, icon, style, action);
        actionButtons.add(button);
        addRenderableWidget(button);
        return button;
    }

    private void playSelected() {
        worldList.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld);
    }

    private void editSelected() {
        worldList.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld);
    }

    private void deleteSelected() {
        worldList.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld);
    }

    private void recreateSelected() {
        worldList.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld);
    }

    @Override
    public void tick() {
        searchBox.tick();
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
        ShadowRenderer.shadow(graphics, panelX, panelY, panelW, panelH, 10,
                ShadowStyle.popup(), alpha);
        ShapeRenderer.roundRect(graphics, panelX, panelY, panelW, panelH, 10,
                UiColor.alpha(UiPalette.shell(), alpha * 0.82f));
        ShapeRenderer.roundRectBorder(graphics, panelX, panelY, panelW, panelH,
                10, 1, UiColor.alpha(UiPalette.shellBorder(), alpha));

        drawHeader(graphics, alpha);
        worldList.setRenderAlpha(alpha);
        worldList.render(graphics, mouseX, mouseY, partialTick);
        drawSearch(graphics, alpha);

        for (XerionActionButton button : actionButtons) {
            button.setAlpha(alpha);
            button.render(graphics, mouseX, mouseY, partialTick);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawHeader(GuiGraphics graphics, float alpha) {
        int titleX = panelX + 20;
        drawText(graphics, Component.translatable("menu.singleplayer").getString(), titleX, panelY + 17, 16.0f,
                UiPalette.text(), alpha);
        drawText(graphics, Component.translatable("xerion.world.local").getString(), titleX, panelY + 40, 7.5f,
                UiPalette.muted(), alpha);
        graphics.fill(panelX + 16, panelY + HEADER_H - 1,
                panelX + panelW - 16, panelY + HEADER_H,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));

        int footerTop = worldList.listBottom();
        graphics.fill(panelX + 16, footerTop, panelX + panelW - 16, footerTop + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
    }

    private void drawSearch(GuiGraphics graphics, float alpha) {
        float focus = searchBox.isFocused() ? 1.0f : 0.0f;
        int border = UiColor.lerp(UiPalette.border(), UiPalette.accent(), focus);
        ShapeRenderer.roundRect(graphics, searchX, searchY, searchW, 28, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.88f));
        ShapeRenderer.roundRectBorder(graphics, searchX, searchY, searchW, 28,
                6, 1, UiColor.alpha(border, alpha));
        float iconSize = 8.5f;
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS,
                searchX + 10, MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS,
                        searchY, 28, iconSize), iconSize,
                UiColor.alpha(UiPalette.muted(), alpha));
        searchBox.render(graphics, 0, 0, 0.0f);
    }

    private void drawText(GuiGraphics graphics, String text, float x, float y,
                          float size, int color, float alpha) {
        int resolved = UiColor.alpha(color, alpha);
        MSDFRenderer.draw(graphics, text, x, y, size, resolved);
    }

    @Override
    public void updateButtonStatus(boolean selectable, boolean selected) {
        if (playButton == null) {
            return;
        }
        playButton.active = selectable;
        editButton.active = selectable;
        recreateButton.active = selectable;
        deleteButton.active = selected;
    }

    @Override
    public void removed() {
        if (worldList != null) {
            worldList.children().forEach(WorldSelectionList.Entry::close);
        }
        super.removed();
    }

    private enum ButtonStyle {
        NORMAL,
        PRIMARY,
        DANGER
    }

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
        public void onPress() {
            action.run();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            hoverProgress = UiMath.lerp(hoverProgress,
                    isHoveredOrFocused() && active ? 1.0f : 0.0f,
                    UiAnimationTimings.CONTROL);
            float widgetAlpha = UiMath.clamp01(alpha) * (active ? 1.0f : 0.42f);
            int accent = style == ButtonStyle.DANGER ? UiPalette.statusRed() : UiPalette.accent();
            int normalFill = UiColor.alpha(UiPalette.surface(), widgetAlpha * 0.78f);
            int activeFill = UiColor.alpha(accent, widgetAlpha * (style == ButtonStyle.PRIMARY ? 0.94f : 0.72f));
            float mix = style == ButtonStyle.PRIMARY ? 0.62f + hoverProgress * 0.38f : hoverProgress;
            ShapeRenderer.roundRect(graphics, getX(), getY(), getWidth(), getHeight(), 6,
                    UiColor.lerp(normalFill, activeFill, mix));
            ShapeRenderer.roundRectBorder(graphics, getX(), getY(), getWidth(), getHeight(),
                    6, 1, UiColor.alpha(UiColor.lerp(UiPalette.border(), accent, hoverProgress), widgetAlpha));

            String label = getMessage().getString();
            float iconSize = 8.5f;
            float textSize = 8.0f;
            if (MSDFRenderer.renderResourcesReady()) {
                float iconW = MSDFRenderer.iconWidth(icon, iconSize);
                float textW = MSDFRenderer.width(label, textSize);
                float groupW = iconW + 5.0f + textW;
                float iconX = getX() + (getWidth() - groupW) * 0.5f;
                int color = style == ButtonStyle.PRIMARY ? 0xFFFFFFFF : UiPalette.text();
                MSDFRenderer.drawIconCodepoint(graphics, icon, iconX,
                        MSDFRenderer.iconCenteredY(icon, getY(), getHeight(), iconSize),
                        iconSize, UiColor.alpha(color, widgetAlpha));
                MSDFRenderer.draw(graphics, label, iconX + iconW + 5.0f,
                        MSDFRenderer.textCenteredY(getY(), getHeight(), textSize),
                        textSize, UiColor.alpha(color, widgetAlpha));
            } else {
                float textW = MSDFRenderer.width(label, textSize);
                MSDFRenderer.draw(graphics, label,
                        getX() + (getWidth() - textW) * 0.5f,
                        MSDFRenderer.textCenteredY(getY(), getHeight(), textSize),
                        textSize, UiColor.alpha(UiPalette.text(), widgetAlpha));
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static final class XerionWorldList extends WorldSelectionList {
        private final Minecraft minecraft;
        private final int screenWidth;
        private final int rowWidth;
        private final Map<Entry, Float> hoverAnimations = new IdentityHashMap<>();
        private float renderAlpha = 1.0f;
        private float scrollbarHover;
        private boolean suppressVanillaScrollbar;

        private XerionWorldList(SelectWorldScreen screen, Minecraft minecraft,
                                int width, int height, int top, int bottom, int itemHeight,
                                String filter, WorldSelectionList previous, int rowWidth) {
            super(screen, minecraft, width, height, top, bottom, itemHeight, filter, previous);
            this.minecraft = minecraft;
            this.screenWidth = width;
            this.rowWidth = rowWidth;
            setRenderBackground(false);
            setRenderTopAndBottom(false);
            setRenderSelection(false);
        }

        private void setRenderAlpha(float alpha) {
            renderAlpha = alpha;
        }

        private int listBottom() {
            return y1;
        }

        @Override
        public int getRowWidth() {
            return rowWidth;
        }

        @Override
        protected int getScrollbarPosition() {
            if (suppressVanillaScrollbar) {
                return screenWidth + 32;
            }
            return screenWidth / 2 + rowWidth / 2 + 12;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            suppressVanillaScrollbar = true;
            try {
                super.render(graphics, mouseX, mouseY, partialTick);
            } finally {
                suppressVanillaScrollbar = false;
            }
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
            int thumbH = (int) ((float) (viewportH * viewportH) / (float) getMaxPosition());
            thumbH = UiMath.clamp(thumbH, 32, viewportH - 8);
            int thumbY = (int) getScrollAmount() * (viewportH - thumbH) / maxScroll + y0;
            thumbY = Math.max(y0, thumbY);
            boolean hovered = mouseX >= scrollbarX - 2 && mouseX <= scrollbarX + 8
                    && mouseY >= y0 && mouseY <= y1;
            scrollbarHover = UiMath.lerp(scrollbarHover, hovered ? 1.0f : 0.0f,
                    UiAnimationTimings.CONTROL);

            ShapeRenderer.roundRect(graphics, scrollbarX + 2, y0 + 2, 2, viewportH - 4, 1,
                    UiColor.alpha(UiPalette.track(), renderAlpha * 0.72f));
            int thumbColor = UiColor.lerp(UiPalette.accentSoft(), UiPalette.accent(), scrollbarHover);
            ShapeRenderer.roundRect(graphics, scrollbarX, thumbY, 5, thumbH, 2,
                    UiColor.alpha(thumbColor, renderAlpha * (0.82f + scrollbarHover * 0.18f)));
        }

        @Override
        protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                                  int index, int rowLeft, int rowTop, int rowWidth, int rowHeight) {
            Entry entry = getEntry(index);
            int cardY = rowTop;
            int cardH = Math.max(34, rowHeight);
            boolean hovered = mouseX >= rowLeft - 5 && mouseX <= rowLeft + rowWidth + 5
                    && mouseY >= cardY && mouseY <= cardY + cardH;
            float hover = UiMath.lerp(hoverAnimations.getOrDefault(entry, 0.0f),
                    hovered ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
            hoverAnimations.put(entry, hover);
            boolean selected = getSelected() == entry;
            int fill = UiColor.lerp(
                    UiColor.alpha(UiPalette.surface(), renderAlpha * 0.48f),
                    UiColor.alpha(UiPalette.accentSoft(), renderAlpha * 0.72f),
                    selected ? 0.72f : hover * 0.34f);
            int border = selected ? UiPalette.accent()
                    : UiColor.lerp(UiPalette.border(), UiPalette.accent(), hover * 0.48f);
            ShapeRenderer.roundRect(graphics, rowLeft - 5, cardY,
                    rowWidth + 10, cardH, 6, fill);
            ShapeRenderer.roundRectBorder(graphics, rowLeft - 5, cardY,
                    rowWidth + 10, cardH, 6, 1,
                    UiColor.alpha(border, renderAlpha));
            if (selected) {
                ShapeRenderer.roundRect(graphics, rowLeft - 5, cardY + 5,
                        3, Math.max(1, cardH - 10), 2,
                        UiColor.alpha(UiPalette.accent(), renderAlpha));
            }
            renderWorldEntry(graphics, entry, index, rowLeft, cardY, rowWidth, cardH,
                    hover, selected);
        }

        private void renderWorldEntry(GuiGraphics graphics, Entry entry, int index,
                                      int rowLeft, int rowTop, int rowWidth, int rowHeight,
                                      float hover, boolean selected) {
            if (!(entry instanceof WorldListEntry worldEntry)) {
                return;
            }
            WorldListEntryAccessor accessor = (WorldListEntryAccessor) (Object) worldEntry;
            LevelSummary summary = accessor.xerion$getSummary();
            FaviconTexture favicon = accessor.xerion$getIcon();

            int imageSize = Math.min(32, rowHeight - 8);
            int imageX = rowLeft;
            int imageY = rowTop + (rowHeight - imageSize) / 2;
            ShapeRenderer.roundRect(graphics, imageX - 1, imageY - 1,
                    imageSize + 2, imageSize + 2, 6,
                    UiColor.alpha(selected ? UiPalette.accent() : UiPalette.border(), renderAlpha));
            ResourceLocation texture = favicon.textureLocation();
            int textureId = minecraft.getTextureManager().getTexture(texture).getId();
            if (!TextureRenderer.roundedTexture(graphics, textureId,
                    imageX, imageY, imageSize, imageSize, 5, renderAlpha)) {
                graphics.blit(texture, imageX, imageY, imageSize, imageSize,
                        0.0f, 0.0f, 64, 64, 64, 64);
            }

            float playAmount = Math.max(hover, selected ? 0.42f : 0.0f);
            if (playAmount > 0.01f && !summary.isDisabled()) {
                ShapeRenderer.roundRect(graphics, imageX, imageY, imageSize, imageSize, 5,
                        UiColor.alpha(UiPalette.accentDark(), renderAlpha * playAmount * 0.62f));
                float iconSize = 10.0f;
                float iconX = imageX + imageSize * 0.5f
                        - MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_PLAY, iconSize) * 0.5f;
                MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_PLAY,
                        iconX, MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_PLAY,
                                imageY, imageSize, iconSize), iconSize,
                        UiColor.alpha(0xFFFFFFFF, renderAlpha * playAmount));
            }

            String worldName = summary.getLevelName();
            if (worldName == null || worldName.isBlank()) {
                worldName = Component.translatable("selectWorld.world").getString() + " " + (index + 1);
            }
            String storageLine = summary.getLevelId();
            if (summary.getLastPlayed() != -1L) {
                storageLine += "  |  " + WORLD_DATE_FORMAT.format(new Date(summary.getLastPlayed()));
            }
            String info = summary.getInfo().getString();
            int textX = imageX + imageSize + 8;
            int statusReserve = summary.isLocked() || summary.isDisabled()
                    || summary.isExperimental() ? 20 : 4;
            int textW = Math.max(20, rowLeft + rowWidth - textX - statusReserve);
            TextPainter.drawClipped(graphics, worldName, textX, rowTop + 4,
                    textW, 9.0f, UiPalette.text(), renderAlpha);
            TextPainter.drawClipped(graphics, storageLine, textX, rowTop + 17,
                    textW, 6.8f, UiPalette.muted(), renderAlpha);
            TextPainter.drawClipped(graphics, info, textX, rowTop + 26,
                    textW, 6.8f, summary.isDisabled() ? UiPalette.statusRed() : UiPalette.muted(), renderAlpha);

            int statusIcon = 0;
            int statusColor = UiPalette.muted();
            if (summary.isLocked()) {
                statusIcon = FontAwesomeIcons.ICON_FA_LOCK;
                statusColor = UiPalette.statusRed();
            } else if (summary.isDisabled() || !summary.isCompatible()) {
                statusIcon = FontAwesomeIcons.ICON_FA_TRIANGLE_EXCLAMATION;
                statusColor = UiPalette.statusRed();
            } else if (summary.isExperimental()) {
                statusIcon = FontAwesomeIcons.ICON_FA_FLASK;
                statusColor = UiPalette.accent();
            }
            if (statusIcon != 0) {
                float iconSize = 8.0f;
                MSDFRenderer.drawIconCodepoint(graphics, statusIcon,
                        rowLeft + rowWidth - 14,
                        MSDFRenderer.iconCenteredY(statusIcon, rowTop, rowHeight, iconSize),
                        iconSize, UiColor.alpha(statusColor, renderAlpha));
            }
        }
    }
}
