package com.xerion.forge.gui.dashboard;

import com.xerion.forge.player.NeteaseMusicService;
import com.xerion.forge.config.VisualSettings;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class MusicLyricsHudRenderer {
    private static final int HEIGHT = 58;
    private static final int MIN_WIDTH = 180;
    private static final int HORIZONTAL_PADDING = 14;
    private static final float LINE_HEIGHT = 17.0f;
    private static boolean enabled;
    private static float alpha;
    private static float animatedWidth = MIN_WIDTH;
    private static float animatedX;
    private static float animatedY;
    private static boolean positionAnimationInitialized;
    private static int lastWidth = MIN_WIDTH;
    private static float animatedLine;
    private static long lyricSongId = -1L;
    private static long lyricTimelineRevision = -1L;

    private MusicLyricsHudRenderer() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void render(GuiGraphics graphics) {
        NeteaseMusicService.Song song = NeteaseMusicService.currentSong();
        List<NeteaseMusicService.LyricLine> lines = NeteaseMusicService.lyrics(song);
        String message = song == null || !lines.isEmpty()
                ? "" : NeteaseMusicService.lyricMessage(song);
        boolean hasContent = !lines.isEmpty() || !message.isBlank();
        boolean visible = enabled && song != null && hasContent;
        alpha = UiMath.lerp(alpha, visible ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        if (alpha <= 0.01f) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        int screenW = window.getGuiScaledWidth();
        int screenH = window.getGuiScaledHeight();
        int activeLine = lines.isEmpty() ? -1 : NeteaseMusicService.currentLyricIndex(song);
        updateLineAnimation(song, activeLine);

        int targetWidth = lyricBoxWidth(screenW, lines, message, activeLine);
        animatedWidth = UiMath.lerp(animatedWidth, targetWidth, UiAnimationTimings.HUD_MOVE);
        if (Math.abs(animatedWidth - targetWidth) < 0.35f) {
            animatedWidth = targetWidth;
        }
        int boxW = Math.min(Math.max(1, screenW - 24), Math.max(MIN_WIDTH, Math.round(animatedWidth)));
        ensurePosition(screenW, screenH, boxW);
        int targetX = VisualSettings.musicLyricsHudX();
        int targetY = VisualSettings.musicLyricsHudY();
        if (!positionAnimationInitialized) {
            animatedX = targetX;
            animatedY = targetY;
            positionAnimationInitialized = true;
        }
        animatedX = UiMath.lerp(animatedX, targetX, UiAnimationTimings.HUD_MOVE);
        animatedY = UiMath.lerp(animatedY, targetY, UiAnimationTimings.HUD_MOVE);
        int x = Math.round(animatedX);
        int y = Math.round(animatedY);
        lastWidth = boxW;
        float hudAlpha = UiMath.clamp01(alpha);

        int contentX = x + HORIZONTAL_PADDING;
        int contentW = Math.max(1, boxW - HORIZONTAL_PADDING * 2);
        graphics.enableScissor(x, y, x + boxW, y + HEIGHT);
        try {
            if (lines.isEmpty()) {
                drawCenteredText(graphics, message, contentX, y + (HEIGHT - 10) / 2,
                        contentW, 10.0f, UiPalette.muted(), hudAlpha);
                return;
            }

            int centerIndex = activeLine < 0 ? 0 : activeLine;
            float centerY = y + HEIGHT * 0.5f;
            int first = Math.max(0, centerIndex - 1);
            int last = Math.min(lines.size() - 1, centerIndex + 1);
            for (int i = first; i <= last; i++) {
                float distance = i - animatedLine;
                float lineY = centerY + distance * LINE_HEIGHT - 5.0f;
                float emphasis = Math.max(0.0f, 1.0f - Math.abs(distance) * 0.24f);
                int color = i == activeLine
                        ? UiPalette.accentDark()
                        : UiColor.lerp(UiPalette.muted(), UiPalette.text(), emphasis * 0.72f);
                float size = i == activeLine ? 10.0f : 9.0f;
                drawCenteredText(graphics, lines.get(i).text(), contentX,
                        Math.round(lineY), contentW, size, color,
                        hudAlpha * Math.max(0.28f, emphasis));
            }
        } finally {
            graphics.disableScissor();
        }
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!enabled || alpha <= 0.01f) {
            return false;
        }
        int x = VisualSettings.musicLyricsHudX();
        int y = VisualSettings.musicLyricsHudY();
        return x != Integer.MIN_VALUE && y != Integer.MIN_VALUE
                && mouseX >= x && mouseX <= x + lastWidth
                && mouseY >= y && mouseY <= y + HEIGHT;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Minecraft minecraft = Minecraft.getInstance();
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = (int) Math.round(mouseY - offsetY);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, lastWidth, HEIGHT,
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        VisualSettings.setMusicLyricsHudPosition(snapped.x(), snapped.y());
        animatedX = snapped.x();
        animatedY = snapped.y();
        positionAnimationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.musicLyricsHudX();
    }

    public static double dragOffsetY(double mouseY) {
        return mouseY - VisualSettings.musicLyricsHudY();
    }

    public static int width() {
        return lastWidth;
    }

    public static int height() {
        return HEIGHT;
    }

    private static void updateLineAnimation(NeteaseMusicService.Song song, int activeLine) {
        long songId = song == null ? -1L : song.id();
        float targetLine = activeLine < 0 ? 0.0f : activeLine;
        long timelineRevision = NeteaseMusicService.lyricTimelineRevision();
        if (songId != lyricSongId
                || timelineRevision != lyricTimelineRevision
                || Math.abs(targetLine - animatedLine) > 3.0f) {
            lyricSongId = songId;
            lyricTimelineRevision = timelineRevision;
            animatedLine = targetLine;
            return;
        }
        animatedLine = UiMath.lerp(animatedLine, targetLine, UiAnimationTimings.CONTROL);
    }

    private static int lyricBoxWidth(int screenW, List<NeteaseMusicService.LyricLine> lines,
                                     String message, int activeLine) {
        int maxTextWidth = TextPainter.msdfWidth(message, 10.0f);
        if (!lines.isEmpty()) {
            int centerIndex = activeLine < 0 ? 0 : activeLine;
            int first = Math.max(0, centerIndex - 1);
            int last = Math.min(lines.size() - 1, centerIndex + 1);
            for (int i = first; i <= last; i++) {
                float size = i == activeLine ? 10.0f : 9.0f;
                maxTextWidth = Math.max(maxTextWidth,
                        TextPainter.msdfWidth(lines.get(i).text(), size));
            }
        }
        return Math.min(Math.max(MIN_WIDTH, screenW - 24),
                Math.max(MIN_WIDTH, maxTextWidth + HORIZONTAL_PADDING * 2));
    }

    private static void ensurePosition(int screenW, int screenH, int width) {
        int x = VisualSettings.musicLyricsHudX();
        int y = VisualSettings.musicLyricsHudY();
        if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
            x = (screenW - width) / 2;
            y = screenH - HEIGHT - 18;
        }
        int clampedX = UiMath.clamp(x, 0, Math.max(0, screenW - width));
        int clampedY = UiMath.clamp(y, 0, Math.max(0, screenH - HEIGHT));
        if (clampedX != VisualSettings.musicLyricsHudX()
                || clampedY != VisualSettings.musicLyricsHudY()) {
            VisualSettings.setMusicLyricsHudPosition(clampedX, clampedY);
        }
    }

    private static void drawCenteredText(GuiGraphics graphics, String value,
                                         int x, int y, int w, float size,
                                         int color, float textAlpha) {
        if (value == null || value.isBlank() || w <= 0) {
            return;
        }
        int textW = Math.min(w, TextPainter.msdfWidth(value, size));
        int textX = x + Math.max(0, (w - textW) / 2);
        TextPainter.drawMsdfClipped(graphics, value,
                textX + 1, y + 1, textW,
                size, 0xD0000000, textAlpha * 0.72f);
        TextPainter.drawMsdfClipped(graphics, value,
                textX, y, textW,
                size, color, textAlpha);
    }
}
