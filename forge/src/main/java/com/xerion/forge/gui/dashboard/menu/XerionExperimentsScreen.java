package com.xerion.forge.gui.dashboard;

import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.ExperimentsScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.ExperimentsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class XerionExperimentsScreen extends XerionSetupScreen {
    private static final int ROW_H = 52;

    private final Screen parent;
    private final PackRepository repository;
    private final Consumer<PackRepository> output;
    private final Map<Pack, Boolean> states = new LinkedHashMap<>();
    private final List<ExperimentRow> rows = new ArrayList<>();
    private int listX;
    private int listY;
    private int listW;
    private int listBottom;
    private float scroll;
    private float targetScroll;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;

    public XerionExperimentsScreen(ExperimentsScreen delegate) {
        super(Component.translatable("experiments_screen.title"));
        ExperimentsScreenAccessor accessor = (ExperimentsScreenAccessor) (Object) delegate;
        this.parent = accessor.xerion$getParent();
        this.repository = accessor.xerion$getPackRepository();
        this.output = accessor.xerion$getOutput();
        for (Pack pack : repository.getAvailablePacks()) {
            if (pack.getPackSource() == PackSource.FEATURE) {
                boolean enabled = repository.getSelectedPacks().contains(pack);
                states.put(pack, enabled);
                rows.add(new ExperimentRow(pack, humanReadableTitle(pack), enabled));
            }
        }
    }

    @Override
    protected void init() {
        layoutPanel(294, 340);
        listX = panelX + 22;
        listY = panelY + HEADER_H + 42;
        listW = panelW - 44;
        listBottom = panelY + panelH - FOOTER_H - 8;

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
        clampScroll();
    }

    private static Component humanReadableTitle(Pack pack) {
        String key = "dataPack." + pack.getId() + ".name";
        return I18n.exists(key) ? Component.translatable(key) : pack.getTitle();
    }

    private void applyAndClose() {
        List<Pack> selected = new ArrayList<>(repository.getSelectedPacks());
        List<Pack> enabledFeatures = new ArrayList<>();
        states.forEach((pack, enabled) -> {
            selected.remove(pack);
            if (enabled) {
                enabledFeatures.add(pack);
            }
        });
        Collections.reverse(enabledFeatures);
        selected.addAll(enabledFeatures);
        repository.setSelected(selected.stream().map(Pack::getId).toList());
        output.accept(repository);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = beginXerionRender(graphics);
        scroll = UiMath.lerp(scroll, targetScroll, UiAnimationTimings.CONTROL);
        if (Math.abs(scroll - targetScroll) < 0.05f) {
            scroll = targetScroll;
        }
        drawHeader(graphics, Component.translatable("selectWorld.experiments"), alpha);
        drawWarning(graphics, alpha);
        drawRows(graphics, mouseX, mouseY, alpha);
        drawScrollbar(graphics, mouseX, mouseY, alpha);
        drawFooterDivider(graphics, alpha);
        prepareWidgets(alpha);
        super.render(graphics, mouseX, mouseY, partialTick);
        finishXerionRender();
    }

    private void drawWarning(GuiGraphics graphics, float alpha) {
        int y = panelY + HEADER_H + 10;
        TextPainter.drawClipped(graphics, Component.translatable("selectWorld.experiments.info").getString(),
                listX, y, listW, 7.0f, UiPalette.statusRed(), alpha);
    }

    private void drawRows(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        graphics.enableScissor(listX - 1, listY, listX + listW + 1, listBottom);
        for (int i = 0; i < rows.size(); i++) {
            ExperimentRow row = rows.get(i);
            int y = Math.round(listY + i * ROW_H - scroll);
            if (y + ROW_H - 5 < listY || y > listBottom) {
                continue;
            }
            int rowH = ROW_H - 5;
            boolean hovered = mouseX >= listX && mouseX <= listX + listW - 9
                    && mouseY >= Math.max(y, listY) && mouseY <= Math.min(y + rowH, listBottom);
            row.hover = UiMath.lerp(row.hover, hovered ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
            row.toggle = UiMath.lerp(row.toggle,
                    states.getOrDefault(row.pack, false) ? 1.0f : 0.0f,
                    UiAnimationTimings.CONTROL);
            ShapeRenderer.roundRect(graphics, listX, y, listW - 9, rowH, 6,
                    UiColor.alpha(UiColor.lerp(UiPalette.surface(), UiPalette.accentSoft(), row.hover * 0.24f),
                            alpha * 0.78f));
            ShapeRenderer.roundRectBorder(graphics, listX, y, listW - 9, rowH, 6, 1,
                    UiColor.alpha(UiColor.lerp(UiPalette.border(), UiPalette.accent(), row.hover * 0.38f), alpha));
            drawText(graphics, row.title.getString(), listX + 12, y + 9,
                    8.5f, UiPalette.text(), alpha);
            String description = row.pack.getDescription().getString();
            if (!description.isBlank()) {
                TextPainter.drawClipped(graphics, description, listX + 12, y + 26,
                        Math.max(70, listW - 100), 6.8f, UiPalette.muted(), alpha);
            }
            int toggleX = listX + listW - 42;
            int toggleY = y + (rowH - 14) / 2;
            ShapeRenderer.roundRect(graphics, toggleX, toggleY, 22, 14, 7,
                    UiColor.lerp(UiPalette.track(), UiPalette.accent(), row.toggle));
            ShapeRenderer.roundRect(graphics, toggleX + 2 + Math.round(row.toggle * 8.0f),
                    toggleY + 2, 10, 10, 5, UiColor.alpha(0xFFFFFFFF, alpha));
        }
        graphics.disableScissor();
    }

    private void drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int x = listX + listW - 4;
        int viewportH = listBottom - listY;
        int thumbH = scrollbarThumbHeight();
        int thumbY = scrollbarThumbY();
        boolean hovered = mouseX >= x - 4 && mouseX <= x + 5
                && mouseY >= listY && mouseY <= listBottom;
        ShapeRenderer.roundRect(graphics, x, listY + 2, 2, viewportH - 4, 1,
                UiColor.alpha(UiPalette.track(), alpha * 0.72f));
        ShapeRenderer.roundRect(graphics, x - 1, thumbY, 4, thumbH, 2,
                UiColor.alpha(hovered || draggingScrollbar ? UiPalette.accent() : UiPalette.accentSoft(), alpha));
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
        if (button == 0 && mouseX >= listX && mouseX <= listX + listW - 9
                && mouseY >= listY && mouseY <= listBottom) {
            int row = (int) ((mouseY - listY + scroll) / ROW_H);
            if (row >= 0 && row < rows.size()) {
                ExperimentRow entry = rows.get(row);
                states.put(entry.pack, !states.getOrDefault(entry.pack, false));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= listX && mouseX <= listX + listW
                && mouseY >= listY && mouseY <= listBottom) {
            targetScroll -= (float) delta * ROW_H * 1.4f;
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
        return Math.max(0, rows.size() * ROW_H - (listBottom - listY));
    }

    private void clampScroll() {
        targetScroll = Math.max(0.0f, Math.min(targetScroll, maxScroll()));
        scroll = Math.max(0.0f, Math.min(scroll, maxScroll()));
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = listX + listW - 4;
        return mouseX >= x - 5 && mouseX <= x + 6
                && mouseY >= listY && mouseY <= listBottom;
    }

    private int scrollbarThumbHeight() {
        int viewportH = listBottom - listY;
        int contentH = rows.size() * ROW_H;
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

    private static final class ExperimentRow {
        private final Pack pack;
        private final Component title;
        private float hover;
        private float toggle;

        private ExperimentRow(Pack pack, Component title, boolean enabled) {
            this.pack = pack;
            this.title = title;
            this.toggle = enabled ? 1.0f : 0.0f;
        }
    }
}
