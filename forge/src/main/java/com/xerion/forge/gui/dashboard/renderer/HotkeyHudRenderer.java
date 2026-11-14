package com.xerion.forge.gui.dashboard;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HotkeyHudRenderer {
    private static final int PAD_X = UiMetrics.HotkeyHud.PAD_X;
    private static final int TITLE_H = UiMetrics.HotkeyHud.TITLE_H;
    private static final int ITEM_PAD = UiMetrics.HotkeyHud.ITEM_PAD;
    private static final int ITEM_GAP = UiMetrics.HotkeyHud.ITEM_GAP;
    private static final int COLUMN_GAP = UiMetrics.HotkeyHud.COLUMN_GAP;
    private static final int MIN_W = UiMetrics.HotkeyHud.MIN_W;
    private static final float TITLE_SIZE = UiMetrics.HotkeyHud.TITLE_SIZE;
    private static final float TEXT_SIZE = UiMetrics.HotkeyHud.TEXT_SIZE;
    private static final float ICON_SIZE = UiMetrics.HotkeyHud.ICON_SIZE;
    private static final float HOTKEY_BLUR_STRENGTH = 18.0f;
    private static final float TITLE_BG_ALPHA = 0.86f;
    private static final float ITEM_BG_ALPHA = 0.62f;
    private static final Map<String, RowAnim> rowAnimations = new HashMap<>();
    private static float animatedX;
    private static float animatedY;
    private static float animatedTitleWidth;
    private static float alpha;
    private static boolean animationInitialized;

    private HotkeyHudRenderer() {
    }

    public static void render(GuiGraphics graphics, boolean editMode) {
        boolean enabled = VisualSettings.hotkeyHudEnabled();
        List<HotkeyHudEntry> entries = WidgetSurface.hotkeyHudEntries();
        boolean visibleTarget = enabled && (!entries.isEmpty() || editMode);
        if (!visibleTarget && alpha <= 0.01f && rowAnimations.isEmpty()) {
            return;
        }
        updateRows(entries, visibleTarget);
        List<RowAnim> rows = visibleRows();
        Layout targetLayout = layoutAnimatedRows(rows);
        if (animatedTitleWidth <= 0.0f) {
            animatedTitleWidth = targetLayout.titleWidth;
        }
        animatedTitleWidth = UiMath.lerp(animatedTitleWidth, targetLayout.titleWidth, UiAnimationTimings.HUD_MOVE);
        if (Math.abs(animatedTitleWidth - targetLayout.titleWidth) < 0.35f) {
            animatedTitleWidth = targetLayout.titleWidth;
        }
        Layout layout = targetLayout.withTitleWidth(Math.round(animatedTitleWidth));
        clampToScreen(layout);
        int targetX = VisualSettings.hotkeyHudX();
        int targetY = VisualSettings.hotkeyHudY();
        if (!animationInitialized) {
            animatedX = targetX;
            animatedY = targetY;
            animationInitialized = true;
        }
        animatedX = UiMath.lerp(animatedX, targetX, UiAnimationTimings.HUD_MOVE);
        animatedY = UiMath.lerp(animatedY, targetY, UiAnimationTimings.HUD_MOVE);
        alpha = UiMath.lerp(alpha, visibleTarget ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        float hudAlpha = UiMath.clamp01(alpha);
        if (hudAlpha <= 0.01f && rows.isEmpty()) {
            return;
        }
        int x = Math.round(animatedX);
        int y = Math.round(animatedY);
        Window window = Minecraft.getInstance().getWindow();

        if (VisualSettings.hotkeyHudBlur()) {
            KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                    x, y, layout.titleWidth, TITLE_H, 4.0f, hudAlpha, HOTKEY_BLUR_STRENGTH);
        }
        ShadowRenderer.shadow(graphics, x, y, layout.titleWidth, TITLE_H, 4,
                ShadowStyle.card(), hudAlpha * TITLE_BG_ALPHA);
        ShapeRenderer.roundRect(graphics, x, y, layout.titleWidth, TITLE_H, 4,
                UiColor.alpha(UiPalette.surface(), hudAlpha * TITLE_BG_ALPHA));
        if (editMode) {
            ShapeRenderer.roundRectBorder(graphics, x, y, layout.titleWidth, TITLE_H, 4, 1,
                    UiColor.alpha(UiPalette.accentSoft(), hudAlpha));
        }

        float iconX = x + PAD_X;
        float titleTextX = iconX + MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_KEYBOARD, ICON_SIZE) + 4.0f;
        float titleTextY = y + UiMath.centeredTextY(TITLE_H, TITLE_SIZE);
        float iconY = y + UiMath.centeredTextY(TITLE_H, ICON_SIZE) + 1.0f;
        int titleColor = UiPalette.text();
        if (VisualSettings.hotkeyHudShowTitle()) {
            MSDFRenderer.drawIconCodepoint(graphics, FontAwesomeIcons.ICON_FA_KEYBOARD, iconX, iconY, ICON_SIZE,
                    UiColor.alpha(UiPalette.accent(), hudAlpha));
            MSDFRenderer.draw(graphics, "Hotkeys", titleTextX, titleTextY, TITLE_SIZE,
                    UiColor.alpha(titleColor, hudAlpha));
        }

        int leftBgX = x;
        for (RowAnim row : rows) {
            float rowAlpha = hudAlpha * UiMath.clamp01(row.alpha);
            if (rowAlpha <= 0.01f) {
                continue;
            }
            String value = row.value == null || row.value.isBlank() ? "-" : row.value;
            String name = row.name == null || row.name.isBlank() ? "-" : row.name;
            float valueW = MSDFRenderer.width(value, TEXT_SIZE);
            float nameW = MSDFRenderer.width(name, TEXT_SIZE);
            int leftBgW = Math.round(valueW) + ITEM_PAD * 2;
            int rightBgX = leftBgX + leftBgW + COLUMN_GAP;
            int rightBgW = Math.round(nameW) + ITEM_PAD * 2;
            int boxH = itemBoxHeight();
            int rowY = y + Math.round(row.y);
            float itemAlpha = rowAlpha * ITEM_BG_ALPHA;
            if (VisualSettings.hotkeyHudBlur()) {
                KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                        leftBgX, rowY, leftBgW, boxH, 4.0f, itemAlpha, HOTKEY_BLUR_STRENGTH);
                KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                        rightBgX, rowY, rightBgW, boxH, 4.0f, itemAlpha, HOTKEY_BLUR_STRENGTH);
            }
            ShapeRenderer.roundRect(graphics, leftBgX, rowY, leftBgW, boxH, 4,
                    UiColor.alpha(UiPalette.field(), itemAlpha));
            ShapeRenderer.roundRect(graphics, rightBgX, rowY, rightBgW, boxH, 4,
                    UiColor.alpha(UiPalette.field(), itemAlpha));
            float textY = rowY + UiMath.centeredTextY(boxH, TEXT_SIZE);
            MSDFRenderer.draw(graphics, value, leftBgX + ITEM_PAD, textY, TEXT_SIZE,
                    UiColor.alpha(UiPalette.text(), rowAlpha));
            MSDFRenderer.draw(graphics, name, rightBgX + ITEM_PAD, textY, TEXT_SIZE,
                    UiColor.alpha(UiPalette.text(), rowAlpha));
        }
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!VisualSettings.hotkeyHudEnabled()) {
            return false;
        }
        Layout layout = currentLayout();
        int x = VisualSettings.hotkeyHudX();
        int y = VisualSettings.hotkeyHudY();
        return mouseX >= x && mouseX <= x + layout.titleWidth && mouseY >= y && mouseY <= y + TITLE_H;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Layout layout = currentLayout();
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = (int) Math.round(mouseY - offsetY);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, layout.titleWidth, TITLE_H, screenW, screenH);
        VisualSettings.setHotkeyHudPosition(snapped.x(), snapped.y());
        animatedX = snapped.x();
        animatedY = snapped.y();
        animationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.hotkeyHudX();
    }

    public static double dragOffsetY(double mouseY) {
        return mouseY - VisualSettings.hotkeyHudY();
    }

    public static int width() {
        return currentLayout().titleWidth;
    }

    public static int height() {
        return TITLE_H;
    }

    private static void clampToScreen(Layout layout) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int x = UiMath.clamp(VisualSettings.hotkeyHudX(), 0, Math.max(0, screenW - layout.width));
        int y = UiMath.clamp(VisualSettings.hotkeyHudY(), 0, Math.max(0, screenH - layout.height));
        if (x != VisualSettings.hotkeyHudX() || y != VisualSettings.hotkeyHudY()) {
            VisualSettings.setHotkeyHudPosition(x, y);
        }
    }

    private static Layout layout(List<HotkeyHudEntry> entries) {
        List<RowAnim> rows = new ArrayList<>();
        int rowY = TITLE_H + 5;
        for (HotkeyHudEntry entry : entries) {
            RowAnim row = new RowAnim(key(entry), entry.value(), entry.name());
            row.y = rowY;
            row.alpha = 1.0f;
            row.target = true;
            rows.add(row);
            rowY += itemBoxHeight() + ITEM_GAP;
        }
        return layoutAnimatedRows(rows);
    }

    private static Layout currentLayout() {
        Layout target = layout(WidgetSurface.hotkeyHudEntries());
        if (animatedTitleWidth <= 0.0f) {
            return target;
        }
        return target.withTitleWidth(Math.round(animatedTitleWidth));
    }

    private static Layout layoutAnimatedRows(List<RowAnim> rows) {
        int titleContentW = Math.round(MSDFRenderer.iconWidth(FontAwesomeIcons.ICON_FA_KEYBOARD, ICON_SIZE)
                + 4.0f + MSDFRenderer.width("Hotkeys", TITLE_SIZE));
        int titleW = titleContentW + PAD_X * 2 + 5;
        int maxWindowW = 0;
        int rowCount = 0;
        for (RowAnim row : rows) {
            if (row.alpha <= 0.01f) {
                continue;
            }
            rowCount++;
            String value = row.value == null || row.value.isBlank() ? "-" : row.value;
            String name = row.name == null || row.name.isBlank() ? "-" : row.name;
            int itemW = Math.round(MSDFRenderer.width(value, TEXT_SIZE) + COLUMN_GAP
                    + MSDFRenderer.width(name, TEXT_SIZE) + ITEM_PAD * 4);
            maxWindowW = Math.max(maxWindowW, itemW);
        }
        int contentW = maxWindowW;
        int width = Math.max(MIN_W, Math.max(titleW, contentW));
        int adaptiveTitleW = Math.min(width, Math.max(titleW, contentW));
        int itemBoxH = itemBoxHeight();
        int height = TITLE_H + (rowCount == 0 ? 0 : 5 + rowCount * itemBoxH + Math.max(0, rowCount - 1) * ITEM_GAP);
        return new Layout(width, height, adaptiveTitleW);
    }

    private static void updateRows(List<HotkeyHudEntry> entries, boolean visibleTarget) {
        for (RowAnim row : rowAnimations.values()) {
            row.target = false;
        }
        int rowY = TITLE_H + 5;
        if (visibleTarget) {
            for (HotkeyHudEntry entry : entries) {
                String key = key(entry);
                RowAnim row = rowAnimations.computeIfAbsent(key,
                        unused -> new RowAnim(key, entry.value(), entry.name()));
                row.value = entry.value();
                row.name = entry.name();
                row.target = true;
                row.targetY = rowY;
                if (!row.initialized) {
                    row.y = rowY + 5;
                    row.initialized = true;
                }
                rowY += itemBoxHeight() + ITEM_GAP;
            }
        }
        rowAnimations.values().removeIf(row -> {
            row.alpha = UiMath.lerp(row.alpha, row.target ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
            row.y = UiMath.lerp(row.y, row.targetY, UiAnimationTimings.HUD_MOVE);
            return !row.target && row.alpha <= 0.02f;
        });
    }

    private static List<RowAnim> visibleRows() {
        List<RowAnim> rows = new ArrayList<>(rowAnimations.values());
        rows.sort((left, right) -> Float.compare(left.targetY, right.targetY));
        return rows;
    }

    private static String key(HotkeyHudEntry entry) {
        return entry.key() == null ? "" : entry.key();
    }

    private static int itemBoxHeight() {
        return Math.round(TEXT_SIZE) + ITEM_PAD * 2;
    }

    private static int themeColor(int light, int dark) {
        return UiPalette.dark() ? dark : light;
    }

    private record Layout(int width, int height, int titleWidth) {
        private Layout withTitleWidth(int nextTitleWidth) {
            int safeTitleWidth = Math.max(MIN_W, nextTitleWidth);
            return new Layout(Math.max(width, safeTitleWidth), height, safeTitleWidth);
        }
    }

    private static final class RowAnim {
        private String value;
        private String name;
        private float y;
        private float targetY;
        private float alpha;
        private boolean target;
        private boolean initialized;

        private RowAnim(String key, String value, String name) {
            this.value = value;
            this.name = name;
        }
    }
}
