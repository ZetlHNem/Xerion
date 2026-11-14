package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.CreateFlatWorldScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.List;
import java.util.function.Consumer;

public final class XerionFlatWorldScreen extends Screen {
    private static final int PANEL_MAX_W = 620;
    private static final int PANEL_MAX_H = 330;
    private static final int PANEL_MARGIN = 16;
    private static final int HEADER_H = 58;
    private static final int FOOTER_H = 54;
    private static final int ROW_H = 38;

    private final CreateFlatWorldScreen delegate;
    private final CreateWorldScreen parent;
    private final Consumer<FlatLevelGeneratorSettings> applySettings;
    private XerionMenuActionButton removeButton;
    private int selectedRow = -1;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int listX;
    private int listY;
    private int listW;
    private int listBottom;
    private float screenAlpha;
    private float scroll;
    private float targetScroll;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;

    public XerionFlatWorldScreen(CreateFlatWorldScreen delegate) {
        super(Component.translatable("createWorld.customize.flat.title"));
        this.delegate = delegate;
        CreateFlatWorldScreenAccessor accessor = (CreateFlatWorldScreenAccessor) (Object) delegate;
        this.parent = accessor.xerion$getParent();
        this.applySettings = accessor.xerion$getApplySettings();
    }

    FlatLevelGeneratorSettings settings() {
        return delegate.settings();
    }

    void setConfig(FlatLevelGeneratorSettings settings) {
        delegate.setConfig(settings);
        selectedRow = -1;
        scroll = 0.0f;
        targetScroll = 0.0f;
        updateButtonState();
    }

    CreateWorldScreen worldParent() {
        return parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(PANEL_MAX_W, Math.max(320, width - PANEL_MARGIN * 2));
        panelH = Math.min(PANEL_MAX_H, Math.max(276, height - PANEL_MARGIN * 2));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        listX = panelX + 22;
        listY = panelY + HEADER_H + 29;
        listW = panelW - 44;
        listBottom = panelY + panelH - FOOTER_H - 7;

        buildFooter();
        settings().updateLayers();
        clampScroll();
        updateButtonState();
    }

    private void buildFooter() {
        int x = panelX + 18;
        int y = panelY + panelH - 40;
        int gap = 6;
        int innerW = panelW - 36;
        int buttonW = (innerW - gap * 3) / 4;

        removeButton = addRenderableWidget(new XerionMenuActionButton(x, y, buttonW, 26,
                Component.translatable("createWorld.customize.flat.removeLayer"),
                FontAwesomeIcons.ICON_FA_TRASH_CAN, XerionMenuActionButton.Style.DANGER,
                this::removeSelectedLayer));
        x += buttonW + gap;
        addRenderableWidget(new XerionMenuActionButton(x, y, buttonW, 26,
                Component.translatable("createWorld.customize.presets"),
                FontAwesomeIcons.ICON_FA_LAYER_GROUP, XerionMenuActionButton.Style.NORMAL,
                this::openPresets));
        x += buttonW + gap;
        addRenderableWidget(new XerionMenuActionButton(x, y, buttonW, 26,
                Component.translatable("gui.cancel"), FontAwesomeIcons.ICON_FA_XMARK,
                XerionMenuActionButton.Style.NORMAL, this::onClose));
        x += buttonW + gap;
        addRenderableWidget(new XerionMenuActionButton(x, y, panelX + panelW - 18 - x, 26,
                Component.translatable("gui.done"), FontAwesomeIcons.ICON_FA_CHECK,
                XerionMenuActionButton.Style.PRIMARY, this::applyAndClose));
    }

    private void removeSelectedLayer() {
        List<FlatLayerInfo> layers = settings().getLayersInfo();
        if (selectedRow < 0 || selectedRow >= layers.size()) {
            return;
        }
        layers.remove(layers.size() - selectedRow - 1);
        settings().updateLayers();
        selectedRow = layers.isEmpty() ? -1 : Math.min(selectedRow, layers.size() - 1);
        clampScroll();
        updateButtonState();
    }

    private void openPresets() {
        settings().updateLayers();
        minecraft.setScreen(new XerionFlatPresetScreen(this));
    }

    private void applyAndClose() {
        settings().updateLayers();
        applySettings.accept(settings());
        minecraft.setScreen(parent);
    }

