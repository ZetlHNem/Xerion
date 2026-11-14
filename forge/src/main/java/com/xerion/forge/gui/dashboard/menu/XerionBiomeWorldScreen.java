package com.xerion.forge.gui.dashboard;

import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.CreateBuffetWorldScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class XerionBiomeWorldScreen extends XerionSetupScreen {
    private static final int ROW_H = 42;

    private final Screen parent;
    private final Consumer<Holder<Biome>> applySettings;
    private final List<BiomeEntry> allEntries = new ArrayList<>();
    private final List<BiomeEntry> visibleEntries = new ArrayList<>();
    private final Map<ResourceLocation, Float> hoverAnimations = new HashMap<>();
    private Holder<Biome> selectedBiome;
    private AlignedEditBox searchBox;
    private int listX;
    private int listY;
    private int listW;
    private int listBottom;
    private float scroll;
    private float targetScroll;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;

    public XerionBiomeWorldScreen(CreateBuffetWorldScreen delegate) {
        super(delegate.getTitle());
        CreateBuffetWorldScreenAccessor accessor = (CreateBuffetWorldScreenAccessor) (Object) delegate;
        this.parent = accessor.xerion$getParent();
        this.applySettings = accessor.xerion$getApplySettings();
        this.selectedBiome = accessor.xerion$getBiome();
        populateEntries(accessor.xerion$getBiomes());
    }

    private void populateEntries(Registry<Biome> biomes) {
        Collator collator = Collator.getInstance(Locale.getDefault());
        biomes.holders().map(BiomeEntry::new)
                .sorted(Comparator.comparing(entry -> entry.name.getString(), collator))
                .forEach(allEntries::add);
        visibleEntries.addAll(allEntries);
    }

    @Override
    protected void init() {
        layoutPanel(310, 360);
        listX = panelX + 22;
        int searchY = panelY + HEADER_H + 12;
        listY = searchY + 36;
        listW = panelW - 44;
        listBottom = panelY + panelH - FOOTER_H - 8;

        searchBox = new AlignedEditBox(font, listX + 29, searchY,
                listW - 42, 26, Component.translatable("gui.recipebook.search_hint"), 8);
        searchBox.setBordered(false);
        searchBox.setMaxLength(128);
        searchBox.setTextColor(UiPalette.inputText());
        searchBox.setTextColorUneditable(UiPalette.muted());
        searchBox.setHint(Component.translatable("gui.recipebook.search_hint"));
        searchBox.setResponder(this::filterEntries);
        addRenderableWidget(searchBox);

        int y = panelY + panelH - 40;
        int right = panelX + panelW - 18;
        int buttonW = 112;
        addRenderableWidget(new XerionMenuActionButton(
                right - buttonW, y, buttonW, 26, Component.translatable("gui.done"),
                FontAwesomeIcons.ICON_FA_CHECK, XerionMenuActionButton.Style.PRIMARY,
                this::applyAndClose));
        addRenderableWidget(new XerionMenuActionButton(
                right - buttonW * 2 - 6, y, buttonW, 26, Component.translatable("gui.cancel"),
                FontAwesomeIcons.ICON_FA_XMARK, XerionMenuActionButton.Style.NORMAL,
                this::onClose));
        revealSelection();
    }

    private void filterEntries(String query) {
        String filter = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        visibleEntries.clear();
        for (BiomeEntry entry : allEntries) {
            if (filter.isEmpty()
                    || entry.name.getString().toLowerCase(Locale.ROOT).contains(filter)
                    || entry.id.toString().toLowerCase(Locale.ROOT).contains(filter)) {
                visibleEntries.add(entry);
            }
        }
        scroll = 0.0f;
        targetScroll = 0.0f;
        clampScroll();
    }

    private void revealSelection() {
        int index = selectedIndex();
        if (index < 0) {
            return;
        }
        int viewportH = listBottom - listY;
        targetScroll = Math.max(0.0f, index * ROW_H - (viewportH - ROW_H) * 0.5f);
        scroll = targetScroll;
        clampScroll();
    }

    private int selectedIndex() {
        for (int i = 0; i < visibleEntries.size(); i++) {
            if (Objects.equals(visibleEntries.get(i).biome, selectedBiome)) {
                return i;
            }
        }
        return -1;
    }

    private void applyAndClose() {
        if (selectedBiome != null) {
            applySettings.accept(selectedBiome);
        }
        minecraft.setScreen(parent);
    }

    @Override
    public void tick() {
        searchBox.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = beginXerionRender(graphics);
        scroll = UiMath.lerp(scroll, targetScroll, UiAnimationTimings.CONTROL);
        if (Math.abs(scroll - targetScroll) < 0.05f) {
            scroll = targetScroll;
        }
        drawHeader(graphics, Component.translatable("createWorld.customize.buffet.biome"), alpha);
        drawSearch(graphics, alpha);
        drawRows(graphics, mouseX, mouseY, alpha);
        drawScrollbar(graphics, mouseX, mouseY, alpha);
        drawFooterDivider(graphics, alpha);
        prepareWidgets(alpha);
        super.render(graphics, mouseX, mouseY, partialTick);
        finishXerionRender();
    }

    private void drawSearch(GuiGraphics graphics, float alpha) {
        int x = listX;
        int y = searchBox.getY();
        ShapeRenderer.roundRect(graphics, x, y, listW, 26, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.9f));
        ShapeRenderer.roundRectBorder(graphics, x, y, listW, 26, 6, 1,
                UiColor.alpha(searchBox.isFocused() ? UiPalette.accent() : UiPalette.border(), alpha));
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS,
                x + 10, MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS,
                        y, 26, 8.0f), 8.0f, UiColor.alpha(UiPalette.muted(), alpha));
    }

    private void drawRows(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        graphics.enableScissor(listX - 1, listY, listX + listW + 1, listBottom);
        for (int i = 0; i < visibleEntries.size(); i++) {
            BiomeEntry entry = visibleEntries.get(i);
            int y = Math.round(listY + i * ROW_H - scroll);
            if (y + ROW_H - 5 < listY || y > listBottom) {
                continue;
            }
            int rowH = ROW_H - 5;
            boolean hovered = mouseX >= listX && mouseX <= listX + listW - 10
                    && mouseY >= Math.max(y, listY) && mouseY <= Math.min(y + rowH, listBottom);
            boolean selected = Objects.equals(entry.biome, selectedBiome);
            float hover = UiMath.lerp(hoverAnimations.getOrDefault(entry.id, 0.0f),
                    hovered ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
            hoverAnimations.put(entry.id, hover);
            float mix = selected ? 0.72f : hover * 0.28f;
            ShapeRenderer.roundRect(graphics, listX, y, listW - 10, rowH, 6,
                    UiColor.alpha(UiColor.lerp(UiPalette.surface(), UiPalette.accentSoft(), mix),
                            alpha * 0.8f));
            ShapeRenderer.roundRectBorder(graphics, listX, y, listW - 10, rowH, 6, 1,
                    UiColor.alpha(selected ? UiPalette.accent()
                            : UiColor.lerp(UiPalette.border(), UiPalette.accent(), hover * 0.42f), alpha));
            if (selected) {
                ShapeRenderer.roundRect(graphics, listX, y + 5, 3, rowH - 10, 2,
                        UiColor.alpha(UiPalette.accent(), alpha));
            }

            int iconX = listX + 11;
            int iconY = y + (rowH - 22) / 2;
            ShapeRenderer.roundRect(graphics, iconX, iconY, 22, 22, 6,
                    UiColor.alpha(UiPalette.field(), alpha * 0.92f));
            int icon = biomeIcon(entry.id);
            MSDFRenderer.drawIconCodepoint(graphics, icon,
                    iconX + 11 - MSDFRenderer.iconWidth(icon, 9.0f) * 0.5f,
                    MSDFRenderer.iconCenteredY(icon, iconY, 22, 9.0f),
                    9.0f, UiColor.alpha(selected ? UiPalette.accentDark() : UiPalette.accent(), alpha));

            int textX = iconX + 31;
            int textW = listW - 88;
            TextPainter.drawClipped(graphics, entry.name.getString(), textX, y + 7,
                    textW, 8.5f, UiPalette.text(), alpha);
            TextPainter.drawClipped(graphics, entry.id.toString(), textX, y + 23,
                    textW, 6.5f, UiPalette.muted(), alpha);
            if (selected) {
                MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_CHECK,
                        listX + listW - 29,
                        MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_CHECK, y, rowH, 8.0f),
                        8.0f, UiColor.alpha(UiPalette.accent(), alpha));
            }
        }
        graphics.disableScissor();
    }

    private static int biomeIcon(ResourceLocation id) {
        String path = id.getPath();
        if (path.contains("ocean") || path.contains("river")) {
            return FontAwesomeIcons.ICON_FA_WATER;
        }
        if (path.contains("snow") || path.contains("frozen") || path.contains("ice")) {
            return FontAwesomeIcons.ICON_FA_SNOWFLAKE;
        }
        if (path.contains("mountain") || path.contains("peak") || path.contains("hill")) {
            return FontAwesomeIcons.ICON_FA_MOUNTAIN;
        }
        if (path.contains("desert") || path.contains("badlands")) {
            return FontAwesomeIcons.ICON_FA_SUN;
        }
        if (path.contains("forest") || path.contains("jungle") || path.contains("taiga")) {
            return FontAwesomeIcons.ICON_FA_TREE;
        }
        return FontAwesomeIcons.ICON_FA_LEAF;
    }

    private void drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int x = listX + listW - 5;
        int viewportH = listBottom - listY;
        int thumbH = scrollbarThumbHeight();
        int thumbY = scrollbarThumbY();
        boolean hovered = mouseX >= x - 4 && mouseX <= x + 6
                && mouseY >= listY && mouseY <= listBottom;
        ShapeRenderer.roundRect(graphics, x, listY + 2, 2, viewportH - 4, 1,
                UiColor.alpha(UiPalette.track(), alpha * 0.72f));
        ShapeRenderer.roundRect(graphics, x - 1, thumbY, 4, thumbH, 2,
                UiColor.alpha(hovered || draggingScrollbar
                        ? UiPalette.accent() : UiPalette.accentSoft(), alpha));
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
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && mouseX >= listX && mouseX <= listX + listW - 10
                && mouseY >= listY && mouseY <= listBottom) {
            int row = (int) ((mouseY - listY + scroll) / ROW_H);
            if (row >= 0 && row < visibleEntries.size()) {
                selectedBiome = visibleEntries.get(row).biome;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= listX && mouseX <= listX + listW
                && mouseY >= listY && mouseY <= listBottom) {
            targetScroll -= (float) delta * ROW_H * 1.5f;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
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

    private int maxScroll() {
        return Math.max(0, visibleEntries.size() * ROW_H - (listBottom - listY));
    }

    private void clampScroll() {
        targetScroll = Math.max(0.0f, Math.min(targetScroll, maxScroll()));
        scroll = Math.max(0.0f, Math.min(scroll, maxScroll()));
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = listX + listW - 5;
        return mouseX >= x - 4 && mouseX <= x + 6
                && mouseY >= listY && mouseY <= listBottom;
    }

    private int scrollbarThumbHeight() {
        int viewportH = listBottom - listY;
        int contentH = visibleEntries.size() * ROW_H;
        return UiMath.clamp(viewportH * viewportH / Math.max(1, contentH), 24, viewportH);
    }

    private int scrollbarThumbY() {
        int travel = listBottom - listY - scrollbarThumbHeight();
        return listY + Math.round(targetScroll / Math.max(1, maxScroll()) * travel);
    }

    private void updateScrollFromMouse(double mouseY) {
        int travel = listBottom - listY - scrollbarThumbHeight();
        if (travel <= 0) {
            scroll = targetScroll = 0.0f;
            return;
        }
        float ratio = ((float) mouseY - listY - scrollbarDragOffset) / travel;
        scroll = targetScroll = UiMath.clamp01(ratio) * maxScroll();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private static final class BiomeEntry {
        private final Holder.Reference<Biome> biome;
        private final ResourceLocation id;
        private final Component name;

        private BiomeEntry(Holder.Reference<Biome> biome) {
            this.biome = biome;
            this.id = biome.key().location();
            String translationKey = id.toLanguageKey("biome");
            this.name = I18n.exists(translationKey)
                    ? Component.translatable(translationKey)
                    : Component.literal(id.toString());
        }
    }
}
