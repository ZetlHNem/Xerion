package com.xerion.forge.gui.dashboard;

import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class XerionGameRulesScreen extends XerionSetupScreen {
    private static final int ROW_H = 43;
    private static final int CATEGORY_W = 132;

    private final GameRules gameRules;
    private final Consumer<Optional<GameRules>> exitCallback;
    private final List<RuleRow> rules = new ArrayList<>();
    private final Set<IntegerRuleRow> invalidRules = new HashSet<>();
    private GameRules.Category selectedCategory = GameRules.Category.PLAYER;
    private EditBox searchBox;
    private XerionMenuActionButton doneButton;
    private int contentY;
    private int contentBottom;
    private int categoryX;
    private int listX;
    private int listW;
    private float scroll;
    private float targetScroll;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;
    private String query = "";

    public XerionGameRulesScreen(GameRules gameRules, Consumer<Optional<GameRules>> exitCallback) {
        super(Component.translatable("editGamerule.title"));
        this.gameRules = gameRules;
        this.exitCallback = exitCallback;
    }

    @Override
    protected void init() {
        String previousSearch = searchBox == null ? query : searchBox.getValue();
        layoutPanel(300, 350);
        contentY = panelY + HEADER_H + 10;
        contentBottom = panelY + panelH - FOOTER_H - 8;
        categoryX = panelX + 18;
        listX = categoryX + CATEGORY_W + 12;
        listW = panelX + panelW - 18 - listX;

        if (rules.isEmpty()) {
            buildRules();
        }
        for (RuleRow row : rules) {
            if (row instanceof IntegerRuleRow integerRow) {
                addRenderableWidget(integerRow.input);
                integerRow.input.visible = false;
            }
        }

        int searchW = Math.min(194, Math.max(128, listW / 2));
        int searchX = panelX + panelW - searchW - 18;
        int searchY = panelY + 17;
        searchBox = new AlignedEditBox(font, searchX + 27, searchY,
                searchW - 37, 27, Component.translatable("gui.recipebook.search_hint"), 9);
        searchBox.setBordered(false);
        searchBox.setTextColor(UiPalette.inputText());
        searchBox.setTextColorUneditable(UiPalette.muted());
        searchBox.setHint(Component.translatable("gui.recipebook.search_hint"));
        searchBox.setValue(previousSearch);
        searchBox.setResponder(value -> {
            query = value.toLowerCase(Locale.ROOT);
            targetScroll = 0.0f;
        });
        addRenderableWidget(searchBox);

        int y = panelY + panelH - 40;
        int right = panelX + panelW - 18;
        int buttonW = 112;
        doneButton = addRenderableWidget(new XerionMenuActionButton(
                right - buttonW, y, buttonW, 26, Component.translatable("gui.done"),
                FontAwesomeIcons.ICON_FA_CHECK, XerionMenuActionButton.Style.PRIMARY,
                () -> exitCallback.accept(Optional.of(gameRules))));
        addRenderableWidget(new XerionMenuActionButton(
                right - buttonW * 2 - 6, y, buttonW, 26, Component.translatable("gui.cancel"),
                FontAwesomeIcons.ICON_FA_XMARK, XerionMenuActionButton.Style.NORMAL,
                () -> exitCallback.accept(Optional.empty())));
        updateDoneButton();
        clampScroll();
    }

    private void buildRules() {
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
            @Override
            public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key,
                                     GameRules.Type<GameRules.BooleanValue> type) {
                rules.add(new BooleanRuleRow(key, gameRules.getRule(key)));
            }

            @Override
            public void visitInteger(GameRules.Key<GameRules.IntegerValue> key,
                                     GameRules.Type<GameRules.IntegerValue> type) {
                rules.add(new IntegerRuleRow(key, gameRules.getRule(key)));
            }
        });
        rules.sort((left, right) -> {
            int category = left.key.getCategory().compareTo(right.key.getCategory());
            return category != 0 ? category : left.key.getId().compareTo(right.key.getId());
        });
    }

    private List<RuleRow> visibleRules() {
        if (query.isEmpty()) {
            return rules.stream().filter(row -> row.key.getCategory() == selectedCategory).toList();
        }
        return rules.stream().filter(row -> row.key.getCategory() == selectedCategory)
                .filter(row -> row.key.getId().toLowerCase(Locale.ROOT).contains(query)
                        || row.label.getString().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    @Override
    public void tick() {
        searchBox.tick();
        for (RuleRow row : rules) {
            if (row instanceof IntegerRuleRow integerRow) {
                integerRow.input.tick();
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = beginXerionRender(graphics);
        scroll = UiMath.lerp(scroll, targetScroll, UiAnimationTimings.CONTROL);
        if (Math.abs(scroll - targetScroll) < 0.05f) {
            scroll = targetScroll;
        }
        drawHeader(graphics, Component.translatable("selectWorld.gameRules"), alpha);
        drawSearchBackground(graphics, alpha);
        drawCategories(graphics, mouseX, mouseY, alpha);
        drawRules(graphics, mouseX, mouseY, alpha);
        drawScrollbar(graphics, mouseX, mouseY, alpha);
        drawFooterDivider(graphics, alpha);
        prepareWidgets(alpha);
        super.render(graphics, mouseX, mouseY, partialTick);
        finishXerionRender();
    }

    private void drawSearchBackground(GuiGraphics graphics, float alpha) {
        int x = searchBox.getX() - 27;
        int y = searchBox.getY();
        int w = searchBox.getWidth() + 37;
        int border = searchBox.isFocused() ? UiPalette.accent() : UiPalette.border();
        ShapeRenderer.roundRect(graphics, x, y, w, 27, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.90f));
        ShapeRenderer.roundRectBorder(graphics, x, y, w, 27, 6, 1,
                UiColor.alpha(border, alpha));
        MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS,
                x + 9, MSDFRenderer.iconCenteredY(FontAwesomeIcons.ICON_FA_MAGNIFYING_GLASS,
                        y, 27, 8.0f), 8.0f, UiColor.alpha(UiPalette.muted(), alpha));
    }

    private void drawCategories(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        GameRules.Category[] categories = GameRules.Category.values();
        int gap = 5;
        int rowH = Math.max(24, (contentBottom - contentY - gap * (categories.length - 1)) / categories.length);
        for (int i = 0; i < categories.length; i++) {
            GameRules.Category category = categories[i];
            int y = contentY + i * (rowH + gap);
            boolean selected = category == selectedCategory;
            boolean hovered = mouseX >= categoryX && mouseX <= categoryX + CATEGORY_W
                    && mouseY >= y && mouseY <= y + rowH;
            float mix = selected ? 0.70f : hovered ? 0.25f : 0.0f;
            ShapeRenderer.roundRect(graphics, categoryX, y, CATEGORY_W, rowH, 6,
                    UiColor.alpha(UiColor.lerp(UiPalette.surface(), UiPalette.accentSoft(), mix),
                            alpha * 0.82f));
            ShapeRenderer.roundRectBorder(graphics, categoryX, y, CATEGORY_W, rowH, 6, 1,
                    UiColor.alpha(selected ? UiPalette.accent() : UiPalette.border(), alpha));
            if (selected) {
                ShapeRenderer.roundRect(graphics, categoryX, y + 5, 3, rowH - 10, 2,
                        UiColor.alpha(UiPalette.accent(), alpha));
            }
            drawText(graphics, Component.translatable(category.getDescriptionId()).getString(),
                    categoryX + 12, MSDFRenderer.textCenteredY(y, rowH, 8.0f),
                    8.0f, selected ? UiPalette.accent() : UiPalette.text(), alpha);
        }
    }

    private void drawRules(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        for (RuleRow row : rules) {
            if (row instanceof IntegerRuleRow integerRow) {
                integerRow.input.visible = false;
            }
        }
        List<RuleRow> visible = visibleRules();
        graphics.enableScissor(listX - 1, contentY, listX + listW + 1, contentBottom);
        for (int i = 0; i < visible.size(); i++) {
            int y = Math.round(contentY + i * ROW_H - scroll);
            if (y + ROW_H - 5 < contentY || y > contentBottom) {
                continue;
            }
            RuleRow row = visible.get(i);
            int rowH = ROW_H - 5;
            boolean hovered = mouseX >= listX && mouseX <= listX + listW - 9
                    && mouseY >= Math.max(y, contentY) && mouseY <= Math.min(y + rowH, contentBottom);
            row.hover = UiMath.lerp(row.hover, hovered ? 1.0f : 0.0f, UiAnimationTimings.CONTROL);
            ShapeRenderer.roundRect(graphics, listX, y, listW - 9, rowH, 6,
                    UiColor.alpha(UiColor.lerp(UiPalette.surface(), UiPalette.accentSoft(), row.hover * 0.24f),
                            alpha * 0.76f));
            ShapeRenderer.roundRectBorder(graphics, listX, y, listW - 9, rowH, 6, 1,
                    UiColor.alpha(UiColor.lerp(UiPalette.border(), UiPalette.accent(), row.hover * 0.38f), alpha));
            drawText(graphics, row.label.getString(), listX + 11, y + 8,
                    8.2f, UiPalette.text(), alpha);
            drawText(graphics, row.key.getId(), listX + 11, y + 22,
                    6.8f, UiPalette.muted(), alpha);
            if (row instanceof BooleanRuleRow booleanRow) {
                drawToggle(graphics, booleanRow, y, rowH, alpha);
            } else if (row instanceof IntegerRuleRow integerRow) {
                drawIntegerInput(graphics, integerRow, y, rowH, alpha);
            }
        }
        graphics.disableScissor();
    }

    private void drawToggle(GuiGraphics graphics, BooleanRuleRow row, int y, int rowH, float alpha) {
        row.toggle = UiMath.lerp(row.toggle, row.value.get() ? 1.0f : 0.0f,
                UiAnimationTimings.CONTROL);
        int x = listX + listW - 42;
        int toggleY = y + (rowH - 14) / 2;
        ShapeRenderer.roundRect(graphics, x, toggleY, 22, 14, 7,
                UiColor.lerp(UiPalette.track(), UiPalette.accent(), row.toggle));
        ShapeRenderer.roundRect(graphics, x + 2 + Math.round(row.toggle * 8.0f),
                toggleY + 2, 10, 10, 5, UiColor.alpha(0xFFFFFFFF, alpha));
    }

    private void drawIntegerInput(GuiGraphics graphics, IntegerRuleRow row, int y, int rowH,
                                  float alpha) {
        int inputW = 70;
        int inputX = listX + listW - inputW - 19;
        int inputY = y + (rowH - 24) / 2;
        row.input.setX(inputX + 8);
        row.input.setY(inputY);
        row.input.setWidth(inputW - 16);
        row.input.visible = inputY >= contentY - 1 && inputY + 24 <= contentBottom + 1;
        if (!row.input.visible) {
            return;
        }
        int border = invalidRules.contains(row) ? UiPalette.statusRed()
                : row.input.isFocused() ? UiPalette.accent() : UiPalette.border();
        ShapeRenderer.roundRect(graphics, inputX, inputY, inputW, 24, 6,
                UiColor.alpha(UiPalette.field(), alpha * 0.92f));
        ShapeRenderer.roundRectBorder(graphics, inputX, inputY, inputW, 24, 6, 1,
                UiColor.alpha(border, alpha));
    }

    private void drawScrollbar(GuiGraphics graphics, int mouseX, int mouseY, float alpha) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int x = listX + listW - 4;
        int viewportH = contentBottom - contentY;
        int thumbH = scrollbarThumbHeight();
        int thumbY = scrollbarThumbY();
        boolean hovered = mouseX >= x - 4 && mouseX <= x + 5
                && mouseY >= contentY && mouseY <= contentBottom;
        ShapeRenderer.roundRect(graphics, x, contentY + 2, 2, viewportH - 4, 1,
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
        if (button != 0) {
            return false;
        }
        GameRules.Category[] categories = GameRules.Category.values();
        int gap = 5;
        int rowH = Math.max(24, (contentBottom - contentY - gap * (categories.length - 1)) / categories.length);
        for (int i = 0; i < categories.length; i++) {
            int y = contentY + i * (rowH + gap);
            if (mouseX >= categoryX && mouseX <= categoryX + CATEGORY_W
                    && mouseY >= y && mouseY <= y + rowH) {
                selectedCategory = categories[i];
                scroll = targetScroll = 0.0f;
                return true;
            }
        }
        if (mouseX >= listX && mouseX <= listX + listW - 9
                && mouseY >= contentY && mouseY <= contentBottom) {
            List<RuleRow> visible = visibleRules();
            int rowIndex = (int) ((mouseY - contentY + scroll) / ROW_H);
            if (rowIndex >= 0 && rowIndex < visible.size()
                    && visible.get(rowIndex) instanceof BooleanRuleRow booleanRow) {
                booleanRow.value.set(!booleanRow.value.get(), (MinecraftServer) null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= listX && mouseX <= listX + listW
                && mouseY >= contentY && mouseY <= contentBottom) {
            targetScroll -= (float) delta * ROW_H * 1.55f;
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
        return Math.max(0, visibleRules().size() * ROW_H - (contentBottom - contentY));
    }

    private void clampScroll() {
        targetScroll = Math.max(0.0f, Math.min(targetScroll, maxScroll()));
        scroll = Math.max(0.0f, Math.min(scroll, maxScroll()));
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int x = listX + listW - 4;
        return mouseX >= x - 5 && mouseX <= x + 6
                && mouseY >= contentY && mouseY <= contentBottom;
    }

    private int scrollbarThumbHeight() {
        int viewportH = contentBottom - contentY;
        int contentH = visibleRules().size() * ROW_H;
        return UiMath.clamp(viewportH * viewportH / Math.max(1, contentH), 24, viewportH);
    }

    private int scrollbarThumbY() {
        int travel = contentBottom - contentY - scrollbarThumbHeight();
        return contentY + Math.round(targetScroll / Math.max(1, maxScroll()) * travel);
    }

    private void updateScrollFromMouse(double mouseY) {
        int travel = contentBottom - contentY - scrollbarThumbHeight();
        if (travel <= 0) {
            scroll = targetScroll = 0.0f;
            return;
        }
        float ratio = ((float) mouseY - contentY - scrollbarDragOffset) / travel;
        scroll = targetScroll = UiMath.clamp01(ratio) * maxScroll();
    }

    private void updateDoneButton() {
        if (doneButton != null) {
            doneButton.active = invalidRules.isEmpty();
        }
    }

    @Override
    public void onClose() {
        exitCallback.accept(Optional.empty());
    }

    private abstract static class RuleRow {
        private final GameRules.Key<?> key;
        private final Component label;
        private float hover;

        private RuleRow(GameRules.Key<?> key) {
            this.key = key;
            this.label = Component.translatable(key.getDescriptionId());
        }
    }

    private static final class BooleanRuleRow extends RuleRow {
        private final GameRules.BooleanValue value;
        private float toggle;

        private BooleanRuleRow(GameRules.Key<GameRules.BooleanValue> key,
                               GameRules.BooleanValue value) {
            super(key);
            this.value = value;
            this.toggle = value.get() ? 1.0f : 0.0f;
        }
    }

    private final class IntegerRuleRow extends RuleRow {
        private final GameRules.IntegerValue value;
        private final EditBox input;

        private IntegerRuleRow(GameRules.Key<GameRules.IntegerValue> key,
                               GameRules.IntegerValue value) {
            super(key);
            this.value = value;
            this.input = new AlignedEditBox(font, 0, 0, 54, 24,
                    Component.translatable(key.getDescriptionId()), 7);
            input.setBordered(false);
            input.setMaxLength(16);
            input.setTextColor(UiPalette.inputText());
            input.setTextColorUneditable(UiPalette.muted());
            input.setValue(Integer.toString(value.get()));
            input.setResponder(text -> {
                if (value.tryDeserialize(text)) {
                    invalidRules.remove(this);
                } else {
                    invalidRules.add(this);
                }
                updateDoneButton();
            });
        }
    }
}
