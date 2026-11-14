package com.xerion.forge.gui.dashboard;

import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.mixin.PackSelectionScreenAccessor;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class XerionDataPackScreen extends XerionSetupScreen {
    private static final int ROW_H = 48;

    private final PackSelectionScreen delegate;
    private final PackSelectionModel model;
    private final Path packDirectory;
    private final PackColumn availableColumn = new PackColumn(false);
    private final PackColumn selectedColumn = new PackColumn(true);
    private XerionMenuActionButton doneButton;
    private String modelSignature = "";

    public XerionDataPackScreen(PackSelectionScreen delegate) {
        super(delegate.getTitle());
        this.delegate = delegate;
        PackSelectionScreenAccessor accessor = (PackSelectionScreenAccessor) (Object) delegate;
        this.model = accessor.xerion$getModel();
        this.packDirectory = accessor.xerion$getPackDirectory();
    }

    @Override
    protected void init() {
        layoutPanel(300, 350);
        delegate.init(minecraft, width, height);

        int gap = 10;
        int listX = panelX + 22;
        int columnW = (panelW - 44 - gap) / 2;
        int listY = panelY + HEADER_H + 30;
        int listBottom = panelY + panelH - FOOTER_H - 8;
        availableColumn.setBounds(listX, listY, columnW, listBottom);
        selectedColumn.setBounds(listX + columnW + gap, listY, columnW, listBottom);
        refreshEntries();

        int y = panelY + panelH - 40;
        int right = panelX + panelW - 18;
        int buttonW = 124;
        doneButton = addRenderableWidget(new XerionMenuActionButton(
                right - buttonW, y, buttonW, 26, Component.translatable("gui.done"),
                FontAwesomeIcons.ICON_FA_CHECK, XerionMenuActionButton.Style.PRIMARY,
                delegate::onClose));
        addRenderableWidget(new XerionMenuActionButton(
                panelX + 18, y, Math.min(150, panelW / 3), 26,
                Component.translatable("pack.openFolder"), FontAwesomeIcons.ICON_FA_FOLDER_OPEN,
                XerionMenuActionButton.Style.NORMAL,
                () -> Util.getPlatform().openUri(packDirectory.toUri())));
        updateDoneButton();
    }

    private void refreshEntries() {
        availableColumn.setEntries(model.getUnselected()
                .filter(PackSelectionModel.Entry::notHidden).toList());
        selectedColumn.setEntries(model.getSelected()
                .filter(PackSelectionModel.Entry::notHidden).toList());
        modelSignature = currentSignature();
        updateDoneButton();
    }

    private String currentSignature() {
        String available = model.getUnselected().filter(PackSelectionModel.Entry::notHidden)
                .map(PackSelectionModel.Entry::getId).collect(Collectors.joining("\u0001"));
        String selected = model.getSelected().filter(PackSelectionModel.Entry::notHidden)
                .map(PackSelectionModel.Entry::getId).collect(Collectors.joining("\u0001"));
        return available + "\u0002" + selected;
    }

    private void updateDoneButton() {
        if (doneButton != null) {
            doneButton.active = !selectedColumn.entries.isEmpty();
        }
    }

    @Override
    public void tick() {
        delegate.tick();
        String signature = currentSignature();
        if (!signature.equals(modelSignature)) {
            refreshEntries();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = beginXerionRender(graphics);
        drawHeader(graphics, Component.translatable("pack.dropInfo"), alpha);
        drawText(graphics, Component.translatable("pack.available.title").getString(),
                availableColumn.x, availableColumn.y - 16, 8.0f, UiPalette.muted(), alpha);
        drawText(graphics, Component.translatable("pack.selected.title").getString(),
                selectedColumn.x, selectedColumn.y - 16, 8.0f, UiPalette.muted(), alpha);
        availableColumn.render(graphics, mouseX, mouseY, alpha);
        selectedColumn.render(graphics, mouseX, mouseY, alpha);
        drawFooterDivider(graphics, alpha);
        prepareWidgets(alpha);
        super.render(graphics, mouseX, mouseY, partialTick);
        finishXerionRender();
    }

    private void addPack(PackSelectionModel.Entry entry) {
        if (!entry.canSelect()) {
            return;
        }
        if (entry.getCompatibility().isCompatible()) {
            entry.select();
            refreshEntries();
            return;
        }
        minecraft.setScreen(new ConfirmScreen(accepted -> {
            minecraft.setScreen(this);
            if (accepted) {
                entry.select();
                refreshEntries();
            }
        }, Component.translatable("pack.incompatible.confirm.title"),
                entry.getCompatibility().getConfirmation()));
    }

    private void removePack(PackSelectionModel.Entry entry) {
        if (entry.canUnselect()) {
            entry.unselect();
            refreshEntries();
        }
    }

    private void movePack(PackSelectionModel.Entry entry, boolean up) {
        if (up ? entry.canMoveUp() : entry.canMoveDown()) {
            if (up) {
                entry.moveUp();
            } else {
                entry.moveDown();
            }
            refreshEntries();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && availableColumn.mouseClicked(mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && selectedColumn.mouseClicked(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (availableColumn.mouseScrolled(mouseX, mouseY, delta)
                || selectedColumn.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button,
                                double dragX, double dragY) {
        if (button == 0 && (availableColumn.mouseDragged(mouseY)
                || selectedColumn.mouseDragged(mouseY))) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (availableColumn.mouseReleased() || selectedColumn.mouseReleased())) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        delegate.onFilesDrop(paths);
    }

    @Override
    public void onClose() {
        delegate.onClose();
    }

    private final class PackColumn {
        private final boolean selected;
        private final List<PackSelectionModel.Entry> entries = new ArrayList<>();
        private int x;
        private int y;
        private int width;
        private int bottom;
        private float scroll;
        private float targetScroll;
        private boolean draggingScrollbar;
        private int scrollbarDragOffset;

        private PackColumn(boolean selected) {
            this.selected = selected;
        }

        private void setBounds(int x, int y, int width, int bottom) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.bottom = bottom;
            clampScroll();
        }

        private void setEntries(List<PackSelectionModel.Entry> newEntries) {
            entries.clear();
            entries.addAll(newEntries);
            clampScroll();
        }

        private void render(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
            scroll = UiMath.lerp(scroll, targetScroll, UiAnimationTimings.CONTROL);
            if (Math.abs(scroll - targetScroll) < 0.05f) {
                scroll = targetScroll;
            }
            graphics.enableScissor(x - 1, y, x + width + 1, bottom);
            for (int i = 0; i < entries.size(); i++) {
                int rowY = Math.round(y + i * ROW_H - scroll);
                if (rowY + ROW_H - 5 < y || rowY > bottom) {
                    continue;
                }
                drawEntry(graphics, entries.get(i), rowY, mouseX, mouseY, alpha);
            }
            graphics.disableScissor();
            drawScrollbar(graphics, mouseX, mouseY, alpha);
        }

        private void drawEntry(GuiGraphics graphics, PackSelectionModel.Entry entry, int rowY,
                               int mouseX, int mouseY, float alpha) {
            int rowH = ROW_H - 5;
            boolean hovered = mouseX >= x && mouseX <= x + width - 9
                    && mouseY >= Math.max(rowY, y) && mouseY <= Math.min(rowY + rowH, bottom);
            ShapeRenderer.roundRect(graphics, x, rowY, width - 9, rowH, 6,
                    UiColor.alpha(hovered ? UiPalette.accentSoft() : UiPalette.surface(),
                            alpha * (hovered ? 0.68f : 0.76f)));
            ShapeRenderer.roundRectBorder(graphics, x, rowY, width - 9, rowH, 6, 1,
                    UiColor.alpha(hovered ? UiPalette.accent() : UiPalette.border(), alpha));

            int iconX = x + 7;
            int iconY = rowY + (rowH - 28) / 2;
            ShapeRenderer.roundRect(graphics, iconX - 2, iconY - 2, 32, 32, 5,
                    UiColor.alpha(UiPalette.field(), alpha * 0.92f));
            graphics.setColor(1.0f, 1.0f, 1.0f, alpha);
            graphics.blit(entry.getIconTexture(), iconX, iconY,
                    0.0f, 0.0f, 28, 28, 28, 28);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            int controlsW = selected ? 58 : 24;
            int textX = iconX + 36;
            int textMaxW = Math.max(40, width - (textX - x) - controlsW - 14);
            String title = entry.getTitle().getString();
            String description = entry.getExtendedDescription().getString();
            int titleColor = entry.getCompatibility().isCompatible()
                    ? UiPalette.text() : UiPalette.statusRed();
            TextPainter.drawClipped(graphics, title, textX, rowY + 9, textMaxW,
                    7.8f, titleColor, alpha);
            TextPainter.drawClipped(graphics, description, textX, rowY + 24, textMaxW,
                    6.4f, UiPalette.muted(), alpha);
            if (selected) {
                drawSelectedControls(graphics, entry, rowY, rowH, mouseX, mouseY, alpha);
            } else {
                drawControlIcon(graphics, FontAwesomeIcons.ICON_FA_PLUS,
                        x + width - 31, rowY, 22, rowH, entry.canSelect(), mouseX, mouseY, alpha);
            }
        }

        private void drawSelectedControls(GuiGraphics graphics, PackSelectionModel.Entry entry,
                                          int rowY, int rowH, int mouseX, int mouseY, float alpha) {
            int startX = x + width - 65;
            drawControlIcon(graphics, FontAwesomeIcons.ICON_FA_ARROW_UP,
                    startX, rowY, 18, rowH, entry.canMoveUp(), mouseX, mouseY, alpha);
            drawControlIcon(graphics, FontAwesomeIcons.ICON_FA_ARROW_DOWN,
                    startX + 18, rowY, 18, rowH, entry.canMoveDown(), mouseX, mouseY, alpha);
            drawControlIcon(graphics, FontAwesomeIcons.ICON_FA_XMARK,
                    startX + 36, rowY, 18, rowH, entry.canUnselect(), mouseX, mouseY, alpha);
        }

        private void drawControlIcon(GuiGraphics graphics, int icon, int controlX, int controlY,
                                     int controlW, int controlH, boolean active,
                                     int mouseX, int mouseY, float alpha) {
            boolean hovered = active && mouseX >= controlX && mouseX <= controlX + controlW
                    && mouseY >= controlY && mouseY <= controlY + controlH;
            if (hovered) {
                ShapeRenderer.roundRect(graphics, controlX + 1, controlY + 6,
                        controlW - 2, controlH - 12, 4,
                        UiColor.alpha(UiPalette.accentSoft(), alpha * 0.72f));
            }
            float iconSize = 7.5f;
            MSDFRenderer.drawIconCodepoint(graphics, icon,
                    controlX + (controlW - MSDFRenderer.iconWidth(icon, iconSize)) * 0.5f,
                    MSDFRenderer.iconCenteredY(icon, controlY, controlH, iconSize),
                    iconSize, UiColor.alpha(active ? UiPalette.accent() : UiPalette.muted(),
                            alpha * (active ? 1.0f : 0.40f)));
        }

        private boolean mouseClicked(double mouseX, double mouseY) {
            if (maxScroll() > 0 && isOverScrollbar(mouseX, mouseY)) {
                int thumbY = scrollbarThumbY();
                int thumbH = scrollbarThumbHeight();
                draggingScrollbar = true;
                scrollbarDragOffset = mouseY >= thumbY && mouseY <= thumbY + thumbH
                        ? (int) mouseY - thumbY : thumbH / 2;
                updateScrollFromMouse(mouseY);
                return true;
            }
            if (mouseX < x || mouseX > x + width - 9 || mouseY < y || mouseY > bottom) {
                return false;
            }
            int row = (int) ((mouseY - y + scroll) / ROW_H);
            if (row < 0 || row >= entries.size()) {
                return false;
            }
            PackSelectionModel.Entry entry = entries.get(row);
            if (!selected) {
                addPack(entry);
                return true;
            }
            int startX = x + width - 65;
            if (mouseX >= startX && mouseX < startX + 18) {
                movePack(entry, true);
                return true;
            }
            if (mouseX >= startX + 18 && mouseX < startX + 36) {
                movePack(entry, false);
                return true;
            }
            if (mouseX >= startX + 36 && mouseX <= startX + 54) {
                removePack(entry);
                return true;
            }
            return true;
        }

        private boolean mouseScrolled(double mouseX, double mouseY, double delta) {
            if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > bottom) {
                return false;
            }
            targetScroll -= (float) delta * ROW_H * 1.4f;
            clampScroll();
            return true;
        }

        private boolean mouseDragged(double mouseY) {
            if (!draggingScrollbar) {
                return false;
            }
            updateScrollFromMouse(mouseY);
            return true;
        }

        private boolean mouseReleased() {
            if (!draggingScrollbar) {
                return false;
            }
            draggingScrollbar = false;
            return true;
        }

        private void drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
            if (maxScroll() <= 0) {
                return;
            }
            int scrollbarX = x + width - 4;
            int viewportH = bottom - y;
            boolean hovered = mouseX >= scrollbarX - 4 && mouseX <= scrollbarX + 5
                    && mouseY >= y && mouseY <= bottom;
            ShapeRenderer.roundRect(graphics, scrollbarX, y + 2, 2, viewportH - 4, 1,
                    UiColor.alpha(UiPalette.track(), alpha * 0.72f));
            ShapeRenderer.roundRect(graphics, scrollbarX - 1, scrollbarThumbY(),
                    4, scrollbarThumbHeight(), 2,
                    UiColor.alpha(hovered || draggingScrollbar
                            ? UiPalette.accent() : UiPalette.accentSoft(), alpha));
        }

        private int maxScroll() {
            return Math.max(0, entries.size() * ROW_H - (bottom - y));
        }

        private void clampScroll() {
            targetScroll = Math.max(0.0f, Math.min(targetScroll, maxScroll()));
            scroll = Math.max(0.0f, Math.min(scroll, maxScroll()));
        }

        private boolean isOverScrollbar(double mouseX, double mouseY) {
            int scrollbarX = x + width - 4;
            return mouseX >= scrollbarX - 5 && mouseX <= scrollbarX + 6
                    && mouseY >= y && mouseY <= bottom;
        }

        private int scrollbarThumbHeight() {
            int viewportH = bottom - y;
            int contentH = entries.size() * ROW_H;
            return UiMath.clamp(viewportH * viewportH / Math.max(1, contentH), 24, viewportH);
        }

        private int scrollbarThumbY() {
            int travel = bottom - y - scrollbarThumbHeight();
            return y + Math.round(targetScroll / Math.max(1, maxScroll()) * travel);
        }

        private void updateScrollFromMouse(double mouseY) {
            int travel = bottom - y - scrollbarThumbHeight();
            if (travel <= 0) {
                scroll = targetScroll = 0.0f;
                return;
            }
            float ratio = ((float) mouseY - y - scrollbarDragOffset) / travel;
            scroll = targetScroll = UiMath.clamp01(ratio) * maxScroll();
        }
    }
}
