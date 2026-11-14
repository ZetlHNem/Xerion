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

import com.xerion.forge.config.ProfileConfigStore;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.gui.MinecraftFps;
import com.xerion.forge.gui.font.FontAwesomeIcons;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.Window;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WatermarkHudRenderer {
    private static final int H = 14;
    private static final int PAD_X = UiMetrics.HotkeyHud.PAD_X + 2;
    private static final int SEGMENT_GAP = 6;
    private static final int ICON_GAP = 3;
    private static final float TEXT_SIZE = UiMetrics.HotkeyHud.TEXT_SIZE;
    private static final float ICON_SIZE = UiMetrics.HotkeyHud.ICON_SIZE;
    private static final int LOGO_SIZE = 9;
    private static final float WATERMARK_BLUR_STRENGTH = 18.0f;
    private static final float WATERMARK_BG_ALPHA = 0.82f;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static float animatedX;
    private static float animatedY;
    private static float alpha;
    private static boolean animationInitialized;
    private static int lastLayoutWidth;
    private static boolean layoutWidthInitialized;
    private static float animatedLayoutWidth;
    private static boolean animatedLayoutWidthInitialized;
    private static final Map<Integer, Float> segmentAnimatedX = new HashMap<>();
    private static final Map<Integer, Float> segmentAlpha = new HashMap<>();
    private static final Map<Integer, Segment> segmentCache = new HashMap<>();
    private static int draggedElement;
    private static float draggedMouseX;
    private static float draggedGrabOffsetX;
    private static int lastRenderedX;
    private static boolean segmentPositionInitialized;
    private static float draggedSegmentAlpha;
    private static float fadingDraggedX;
    private static float fadingDraggedY;
    private static float fadingDraggedW;
    private static float fadingDraggedH;

    private WatermarkHudRenderer() {
    }

    public static void render(GuiGraphics graphics, boolean editMode) {
        boolean visibleTarget = VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_WATERMARK);
        Layout layout = layout();
        clampToScreen(layout);
        int targetX = VisualSettings.watermarkX();
        int targetY = VisualSettings.watermarkY();
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        if (!animationInitialized) {
            animatedX = targetX;
            animatedY = targetY;
            animationInitialized = true;
        }
        animatedX = UiMath.lerp(animatedX, targetX, UiAnimationTimings.HUD_MOVE);
        animatedY = UiMath.lerp(animatedY, targetY, UiAnimationTimings.HUD_MOVE);
        alpha = UiMath.lerp(alpha, visibleTarget ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        float hudAlpha = UiMath.clamp01(alpha);
        if (hudAlpha <= 0.01f) {
            return;
        }

        int x = Math.round(animatedX);
        int y = Math.round(animatedY);
        if (!animatedLayoutWidthInitialized) {
            animatedLayoutWidth = layout.w();
            animatedLayoutWidthInitialized = true;
        }
        animatedLayoutWidth = UiMath.lerp(animatedLayoutWidth, layout.w(), UiAnimationTimings.HUD_MOVE);
        if (Math.abs(animatedLayoutWidth - layout.w()) < 0.35f) {
            animatedLayoutWidth = layout.w();
        }
        int drawW = Math.max(32, Math.round(animatedLayoutWidth));
        boolean rightAnchored = targetX + layout.w() * 0.5f > screenW * 0.5f;
        int baseX = rightAnchored ? targetX + layout.w() - drawW : x;
        if (!segmentPositionInitialized) {
            lastRenderedX = baseX;
            segmentPositionInitialized = true;
        } else if (lastRenderedX != baseX) {
            int dx = baseX - lastRenderedX;
            segmentAnimatedX.replaceAll((element, value) -> value + dx);
            lastRenderedX = baseX;
        }
        draggedSegmentAlpha = UiMath.lerp(draggedSegmentAlpha, draggedElement != 0 ? 1.0f : 0.0f, UiAnimationTimings.HUD_FADE);
        int bg = UiColor.alpha(UiPalette.surface(), hudAlpha * WATERMARK_BG_ALPHA);
        drawWatermarkBlur(graphics, baseX, y, drawW, H, 7.0f, hudAlpha);
        ShadowRenderer.shadow(graphics, baseX, y, drawW, H, 7, ShadowStyle.card(), hudAlpha);
        ShapeRenderer.roundRect(graphics, baseX, y, drawW, H, 7, bg);
        if (editMode) {
            ShapeRenderer.roundRectBorder(graphics, baseX, y, drawW, H, 7, 1,
                    UiColor.alpha(UiPalette.accentSoft(), hudAlpha));
        }

        int textColor = UiColor.alpha(UiPalette.text(), hudAlpha);
        int iconColor = UiColor.alpha(UiPalette.accent(), hudAlpha);
        int cursorX = baseX + PAD_X;
        Segment draggedSegment = null;
        float draggedX = 0.0f;
        List<Integer> targetElements = layout.segments().stream().map(Segment::element).toList();
        for (Segment segment : layout.segments()) {
            segmentCache.put(segment.element(), segment);
            float currentAlpha = segmentAlpha.getOrDefault(segment.element(), 0.0f);
            segmentAlpha.put(segment.element(), UiMath.lerp(currentAlpha, 1.0f, UiAnimationTimings.HUD_FADE));
        }
        for (Integer element : new ArrayList<>(segmentCache.keySet())) {
            if (targetElements.contains(element)) {
                continue;
            }
            float currentAlpha = segmentAlpha.getOrDefault(element, 1.0f);
            float nextAlpha = UiMath.lerp(currentAlpha, 0.0f, UiAnimationTimings.HUD_FADE);
            if (nextAlpha <= 0.02f) {
                segmentAlpha.remove(element);
                segmentCache.remove(element);
                segmentAnimatedX.remove(element);
            } else {
                segmentAlpha.put(element, nextAlpha);
            }
        }
        enableScissor(baseX, y, drawW, H);
        try {
            for (Segment segment : layout.segments()) {
                int slotW = segment.slotWidth();
                float targetSegmentX = cursorX;
                float segmentX;
                if (segment.element() == draggedElement) {
                    segmentX = UiMath.clamp(draggedMouseX - draggedGrabOffsetX,
                            baseX + PAD_X, baseX + drawW - PAD_X - slotW);
                    draggedSegment = segment;
                    draggedX = segmentX;
                } else {
                    float current = segmentAnimatedX.getOrDefault(segment.element(), targetSegmentX);
                    segmentX = UiMath.lerp(current, targetSegmentX, UiAnimationTimings.HUD_MOVE);
                    if (Math.abs(segmentX - targetSegmentX) < 0.35f) {
                        segmentX = targetSegmentX;
                    }
                    segmentAnimatedX.put(segment.element(), segmentX);
                    float elementAlpha = hudAlpha * UiMath.clamp01(segmentAlpha.getOrDefault(segment.element(), 1.0f));
                    drawSegment(graphics, segment, segmentX, y,
                            UiColor.alpha(UiPalette.text(), elementAlpha),
                            UiColor.alpha(UiPalette.accent(), elementAlpha));
                }
                cursorX += slotW + SEGMENT_GAP;
            }
            for (Map.Entry<Integer, Segment> entry : segmentCache.entrySet()) {
                int element = entry.getKey();
                if (targetElements.contains(element) || element == draggedElement) {
                    continue;
                }
                float elementAlpha = hudAlpha * UiMath.clamp01(segmentAlpha.getOrDefault(element, 0.0f));
                if (elementAlpha <= 0.01f) {
                    continue;
                }
                drawSegment(graphics, entry.getValue(), segmentAnimatedX.getOrDefault(element, (float) baseX + PAD_X), y,
                        UiColor.alpha(UiPalette.text(), elementAlpha),
                        UiColor.alpha(UiPalette.accent(), elementAlpha));
            }
            if (draggedSegment != null) {
                segmentAnimatedX.put(draggedSegment.element(), draggedX);
                fadingDraggedX = draggedX - 2.0f;
                fadingDraggedY = y + 1.0f;
                fadingDraggedW = draggedSegment.slotWidth() + 4.0f;
                fadingDraggedH = H - 2.0f;
                RenderSystem.disableScissor();
                graphics.flush();
                KawaseBlurPipeline.refreshOverlaySample();
                drawWatermarkBlur(graphics, fadingDraggedX, fadingDraggedY, fadingDraggedW, fadingDraggedH,
                        4.0f, hudAlpha * UiMath.clamp01(draggedSegmentAlpha));
                enableScissor(baseX, y, drawW, H);
                float elementAlpha = hudAlpha * UiMath.clamp01(segmentAlpha.getOrDefault(draggedSegment.element(), 1.0f));
                drawSegment(graphics, draggedSegment, draggedX, y,
                        UiColor.alpha(UiPalette.text(), elementAlpha),
                        UiColor.alpha(UiPalette.accent(), elementAlpha));
            } else if (draggedSegmentAlpha > 0.01f && fadingDraggedW > 0.0f && fadingDraggedH > 0.0f) {
                RenderSystem.disableScissor();
                graphics.flush();
                KawaseBlurPipeline.refreshOverlaySample();
                drawWatermarkBlur(graphics, fadingDraggedX, fadingDraggedY, fadingDraggedW, fadingDraggedH,
                        4.0f, hudAlpha * UiMath.clamp01(draggedSegmentAlpha));
                enableScissor(baseX, y, drawW, H);
            }
        } finally {
            RenderSystem.disableScissor();
        }
    }

    public static boolean contains(double mouseX, double mouseY) {
        if (!VisualSettings.hudWidgetEnabled(VisualSettings.HUD_WIDGET_WATERMARK)) {
            return false;
        }
        return containsVisible(mouseX, mouseY);
    }

    public static boolean containsVisible(double mouseX, double mouseY) {
        Layout layout = layout();
        int x = VisualSettings.watermarkX();
        int y = VisualSettings.watermarkY();
        return mouseX >= x && mouseX <= x + layout.w() && mouseY >= y && mouseY <= y + H;
    }

    public static int width() {
        return layout().w();
    }

    public static int height() {
        return H;
    }

    public static void moveTo(double mouseX, double mouseY, double offsetX, double offsetY) {
        Layout layout = layout();
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int rawX = (int) Math.round(mouseX - offsetX);
        int rawY = (int) Math.round(mouseY - offsetY);
        HudSnap.Position snapped = HudSnap.snap(rawX, rawY, layout.w(), H, screenW, screenH);
        VisualSettings.setWatermarkPosition(snapped.x(), snapped.y());
        animatedX = snapped.x();
        animatedY = snapped.y();
        animationInitialized = true;
    }

    public static double dragOffsetX(double mouseX) {
        return mouseX - VisualSettings.watermarkX();
    }

    public static double dragOffsetY(double mouseY) {
        return mouseY - VisualSettings.watermarkY();
    }

    public static int elementAt(double mouseX, double mouseY) {
        Layout layout = layout();
        int x = VisualSettings.watermarkX();
        int y = VisualSettings.watermarkY();
        if (mouseY < y || mouseY > y + H || mouseX < x || mouseX > x + layout.w()) {
            return 0;
        }
        int cursorX = x + PAD_X;
        for (Segment segment : layout.segments()) {
            int endX = cursorX + segment.slotWidth();
            if (mouseX >= cursorX && mouseX <= endX) {
                return segment.element();
            }
            cursorX = endX + SEGMENT_GAP;
        }
        return 0;
    }

    public static void reorderElementAt(int element, double mouseX) {
        if (element == 0) {
            return;
        }
        setDraggedElement(element, mouseX);
        Layout layout = layout();
        List<Segment> segments = layout.segments();
        if (segments.size() <= 1) {
            return;
        }
        Integer targetElement = null;
        int cursorX = VisualSettings.watermarkX() + PAD_X;
        for (Segment segment : segments) {
            if (segment.element() == element) {
                cursorX += segment.slotWidth() + SEGMENT_GAP;
                continue;
            }
            float center = cursorX + segment.slotWidth() * 0.5f;
            if (mouseX < center) {
                targetElement = segment.element();
                break;
            }
            cursorX += segment.slotWidth() + SEGMENT_GAP;
        }
        int[] order = VisualSettings.watermarkElementOrder();
        List<Integer> next = new ArrayList<>();
        for (int value : order) {
            if (value != element) {
                next.add(value);
            }
        }
        int insertIndex = next.size();
        if (targetElement != null) {
            int targetIndex = next.indexOf(targetElement);
            if (targetIndex >= 0) {
                insertIndex = targetIndex;
            }
        }
        next.add(insertIndex, element);
        int[] nextOrder = new int[next.size()];
        for (int i = 0; i < next.size(); i++) {
            nextOrder[i] = next.get(i);
        }
        VisualSettings.setWatermarkElementOrder(nextOrder);
    }

    public static void setDraggedElement(int element, double mouseX) {
        draggedElement = element;
        draggedMouseX = (float) mouseX;
    }

    public static void beginDraggedElement(int element, double mouseX) {
        draggedElement = element;
        draggedMouseX = (float) mouseX;
        draggedGrabOffsetX = Math.max(0.0f, (float) mouseX - elementDrawX(element));
    }

    public static void clearDraggedElement() {
        draggedElement = 0;
    }

    private static float elementDrawX(int element) {
        Layout layout = layout();
        int cursorX = VisualSettings.watermarkX() + PAD_X;
        for (Segment segment : layout.segments()) {
            if (segment.element() == element) {
                return segmentAnimatedX.getOrDefault(element, (float) cursorX);
            }
            cursorX += segment.slotWidth() + SEGMENT_GAP;
        }
        return VisualSettings.watermarkX() + PAD_X;
    }

    private static void clampToScreen(Layout layout) {
        Minecraft minecraft = Minecraft.getInstance();
        int screenW = minecraft.getWindow().getGuiScaledWidth();
        int screenH = minecraft.getWindow().getGuiScaledHeight();
        int currentX = VisualSettings.watermarkX();
        if (layoutWidthInitialized && lastLayoutWidth != layout.w()) {
            boolean rightAnchored = currentX + lastLayoutWidth * 0.5f > screenW * 0.5f;
            if (rightAnchored) {
                currentX += lastLayoutWidth - layout.w();
            }
        }
        int x = UiMath.clamp(currentX, 0, Math.max(0, screenW - layout.w()));
        int y = UiMath.clamp(VisualSettings.watermarkY(), 0, Math.max(0, screenH - H));
        if (x != VisualSettings.watermarkX() || y != VisualSettings.watermarkY()) {
            VisualSettings.setWatermarkPosition(x, y);
        }
        lastLayoutWidth = layout.w();
        layoutWidthInitialized = true;
    }

    private static Layout layout() {
        List<Segment> segments = segments();
        int w = PAD_X * 2;
        for (Segment segment : segments) {
            w += segment.slotWidth() + SEGMENT_GAP;
        }
        if (!segments.isEmpty()) {
            w -= SEGMENT_GAP;
        }
        return new Layout(Math.max(32, w), segments);
    }

    private static List<Segment> segments() {
        Minecraft minecraft = Minecraft.getInstance();
        List<Segment> segments = new ArrayList<>();
        for (int element : VisualSettings.watermarkElementOrder()) {
            if (!VisualSettings.watermarkElementEnabled(element)) {
                continue;
            }
            Segment segment = segmentFor(minecraft, element);
            if (segment != null) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private static Segment segmentFor(Minecraft minecraft, int element) {
        return switch (element) {
            case VisualSettings.WATERMARK_ELEMENT_LOGO ->
                    logoSegment(element, "Xerion", "Xerion");
            case VisualSettings.WATERMARK_ELEMENT_PLAYER -> {
                LocalPlayer player = minecraft.player;
                String name = player == null ? "Player" : player.getGameProfile().getName();
                yield segment(element, FontAwesomeIcons.ICON_FA_USER, name, name);
            }
            case VisualSettings.WATERMARK_ELEMENT_CONFIG -> {
                String profile = ProfileConfigStore.lastLoadedProfile();
                String label = profile == null || profile.isBlank() ? "default" : profile;
                yield segment(element, FontAwesomeIcons.ICON_FA_FLOPPY_DISK, label, label);
            }
            case VisualSettings.WATERMARK_ELEMENT_PING -> {
                String label = pingLabel(minecraft);
                yield segment(element, FontAwesomeIcons.ICON_FA_SIGNAL, label, label);
            }
            case VisualSettings.WATERMARK_ELEMENT_FPS -> {
                String label = MinecraftFps.read() + " FPS";
                yield segment(element, FontAwesomeIcons.ICON_FA_CHART_BAR, label, label);
            }
            case VisualSettings.WATERMARK_ELEMENT_SERVER -> {
                String label = serverLabel(minecraft);
                yield segment(element, FontAwesomeIcons.ICON_FA_SERVER, label, label);
            }
            case VisualSettings.WATERMARK_ELEMENT_TIME -> {
                String label = LocalTime.now().format(TIME_FORMAT);
                yield segment(element, FontAwesomeIcons.ICON_FA_CLOCK, label, label);
            }
            default -> null;
        };
    }

    private static Segment segment(int element, int icon, String text, String maxText) {
        int iconW = Math.round(MSDFRenderer.iconWidth(icon, ICON_SIZE));
        int textW = Math.round(MSDFRenderer.width(text, TEXT_SIZE));
        int maxTextW = Math.round(MSDFRenderer.width(maxText, TEXT_SIZE));
        return new Segment(element, icon, text, iconW + ICON_GAP + Math.max(textW, maxTextW));
    }

    private static Segment logoSegment(int element, String text, String maxText) {
        int textW = Math.round(MSDFRenderer.width(text, TEXT_SIZE));
        int maxTextW = Math.round(MSDFRenderer.width(maxText, TEXT_SIZE));
        return new Segment(element, 0, text, LOGO_SIZE + ICON_GAP + Math.max(textW, maxTextW));
    }

    private static void drawSegment(GuiGraphics graphics, Segment segment, float x, int y, int textColor, int iconColor) {
        int textW = Math.round(MSDFRenderer.width(segment.text(), TEXT_SIZE));
        if (segment.element() == VisualSettings.WATERMARK_ELEMENT_LOGO) {
            float textureAlpha = ((iconColor >>> 24) & 0xFF) / 255.0f;
            XerionLogoTexture.draw(graphics, x, y + (H - LOGO_SIZE) * 0.5f,
                    LOGO_SIZE, LOGO_SIZE, textureAlpha);
        } else {
            float iconY = y + UiMath.centeredTextY(H, ICON_SIZE) + 1.0f;
            MSDFRenderer.drawIconCodepoint(graphics, segment.icon(), x, iconY, ICON_SIZE, iconColor);
        }
        float textY = y + UiMath.centeredTextY(H, TEXT_SIZE);
        MSDFRenderer.draw(graphics, segment.text(), x + segment.slotWidth() - textW, textY, TEXT_SIZE, textColor);
    }

    private static void drawWatermarkBlur(GuiGraphics graphics, float x, float y, float w, float h,
                                          float radius, float alpha) {
        if (!VisualSettings.watermarkBlur() || alpha <= 0.01f) {
            return;
        }
        graphics.flush();
        Window window = Minecraft.getInstance().getWindow();
        KawaseBlurPipeline.renderSurfaceBackdrop(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                x, y, w, h, radius, alpha, WATERMARK_BLUR_STRENGTH);
    }

    private static void enableScissor(int x, int y, int w, int h) {
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int scissorX = (int) Math.floor(x * scale);
        int scissorY = (int) Math.floor(y * scale);
        int scissorW = Math.max(1, (int) Math.ceil(w * scale));
        int scissorH = Math.max(1, (int) Math.ceil(h * scale));
        RenderSystem.enableScissor(scissorX, window.getHeight() - scissorY - scissorH, scissorW, scissorH);
    }

    private static String pingLabel(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return "0 MS";
        }
        var info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        return (info == null ? 0 : Math.max(0, info.getLatency())) + " MS";
    }

    private static String serverLabel(Minecraft minecraft) {
        ServerData server = minecraft.getCurrentServer();
        if (server != null && server.ip != null && !server.ip.isBlank()) {
            return server.ip;
        }
        if (minecraft.hasSingleplayerServer()) {
            return "Singleplayer";
        }
        return "Server";
    }

    private record Segment(int element, int icon, String text, int slotWidth) {
    }

    private record Layout(int w, List<Segment> segments) {
    }
}
