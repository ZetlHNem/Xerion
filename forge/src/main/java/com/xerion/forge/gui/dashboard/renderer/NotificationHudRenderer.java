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

public final class NotificationHudRenderer {
    private static final int W = 112;
    private static final int H = 26;
    private static final int GAP = 3;
    private static final int MARGIN = 7;
    private static final int LOGO_BOX = 15;
    private static final float TITLE_SIZE = 5.4f;
    private static final float MESSAGE_SIZE = 4.55f;
    private static final float COUNT_SIZE = 4.2f;
    private static final float ICON_SIZE = 5.8f;
    private static final float BLUR_STRENGTH = 7.0f;
    private static final float BG_ALPHA = 0.76f;
    private static final NotificationCenter.Entry MENU_PREVIEW =
            NotificationCenter.preview(NotificationCenter.Kind.SUCCESS, "Notifications", "Preview enabled");

    private NotificationHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        render(graphics, false);
    }

    public static void render(GuiGraphics graphics, boolean menuPreview) {
        if (!menuPreview && !VisualSettings.notificationsEnabled() && NotificationCenter.entries().isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        NotificationCenter.trimExpired(now);
        List<NotificationCenter.Entry> all = new ArrayList<>(NotificationCenter.entries());
        if (menuPreview) {
            all.add(MENU_PREVIEW);
        }
        if (all.isEmpty()) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        int screenW = window.getGuiScaledWidth();
        int screenH = window.getGuiScaledHeight();
        Layout layout = layout(screenW, screenH);
        List<NotificationCenter.Entry> activeVisible = visibleEntries(all, now);
        Map<Long, Integer> targetIndices = new HashMap<>();
        for (int i = 0; i < activeVisible.size(); i++) {
            targetIndices.put(activeVisible.get(i).id, i);
        }

        for (NotificationCenter.Entry entry : all) {
            Integer index = targetIndices.get(entry.id);
            boolean active = index != null;
            if (!active && !entry.initialized) {
                continue;
            }
            float targetAlpha = active ? 1.0f : 0.0f;
            float targetX = layout.x;
            float targetY = active ? layout.yFor(index) : entry.y;
            if (!entry.initialized) {
                entry.x = layout.slideFromRight ? targetX + 16.0f : targetX - 16.0f;
                entry.y = targetY + (layout.bottom ? 4.0f : -4.0f);
                entry.initialized = true;
            }
            entry.x = UiMath.lerp(entry.x, targetX, 0.28f);
            entry.y = UiMath.lerp(entry.y, targetY, UiAnimationTimings.HUD_MOVE);
            entry.alpha = UiMath.lerp(entry.alpha, targetAlpha, UiAnimationTimings.HUD_FADE);
            float alpha = UiMath.clamp01(entry.alpha);
            if (alpha <= 0.01f) {
                continue;
            }
            drawEntry(graphics, window, entry, Math.round(entry.x), Math.round(entry.y), alpha, now,
                    active ? index + 1 : 0, activeVisible.size(), layout.right);
        }
    }

    private static List<NotificationCenter.Entry> visibleEntries(List<NotificationCenter.Entry> all, long now) {
        List<NotificationCenter.Entry> active = new ArrayList<>();
        for (NotificationCenter.Entry entry : all) {
            if (!entry.expired(now)) {
                active.add(entry);
            }
        }
        int max = Math.max(1, VisualSettings.notificationsMaxVisible());
        int start = Math.max(0, active.size() - max);
        return new ArrayList<>(active.subList(start, active.size()));
    }

    private static void drawEntry(GuiGraphics graphics, Window window, NotificationCenter.Entry entry,
                                  int x, int y, float alpha, long now, int index, int total, boolean rightAligned) {
        if (VisualSettings.notificationsBlur()) {
            KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                    x, y, W, H, 6.0f, alpha, BLUR_STRENGTH);
        }
        ShadowRenderer.shadow(graphics, x, y, W, H, 5, ShadowStyle.card(), alpha * 0.56f);
        ShapeRenderer.roundRect(graphics, x, y, W, H, 5,
                UiColor.alpha(UiPalette.surface(), alpha * BG_ALPHA));

        int color = kindColor(entry.kind);
        int logoX = x + 5;
        int textX = x + 25;
        int textMaxW = W - 56;
        int countRight = x + W - 9;
        int barX = x + W - 5;

        ShapeRenderer.roundRect(graphics, logoX, y + 5, LOGO_BOX, LOGO_BOX, 4,
                UiColor.alpha(UiPalette.field(), alpha * 0.82f));
        float iconW = MSDFRenderer.iconWidth(featureIcon(entry), ICON_SIZE);
        float iconX = logoX + (LOGO_BOX - iconW) * 0.5f;
        float iconY = y + 5 + UiMath.centeredTextY(LOGO_BOX, ICON_SIZE) + 0.8f;
        MSDFRenderer.drawIconCodepoint(graphics, featureIcon(entry), iconX, iconY, ICON_SIZE,
                UiColor.alpha(color, alpha));

        String title = clip(entry.title, textMaxW, TITLE_SIZE);
        String message = clip(entry.message, textMaxW, MESSAGE_SIZE);
        MSDFRenderer.draw(graphics, title, textX, y + 4.6f, TITLE_SIZE,
                UiColor.alpha(UiPalette.text(), alpha * 0.96f));
        if (!message.isBlank()) {
            MSDFRenderer.draw(graphics, message, textX, y + 14.7f, MESSAGE_SIZE,
                    UiColor.alpha(UiPalette.muted(), alpha));
        }

        if (index > 0 && total > 1) {
            String count = index + " of " + total;
            float countW = MSDFRenderer.width(count, COUNT_SIZE);
            MSDFRenderer.draw(graphics, count, countRight - countW,
                    y + 5.2f, COUNT_SIZE, UiColor.alpha(UiPalette.muted(), alpha * 0.82f));
        }

        int trackH = H - 8;
        ShapeRenderer.roundRect(graphics, barX, y + 4, 2, trackH, 1,
                UiColor.alpha(UiPalette.field(), alpha * 0.78f));
        int fillH = Math.max(1, Math.round(trackH * entry.progress(now)));
        if (fillH > 0) {
            int fillY = y + 4 + trackH - fillH;
            ShapeRenderer.roundRect(graphics, barX, fillY, 2, fillH, 1,
                    UiColor.alpha(color, alpha));
        }
    }

    private static int featureIcon(NotificationCenter.Entry entry) {
        String text = (entry.title + " " + entry.message).toLowerCase(java.util.Locale.ROOT);
        if (text.contains("config") || text.contains("profile")) {
            return FontAwesomeIcons.ICON_FA_FLOPPY_DISK;
        }
        if (text.contains("chat")) {
            return FontAwesomeIcons.ICON_FA_COMMENT;
        }
        if (text.contains("sound") || text.contains("volume")) {
            return FontAwesomeIcons.ICON_FA_VOLUME_HIGH;
        }
        if (text.contains("speed")) {
            return FontAwesomeIcons.ICON_FA_GAUGE_HIGH;
        }
        if (text.contains("fish")) {
            return FontAwesomeIcons.ICON_FA_FISH;
        }
        if (text.contains("eat") || text.contains("gap")) {
            return FontAwesomeIcons.ICON_FA_APPLE_WHOLE;
        }
        if (text.contains("x-ray") || text.contains("esp") || text.contains("visual")) {
            return FontAwesomeIcons.ICON_FA_EYE;
        }
        if (text.contains("combat") || text.contains("aura") || text.contains("target")) {
            return FontAwesomeIcons.ICON_FA_CROSSHAIRS;
        }
        if (text.contains("inventory") || text.contains("backpack") || text.contains("chest")) {
            return FontAwesomeIcons.ICON_FA_BOX_OPEN;
        }
        if (text.contains("packet") || text.contains("shield")) {
            return FontAwesomeIcons.ICON_FA_SHIELD;
        }
        if (text.contains("reconnect") || text.contains("server")) {
            return FontAwesomeIcons.ICON_FA_SERVER;
        }
        return kindIcon(entry.kind);
    }

    private static int kindColor(NotificationCenter.Kind kind) {
        return switch (kind) {
            case SUCCESS -> UiPalette.statusGreen();
            case WARNING -> UiPalette.statusOrange();
            case ERROR -> UiPalette.statusRed();
            case INFO -> UiPalette.accent();
        };
    }

    private static int kindIcon(NotificationCenter.Kind kind) {
        return switch (kind) {
            case SUCCESS -> FontAwesomeIcons.ICON_FA_CHECK;
            case WARNING -> FontAwesomeIcons.ICON_FA_TRIANGLE_EXCLAMATION;
            case ERROR -> FontAwesomeIcons.ICON_FA_XMARK;
            case INFO -> FontAwesomeIcons.ICON_FA_CIRCLE_INFO;
        };
    }

    private static String clip(String text, float maxW, float size) {
        if (text == null || text.isBlank()) {
            return "";
        }
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

    private static Layout layout(int screenW, int screenH) {
        String pos = VisualSettings.notificationsPosition();
        boolean right = !pos.contains("Left");
        boolean bottom = !pos.contains("Top");
        int x = right ? screenW - W - MARGIN : MARGIN;
        int y = bottom ? screenH - H - MARGIN : MARGIN;
        return new Layout(x, y, bottom, right, right);
    }

    private record Layout(int x, int y, boolean bottom, boolean slideFromRight, boolean right) {
        private int yFor(int index) {
            int step = H + GAP;
            return bottom ? y - index * step : y + index * step;
        }
    }
}