    private void updateButtonState() {
        if (removeButton != null) {
            removeButton.active = selectedRow >= 0 && selectedRow < settings().getLayersInfo().size();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        AnimationClock.beginFrame();
        KawaseBlurPipeline.beginFrame();
        screenAlpha = UiMath.lerp(screenAlpha, 1.0f, UiAnimationTimings.PANEL_FADE);
        scroll = UiMath.lerp(scroll, targetScroll, UiAnimationTimings.CONTROL);
        if (Math.abs(scroll - targetScroll) < 0.05f) {
            scroll = targetScroll;
        }
        float alpha = UiMath.clamp01(screenAlpha);

        drawPanel(graphics, alpha);
        drawHeader(graphics, alpha);
        drawList(graphics, mouseX, mouseY, alpha);
        drawScrollbar(graphics, mouseX, mouseY, alpha);
        graphics.fill(panelX + 16, panelY + panelH - FOOTER_H,
                panelX + panelW - 16, panelY + panelH - FOOTER_H + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
        for (var renderable : renderables) {
            if (renderable instanceof AbstractWidget widget) {
                widget.setAlpha(alpha);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawPanel(GuiGraphics graphics, float alpha) {
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
    }

    private void drawHeader(GuiGraphics graphics, float alpha) {
        drawText(graphics, title.getString(), panelX + 20, panelY + 16,
                16.0f, UiPalette.text(), alpha);
        drawText(graphics, Component.translatable("selectWorld.customizeType").getString(),
                panelX + 20, panelY + 39, 7.5f, UiPalette.muted(), alpha);
        graphics.fill(panelX + 16, panelY + HEADER_H,
                panelX + panelW - 16, panelY + HEADER_H + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));

        drawText(graphics, Component.translatable("createWorld.customize.flat.tile").getString(),
                listX + 8, panelY + HEADER_H + 11, 7.5f, UiPalette.muted(), alpha);
        String height = Component.translatable("createWorld.customize.flat.height").getString();
        float heightW = textWidth(height, 7.5f);
        drawText(graphics, height, listX + listW - heightW - 15,
                panelY + HEADER_H + 11, 7.5f, UiPalette.muted(), alpha);
    }

    private void drawList(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        List<FlatLayerInfo> layers = settings().getLayersInfo();
        graphics.enableScissor(listX - 1, listY, listX + listW + 1, listBottom);
        for (int row = 0; row < layers.size(); row++) {
            int y = Math.round(listY + row * ROW_H - scroll);
            if (y + ROW_H - 4 < listY || y > listBottom) {
                continue;
            }
            FlatLayerInfo layer = layers.get(layers.size() - row - 1);
            drawLayerRow(graphics, row, layer, y, mouseX, mouseY, alpha);
        }
        graphics.disableScissor();
    }

    private void drawLayerRow(GuiGraphics graphics, int row, FlatLayerInfo layer, int y,
                              int mouseX, int mouseY, float alpha) {
        int rowH = ROW_H - 5;
        boolean hovered = mouseX >= listX && mouseX <= listX + listW - 10
                && mouseY >= Math.max(y, listY) && mouseY <= Math.min(y + rowH, listBottom);
        boolean selected = row == selectedRow;
        float mix = selected ? 0.72f : hovered ? 0.28f : 0.0f;
        int fill = UiColor.lerp(UiPalette.surface(), UiPalette.accentSoft(), mix);
        int border = selected ? UiPalette.accent()
                : UiColor.lerp(UiPalette.border(), UiPalette.accent(), hovered ? 0.42f : 0.0f);
        ShapeRenderer.roundRect(graphics, listX, y, listW - 10, rowH, 6,
                UiColor.alpha(fill, alpha * 0.76f));
        ShapeRenderer.roundRectBorder(graphics, listX, y, listW - 10, rowH, 6, 1,
                UiColor.alpha(border, alpha));
        if (selected) {
            ShapeRenderer.roundRect(graphics, listX, y + 5, 3, rowH - 10, 2,
                    UiColor.alpha(UiPalette.accent(), alpha));
        }

        ItemStack displayItem = displayItem(layer.getBlockState());
        int itemX = listX + 10;
        int itemY = y + (rowH - 16) / 2;
        ShapeRenderer.roundRect(graphics, itemX - 2, itemY - 2, 20, 20, 5,
                UiColor.alpha(UiPalette.field(), alpha * 0.92f));
        if (!displayItem.isEmpty()) {
            graphics.renderFakeItem(displayItem, itemX, itemY);
        }
        drawText(graphics, displayItem.getHoverName().getString(), itemX + 25,
                MSDFRenderer.textCenteredY(y, rowH, 8.5f), 8.5f,
                UiPalette.text(), alpha);

        Component height;
        if (row == 0) {
            height = Component.translatable("createWorld.customize.flat.layer.top", layer.getHeight());
        } else if (row == settings().getLayersInfo().size() - 1) {
            height = Component.translatable("createWorld.customize.flat.layer.bottom", layer.getHeight());
        } else {
            height = Component.translatable("createWorld.customize.flat.layer", layer.getHeight());
        }
        String heightText = height.getString();
        drawText(graphics, heightText,
                listX + listW - textWidth(heightText, 8.0f) - 22,
                MSDFRenderer.textCenteredY(y, rowH, 8.0f), 8.0f,
                UiPalette.accentDark(), alpha);
    }

    private static ItemStack displayItem(BlockState state) {
        Item item = state.getBlock().asItem();
        if (item == Items.AIR) {
            if (state.is(Blocks.WATER)) {
                item = Items.WATER_BUCKET;
            } else if (state.is(Blocks.LAVA)) {
                item = Items.LAVA_BUCKET;
            }
        }
        return new ItemStack(item);
    }

    private void drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int x = listX + listW - 5;
        int viewportH = listBottom - listY;
        int contentH = settings().getLayersInfo().size() * ROW_H;
        int thumbH = UiMath.clamp(viewportH * viewportH / Math.max(1, contentH), 24, viewportH);
        int travel = viewportH - thumbH;
        int thumbY = listY + Math.round(targetScroll / maxScroll * travel);
        boolean hovered = mouseX >= x - 3 && mouseX <= x + 5
                && mouseY >= listY && mouseY <= listBottom;
        ShapeRenderer.roundRect(graphics, x, listY + 2, 2, viewportH - 4, 1,
                UiColor.alpha(UiPalette.track(), alpha * 0.72f));
        ShapeRenderer.roundRect(graphics, x - 1, thumbY, 4, thumbH, 2,
                UiColor.alpha(hovered || draggingScrollbar ? UiPalette.accent() : UiPalette.accentSoft(),
                        alpha * 0.92f));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && maxScroll() > 0 && isOverScrollbar(mouseX, mouseY)) {
            int thumbY = scrollbarThumbY();
            int thumbH = scrollbarThumbHeight();
            draggingScrollbar = true;
            scrollbarDragOffset = mouseY >= thumbY && mouseY <= thumbY + thumbH
                    ? (int) mouseY - thumbY : thumbH / 2;
            updateScrollFromMouse(mouseY);
            return true;
        }
        if (button == 0 && mouseX >= listX && mouseX <= listX + listW - 10
                && mouseY >= listY && mouseY <= listBottom) {
            int row = (int) ((mouseY - listY + scroll) / ROW_H);
            if (row >= 0 && row < settings().getLayersInfo().size()) {
                selectedRow = row;
                updateButtonState();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (draggingScrollbar && button == 0) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= listX && mouseX <= listX + listW
                && mouseY >= listY && mouseY <= listBottom) {
            targetScroll -= (float) delta * ROW_H * 1.7f;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = listX + listW - 5;
        return mouseX >= x - 4 && mouseX <= x + 6
                && mouseY >= listY && mouseY <= listBottom;
    }

    private void updateScrollFromMouse(double mouseY) {
        int viewportH = listBottom - listY;
        int travel = viewportH - scrollbarThumbHeight();
        if (travel <= 0) {
            targetScroll = 0.0f;
            return;
        }
        float ratio = ((float) mouseY - listY - scrollbarDragOffset) / travel;
        targetScroll = UiMath.clamp01(ratio) * maxScroll();
        scroll = targetScroll;
    }

    private int scrollbarThumbY() {
        int travel = listBottom - listY - scrollbarThumbHeight();
        return listY + Math.round(targetScroll / Math.max(1, maxScroll()) * travel);
    }

    private int scrollbarThumbHeight() {
        int viewportH = listBottom - listY;
        int contentH = settings().getLayersInfo().size() * ROW_H;
        return UiMath.clamp(viewportH * viewportH / Math.max(1, contentH), 24, viewportH);
    }

    private int maxScroll() {
        return Math.max(0, settings().getLayersInfo().size() * ROW_H - (listBottom - listY));
    }

    private void clampScroll() {
        targetScroll = Math.max(0.0f, Math.min(targetScroll, maxScroll()));
        scroll = Math.max(0.0f, Math.min(scroll, maxScroll()));
    }

    @Override
    public void onClose() {
        settings().updateLayers();
        minecraft.setScreen(parent);
    }

    private void drawText(GuiGraphics graphics, String value, float x, float y,
                          float size, int color, float alpha) {
        int resolved = UiColor.alpha(color, alpha);
        MSDFRenderer.draw(graphics, value, x, y, size, resolved);
    }

    private float textWidth(String value, float size) {
        return MSDFRenderer.width(value, size);
    }
}
