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
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PotionBuffHudRenderer {
    private static final int MIN_W = 72;
    private static final int MAX_W = 112;
    private static final int ROW_H = 15;
    private static final int ROW_GAP = 2;
    private static final int PAD_X = 4;
    private static final int ICON = 11;
    private static final float NAME_SIZE = 5.5f;
    private static final float TIME_SIZE = 5.6f;
    private static final float TEXT_Y_OFFSET = 0.6f;
    private static final float BLUR_STRENGTH = 18.0f;
    private static final float BG_ALPHA = 0.62f;
    private static final String PREVIEW_ID = "preview";
    private static final Map<String, RowAnim> rowAnimations = new HashMap<>();
    private static float animatedX;
    private static float animatedY;
    private static float alpha;
    private static boolean animationInitialized;

    private PotionBuffHudRenderer() {
    }

    public static void render(GuiGraphics graphics, boolean editMode) {
        boolean enabled = VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_POTION_BUFF);
        List<RowData> rowsData = rows(editMode);
        boolean visibleTarget = enabled && !rowsData.isEmpty();
        if (!visibleTarget && alpha <= 0.01f && rowAnimations.isEmpty()) {
            return;
        }
        updateRows(rowsData, visibleTarget);
        List<RowAnim> rows = visibleRows();
        Layout layout = layout(rows);
        clampToScreen(layout);
        int targetX = VisualSettings.potionBuffHudX();
        int targetY = VisualSettings.potionBuffHudY();
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
        if (editMode) {
            if (VisualSettings.potionBuffHudBlur()) {
                KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                        x, y, layout.width, layout.height, 5.0f, hudAlpha, BLUR_STRENGTH);
            }
            ShapeRenderer.roundRectBorder(graphics, x, y, layout.width, layout.height, 5, 1,
                    UiColor.alpha(UiPalette.accentSoft(), hudAlpha));
        }

        boolean rightAligned = x + layout.width * 0.5f > window.getGuiScaledWidth() * 0.5f;
        for (RowAnim row : rows) {
            float rowAlpha = hudAlpha * UiMath.clamp01(row.alpha);
            if (rowAlpha <= 0.01f) {
                continue;
            }
            int rowW = rowWidth(row);
            int rowX = rightAligned ? x + layout.width - rowW : x;
            int rowY = y + Math.round(row.y);
            if (VisualSettings.potionBuffHudBlur()) {
                KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                        rowX, rowY, rowW, ROW_H, 4.0f, rowAlpha, BLUR_STRENGTH);
            }
            ShapeRenderer.roundRect(graphics, rowX, rowY, rowW, ROW_H, 4,
                    UiColor.alpha(UiPalette.field(), rowAlpha * BG_ALPHA));
            int sideX = rightAligned ? rowX + rowW - 3 : rowX + 2;
            drawSideProgress(graphics, row, sideX, rowY + 2, ROW_H - 4, rowAlpha);
            int iconY = rowY + Math.round((ROW_H - ICON) * 0.5f);
            int iconX = rightAligned ? rowX + rowW - ICON - 5 : rowX + 5;
            boolean showIcon = VisualSettings.potionBuffHudShowIcons();
            if (showIcon) {
                if (row.effect != null) {
                    drawEffectIcon(graphics, row.effect, iconX, iconY, rowAlpha);
                } else {
                    ShapeRenderer.roundRect(graphics, iconX, iconY, ICON, ICON, 3,
                            UiColor.alpha(UiPalette.accent(), rowAlpha * 0.35f));
                }
            }

            boolean showDuration = VisualSettings.potionBuffHudShowDuration();
            float durationW = showDuration ? MSDFRenderer.width(row.duration, TIME_SIZE) : 0.0f;
            String displayName = row.displayName();
            float nameY = rowY + UiMath.centeredTextY(ROW_H, NAME_SIZE) + TEXT_Y_OFFSET;
            float timeY = rowY + UiMath.centeredTextY(ROW_H, TIME_SIZE) + TEXT_Y_OFFSET;
            if (rightAligned) {
                float timeX = rowX + PAD_X;
                float rightLimit = showIcon ? iconX - 4.0f : rowX + rowW - PAD_X;
                float nameMaxW = rightLimit - timeX - durationW - (showDuration ? 7.0f : 0.0f);
                String name = clip(displayName, nameMaxW, NAME_SIZE);
                float nameW = MSDFRenderer.width(name, NAME_SIZE);
                float nameX = rightLimit - nameW;
                if (showDuration) {
                    MSDFRenderer.draw(graphics, row.duration, timeX, timeY, TIME_SIZE,
                            UiColor.alpha(UiPalette.text(), rowAlpha * 0.86f));
                }
                MSDFRenderer.draw(graphics, name, nameX, nameY, NAME_SIZE,
                        UiColor.alpha(UiPalette.text(), rowAlpha));
            } else {
                float textX = showIcon ? rowX + ICON + 8.0f : rowX + PAD_X + 3.0f;
                String name = clip(displayName, rowW - (textX - rowX) - durationW - PAD_X - (showDuration ? 3.0f : 0.0f), NAME_SIZE);
                MSDFRenderer.draw(graphics, name, textX, nameY, NAME_SIZE,
                        UiColor.alpha(UiPalette.text(), rowAlpha));
                if (showDuration) {
                    MSDFRenderer.draw(graphics, row.duration, rowX + rowW - durationW - PAD_X,
                            timeY, TIME_SIZE,
                            UiColor.alpha(UiPalette.text(), rowAlpha * 0.86f));
                }
            }
        }
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_POTION_BUFF)) {
            return false;
        }
        Layout layout = layout(visibleRows());
        int x = VisualSettings.potionBuffHudX();
        int y = VisualSettings.potionBuffHudY();
        return mouseX >= x && mouseX <= x + layout.width && mouseY >= y && mouseY <= y + layout.height;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Minecraft minecraft = Minecraft.getInstance();
        Layout layout = layout(visibleRows());
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = (int) Math.round(mouseY - offsetY);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, layout.width, layout.height,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        VisualSettings.setPotionBuffHudPosition(snapped.x(), snapped.y());
        animatedX = snapped.x();
        animatedY = snapped.y();
        animationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.potionBuffHudX();
    }

    public static double dragOffsetY(double mouseY) {
        return mouseY - VisualSettings.potionBuffHudY();
    }

    public static int width() {
        return layout(visibleRows()).width;
    }

    public static int height() {
        return layout(visibleRows()).height;
    }

    private static List<RowData> rows(boolean editMode) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return editMode ? List.of(RowData.preview()) : List.of();
        }
        Collection<MobEffectInstance> effects = minecraft.player.getActiveEffects();
        if (effects.isEmpty()) {
            return editMode ? List.of(RowData.preview()) : List.of();
        }
        List<RowData> result = new ArrayList<>();
        for (MobEffectInstance effect : effects) {
            result.add(RowData.from(effect));
        }
        result.sort(Comparator.comparing(RowData::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private static void updateRows(List<RowData> data, boolean visibleTarget) {
        for (RowAnim row : rowAnimations.values()) {
            row.target = false;
        }
        int rowY = 0;
        if (visibleTarget) {
            for (RowData item : data) {
                RowAnim row = rowAnimations.computeIfAbsent(item.id(), RowAnim::new);
                row.name = item.name();
                row.duration = item.duration();
                row.level = item.level();
                row.durationTicks = item.durationTicks();
                row.infinite = item.infinite();
                if (row.maxDurationTicks <= 0 || item.durationTicks() > row.maxDurationTicks || item.infinite()) {
                    row.maxDurationTicks = item.infinite() ? 1 : item.durationTicks();
                }
                row.effect = item.effect();
                row.target = true;
                row.targetY = rowY;
                if (!row.initialized) {
                    row.y = rowY + 5;
                    row.initialized = true;
                }
                rowY += ROW_H + ROW_GAP;
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
        rows.sort(Comparator.comparingDouble(row -> row.targetY));
        return rows;
    }

    private static Layout layout(List<RowAnim> rows) {
        int count = 0;
        int width = MIN_W;
        for (RowAnim row : rows) {
            if (row.alpha > 0.01f) {
                count++;
                width = Math.max(width, rowWidth(row));
            }
        }
        int height = count == 0 ? ROW_H : count * ROW_H + Math.max(0, count - 1) * ROW_GAP;
        return new Layout(Math.min(MAX_W, width), height);
    }

    private static int rowWidth(RowAnim row) {
        float nameW = MSDFRenderer.width(row.displayName(), NAME_SIZE);
        float durationW = VisualSettings.potionBuffHudShowDuration() ? MSDFRenderer.width(row.duration, TIME_SIZE) : 0.0f;
        float iconW = VisualSettings.potionBuffHudShowIcons() ? ICON + 7.0f : 5.0f;
        return UiMath.clamp(Math.round(iconW + nameW + durationW + 11.0f), MIN_W, MAX_W);
    }

    private static void clampToScreen(Layout layout) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int x = UiMath.clamp(VisualSettings.potionBuffHudX(), 0, Math.max(0, screenW - layout.width));
        int y = UiMath.clamp(VisualSettings.potionBuffHudY(), 0, Math.max(0, screenH - layout.height));
        if (x != VisualSettings.potionBuffHudX() || y != VisualSettings.potionBuffHudY()) {
            VisualSettings.setPotionBuffHudPosition(x, y);
        }
    }

    private static void drawEffectIcon(GuiGraphics graphics, MobEffectInstance effect, int x, int y, float alpha) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureAtlasSprite sprite = minecraft.getMobEffectTextures().get(effect.getEffect());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, UiMath.clamp01(alpha));
            graphics.blit(x, y, 0, ICON, ICON, sprite);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }

    private static void drawSideProgress(GuiGraphics graphics, RowAnim row, int x, int y, int h, float alpha) {
        ShapeRenderer.roundRect(graphics, x, y, 1, h, 1,
                UiColor.alpha(UiPalette.surface(), alpha * 0.46f));
        float progress = row.infinite || row.maxDurationTicks <= 0
                ? 1.0f
                : UiMath.clamp01((float) row.durationTicks / row.maxDurationTicks);
        int fillH = Math.max(1, Math.round(h * progress));
        ShapeRenderer.roundRect(graphics, x, y + h - fillH, 1, fillH, 1,
                UiColor.alpha(effectColor(row.effect), alpha * 0.95f));
    }

    private static int effectColor(MobEffectInstance effect) {
        return !VisualSettings.potionBuffHudEffectColors() || effect == null
                ? UiPalette.accent()
                : 0xFF000000 | effect.getEffect().getColor();
    }

    private static String clip(String text, float maxW, float size) {
        if (MSDFRenderer.width(text, size) <= maxW) {
            return text;
        }
        String suffix = "...";
        String clipped = text;
        while (!clipped.isEmpty() && MSDFRenderer.width(clipped + suffix, size) > maxW) {
            clipped = clipped.substring(0, clipped.length() - 1);
        }
        return clipped.isEmpty() ? suffix : clipped + suffix;
    }

    private static String formatDuration(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) {
            return "\u221E";
        }
        int seconds = Math.max(0, effect.getDuration() / 20);
        int minutes = seconds / 60;
        int rest = seconds % 60;
        return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, rest);
    }

    private static String formatLevel(MobEffectInstance effect) {
        int amplifier = effect.getAmplifier();
        return amplifier <= 0 ? "" : roman(amplifier + 1);
    }

    private static String roman(int value) {
        return switch (value) {
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(value);
        };
    }

    private record RowData(String id, String name, String duration, String level, int durationTicks,
                           boolean infinite, MobEffectInstance effect) {
        private static RowData preview() {
            return new RowData(PREVIEW_ID, "Speed", "1:23", "II", 1660, false, null);
        }

        private static RowData from(MobEffectInstance effect) {
            String id = effect.getEffect().getDescriptionId();
            Component name = Component.translatable(id);
            return new RowData(id, name.getString(), formatDuration(effect), formatLevel(effect),
                    Math.max(0, effect.getDuration()), effect.isInfiniteDuration(), effect);
        }
    }

    private record Layout(int width, int height) {
    }

    private static final class RowAnim {
        private String name = "";
        private String duration = "";
        private String level = "";
        private int durationTicks;
        private int maxDurationTicks;
        private boolean infinite;
        private MobEffectInstance effect;
        private float y;
        private float targetY;
        private float alpha;
        private boolean target;
        private boolean initialized;

        private RowAnim(String id) {
        }

        private String displayName() {
            return level.isBlank() ? name : name + " " + level;
        }
    }
}
