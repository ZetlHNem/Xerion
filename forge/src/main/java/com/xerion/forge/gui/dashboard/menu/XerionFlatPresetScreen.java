package com.xerion.forge.gui.dashboard;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

final class XerionFlatPresetScreen extends Screen {
    private static final int PANEL_MAX_W = 620;
    private static final int PANEL_MAX_H = 330;
    private static final int PANEL_MARGIN = 16;
    private static final int HEADER_H = 58;
    private static final int FOOTER_H = 54;
    private static final int FIELD_H = 28;
    private static final int ROW_H = 38;

    private final XerionFlatWorldScreen parent;
    private final List<PresetEntry> presets = new ArrayList<>();
    private FlatLevelGeneratorSettings workingSettings;
    private EditBox export;
    private XerionMenuActionButton selectButton;
    private int selectedRow = -1;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int fieldX;
    private int fieldY;
    private int fieldW;
    private int listX;
    private int listY;
    private int listW;
    private int listBottom;
    private float screenAlpha;
    private float scroll;
    private float targetScroll;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;

    XerionFlatPresetScreen(XerionFlatWorldScreen parent) {
        super(Component.translatable("createWorld.customize.presets.title"));
        this.parent = parent;
        this.workingSettings = parent.settings();
    }

    @Override
    protected void init() {
        String previousValue = export == null ? null : export.getValue();
        panelW = Math.min(PANEL_MAX_W, Math.max(320, width - PANEL_MARGIN * 2));
        panelH = Math.min(PANEL_MAX_H, Math.max(286, height - PANEL_MARGIN * 2));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        fieldX = panelX + 22;
        fieldY = panelY + HEADER_H + 19;
        fieldW = panelW - 44;
        listX = fieldX;
        listY = fieldY + FIELD_H + 31;
        listW = fieldW;
        listBottom = panelY + panelH - FOOTER_H - 7;

        export = new AlignedEditBox(font, fieldX + 10, fieldY,
                fieldW - 20, FIELD_H,
                Component.translatable("createWorld.customize.presets.share"), 9);
        export.setBordered(false);
        export.setMaxLength(1230);
        export.setTextColor(UiPalette.inputText());
        export.setTextColorUneditable(UiPalette.muted());
        export.setValue(previousValue == null ? save(workingSettings) : previousValue);
        export.setResponder(value -> updateButtonState());
        addRenderableWidget(export);

        loadPresets();
        buildFooter();
        clampScroll();
        updateButtonState();
        setInitialFocus(export);
    }

    private void loadPresets() {
        presets.clear();
        WorldCreationContext context = parent.worldParent().getUiState().getSettings();
        RegistryAccess registries = context.worldgenLoadContext();
        FeatureFlagSet features = context.dataConfiguration().enabledFeatures();
        for (Holder<FlatLevelGeneratorPreset> holder : registries
                .registryOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET)
                .getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
            Set<Block> disabledBlocks = holder.value().settings().getLayersInfo().stream()
                    .map(layer -> layer.getBlockState().getBlock())
                    .filter(block -> !block.isEnabled(features))
                    .collect(Collectors.toSet());
            if (!disabledBlocks.isEmpty()) {
                continue;
            }
            Component name = holder.unwrapKey()
                    .<Component>map(key -> Component.translatable(
                            key.location().toLanguageKey("flat_world_preset")))
                    .orElse(PresetFlatWorldScreen.UNKNOWN_PRESET);
            presets.add(new PresetEntry(holder.value(), name));
        }
        if (selectedRow >= presets.size()) {
            selectedRow = -1;
        }
    }

    private void buildFooter() {
        int gap = 6;
        int right = panelX + panelW - 18;
        int y = panelY + panelH - 40;
        int buttonW = Math.min(142, (panelW - 42) / 2);
        selectButton = addRenderableWidget(new XerionMenuActionButton(
                right - buttonW, y, buttonW, 26,
                Component.translatable("createWorld.customize.presets.select"),
                FontAwesomeIcons.ICON_FA_CHECK, XerionMenuActionButton.Style.PRIMARY,
                this::applyPreset));
        addRenderableWidget(new XerionMenuActionButton(
                right - buttonW * 2 - gap, y, buttonW, 26,
                Component.translatable("gui.cancel"), FontAwesomeIcons.ICON_FA_XMARK,
                XerionMenuActionButton.Style.NORMAL, this::onClose));
    }

    private void selectPreset(int row) {
        if (row < 0 || row >= presets.size()) {
            return;
        }
        selectedRow = row;
        workingSettings = presets.get(row).preset.settings();
        export.setValue(save(workingSettings));
        export.moveCursorToStart();
        updateButtonState();
    }

    private void applyPreset() {
        WorldCreationContext context = parent.worldParent().getUiState().getSettings();
        RegistryAccess registries = context.worldgenLoadContext();
        FeatureFlagSet features = context.dataConfiguration().enabledFeatures();
        HolderGetter<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);
        HolderGetter<StructureSet> structures = registries.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderGetter<PlacedFeature> placedFeatures = registries.lookupOrThrow(Registries.PLACED_FEATURE);
        HolderGetter<Block> blocks = registries.lookupOrThrow(Registries.BLOCK).filterFeatures(features);
        FlatLevelGeneratorSettings parsed = PresetFlatWorldScreen.fromString(
                blocks, biomes, structures, placedFeatures, export.getValue(), workingSettings);
        parent.setConfig(parsed);
        minecraft.setScreen(parent);
    }

    private void updateButtonState() {
        if (selectButton != null && export != null) {
            selectButton.active = selectedRow >= 0 || export.getValue().length() > 1;
        }
    }

    @Override
    public void tick() {
        export.tick();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String value = export == null ? null : export.getValue();
        init(minecraft, width, height);
        if (value != null) {
            export.setValue(value);
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
        drawExportField(graphics, alpha);
        drawPresetList(graphics, mouseX, mouseY, alpha);
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
        drawText(graphics, Component.translatable("createWorld.customize.presets.list").getString(),
                panelX + 20, panelY + 39, 7.5f, UiPalette.muted(), alpha);
        graphics.fill(panelX + 16, panelY + HEADER_H,
                panelX + panelW - 16, panelY + HEADER_H + 1,
                UiColor.alpha(UiPalette.border(), alpha * 0.82f));
    }

    private void drawExportField(GuiGraphics graphics, float alpha) {
        drawText(graphics, Component.translatable("createWorld.customize.presets.share").getString(),
                fieldX, fieldY - 11, 7.5f, UiPalette.muted(), alpha);
        int border = export.isFocused() ? UiPalette.accent() : UiPalette.border();
        ShapeRenderer.roundRect(graphics, fieldX, fieldY, fieldW, FIELD_H, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.90f));
        ShapeRenderer.roundRectBorder(graphics, fieldX, fieldY, fieldW, FIELD_H, 6, 1,
                UiColor.alpha(border, alpha));
        drawText(graphics, Component.translatable("createWorld.customize.presets.list").getString(),
                listX, listY - 13, 7.5f, UiPalette.muted(), alpha);
    }

    private void drawPresetList(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        graphics.enableScissor(listX - 1, listY, listX + listW + 1, listBottom);
        for (int row = 0; row < presets.size(); row++) {
            int y = Math.round(listY + row * ROW_H - scroll);
            if (y + ROW_H - 5 < listY || y > listBottom) {
                continue;
            }
            drawPresetRow(graphics, row, presets.get(row), y, mouseX, mouseY, alpha);
        }
        graphics.disableScissor();
    }

    private void drawPresetRow(GuiGraphics graphics, int row, PresetEntry entry, int y,
                               int mouseX, int mouseY, float alpha) {
        int rowH = ROW_H - 5;
        boolean hovered = mouseX >= listX && mouseX <= listX + listW - 10
                && mouseY >= Math.max(y, listY) && mouseY <= Math.min(y + rowH, listBottom);
        entry.hover = UiMath.lerp(entry.hover, hovered ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
        boolean selected = row == selectedRow;
        float mix = selected ? 0.72f : entry.hover * 0.28f;
        int fill = UiColor.lerp(UiPalette.surface(), UiPalette.accentSoft(), mix);
        int border = selected ? UiPalette.accent()
                : UiColor.lerp(UiPalette.border(), UiPalette.accent(), entry.hover * 0.42f);
        ShapeRenderer.roundRect(graphics, listX, y, listW - 10, rowH, 6,
                UiColor.alpha(fill, alpha * 0.76f));
        ShapeRenderer.roundRectBorder(graphics, listX, y, listW - 10, rowH, 6, 1,
                UiColor.alpha(border, alpha));
        if (selected) {
            ShapeRenderer.roundRect(graphics, listX, y + 5, 3, rowH - 10, 2,
                    UiColor.alpha(UiPalette.accent(), alpha));
        }

        int itemX = listX + 10;
        int itemY = y + (rowH - 16) / 2;
        ShapeRenderer.roundRect(graphics, itemX - 2, itemY - 2, 20, 20, 5,
                UiColor.alpha(UiPalette.field(), alpha * 0.92f));
        graphics.renderFakeItem(new ItemStack(entry.preset.displayItem().value()), itemX, itemY);
        drawText(graphics, entry.name.getString(), itemX + 25,
                MSDFRenderer.textCenteredY(y, rowH, 8.5f), 8.5f,
                UiPalette.text(), alpha);
    }

    private void drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int x = listX + listW - 5;
        int viewportH = listBottom - listY;
        int thumbH = scrollbarThumbHeight();
        int thumbY = scrollbarThumbY();
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
            if (row >= 0 && row < presets.size()) {
                selectPreset(row);
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
        int travel = listBottom - listY - scrollbarThumbHeight();
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
        int contentH = presets.size() * ROW_H;
        return UiMath.clamp(viewportH * viewportH / Math.max(1, contentH), 24, viewportH);
    }

    private int maxScroll() {
        return Math.max(0, presets.size() * ROW_H - (listBottom - listY));
    }

    private void clampScroll() {
        targetScroll = Math.max(0.0f, Math.min(targetScroll, maxScroll()));
        scroll = Math.max(0.0f, Math.min(scroll, maxScroll()));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private static String save(FlatLevelGeneratorSettings settings) {
        StringJoiner layers = new StringJoiner(",");
        settings.getLayersInfo().forEach(layer -> layers.add(layer.toString()));
        ResourceKey<Biome> biome = settings.getBiome().unwrapKey()
                .orElseThrow(() -> new IllegalStateException("Biome not registered"));
        return layers + ";" + biome.location();
    }

    private void drawText(GuiGraphics graphics, String value, float x, float y,
                          float size, int color, float alpha) {
        int resolved = UiColor.alpha(color, alpha);
        MSDFRenderer.draw(graphics, value, x, y, size, resolved);
    }

    private static final class PresetEntry {
        private final FlatLevelGeneratorPreset preset;
        private final Component name;
        private float hover;

        private PresetEntry(FlatLevelGeneratorPreset preset, Component name) {
            this.preset = preset;
            this.name = name;
        }
    }
}
