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

import net.minecraft.client.gui.GuiGraphics;

final class ScrollbarControl {
    static final int RIGHT_INSET = 14;
    static final int TRACK_W = 4;
    static final int TRACK_SLOT_W = 8;
    static final int VERTICAL_INSET = 3;
    static final int THUMB_MIN_H = 30;

    private ScrollbarControl() {
    }

    static float updateReserve(float current, boolean visible) {
        float target = visible ? RIGHT_INSET : 0.0f;
        float next = AnimationClock.lerp(current, target, UiAnimationTimings.CONTENT_SCROLL);
        if (Math.abs(next - target) < 0.25f) {
            next = target;
        }
        return next;
    }

    static float updateAlpha(float current, boolean visible) {
        float target = visible ? 1.0f : 0.0f;
        float speed = visible ? UiAnimationTimings.POPUP_OPEN : UiAnimationTimings.POPUP_CLOSE;
        float next = AnimationClock.lerp(current, target, speed);
        if (Math.abs(next - target) < 0.01f) {
            next = target;
        }
        return UiMath.clamp01(next);
    }

    static Layout layout(int windowX, int windowY, int windowW, int contentY, int contentH,
                         int contentHFull, float reserve) {
        int reserved = Math.round(UiMath.clamp(reserve, 0.0f, RIGHT_INSET));
        int contentW = Math.max(0, contentHFull - reserved);
        int trackX = windowX + windowW - RIGHT_INSET;
        int trackY = contentY + VERTICAL_INSET;
        int trackH = Math.max(0, contentH - VERTICAL_INSET * 2);
        return new Layout(contentW, trackX, trackY, trackH);
    }

    static void draw(GuiGraphics graphics, int x, int y, int h, float progress, int viewportH, int contentH,
                     int trackColor, int thumbColor) {
        ShapeRenderer.roundRect(graphics, x + 2, y, TRACK_W, h, 2, trackColor);
        int thumbH = UiMath.clamp(Math.round(h * (viewportH / (float) Math.max(viewportH, contentH))), THUMB_MIN_H, h);
        int thumbY = y + Math.round((h - thumbH) * UiMath.clamp01(progress));
        ShapeRenderer.roundRect(graphics, x + 2, thumbY, TRACK_W, thumbH, 2, thumbColor);
    }

    static void draw(GuiGraphics graphics, int x, int y, int h, float progress, int viewportH, int contentH) {
        draw(graphics, x, y, h, progress, viewportH, contentH, 1.0f);
    }

    static void draw(GuiGraphics graphics, int x, int y, int h, float progress, int viewportH, int contentH, float alpha) {
        if (alpha <= 0.01f || h <= 0) {
            return;
        }
        int trackColor = UiColor.alpha(UiPalette.track(), 0.64f * alpha);
        int thumbBase = UiColor.lerp(UiPalette.muted(), UiPalette.accent(), 0.18f);
        int thumbColor = UiColor.alpha(thumbBase, 0.86f * alpha);
        draw(graphics, x, y, h, progress, viewportH, contentH, trackColor, thumbColor);
    }

    static Thumb thumb(int y, int h, int viewportH, int contentH, int scrollY, int maxScrollY) {
        int thumbH = UiMath.clamp(Math.round(h * (viewportH / (float) Math.max(viewportH, contentH))), THUMB_MIN_H, h);
        float progress = maxScrollY <= 0 ? 0.0f : scrollY / (float) maxScrollY;
        int thumbY = y + Math.round((h - thumbH) * UiMath.clamp01(progress));
        return new Thumb(thumbY, thumbH);
    }

    static int scrollFromThumbDrag(double mouseY, double grabOffsetY, int trackY, int trackH,
                                   int viewportH, int contentH, int maxScrollY) {
        if (maxScrollY <= 0 || trackH <= 0) {
            return 0;
        }
        int thumbH = UiMath.clamp(Math.round(trackH * (viewportH / (float) Math.max(viewportH, contentH))), THUMB_MIN_H, trackH);
        int travel = Math.max(1, trackH - thumbH);
        float thumbTop = (float) (mouseY - grabOffsetY - trackY);
        float progress = UiMath.clamp(thumbTop / travel, 0.0f, 1.0f);
        return UiMath.clamp(Math.round(progress * maxScrollY), 0, maxScrollY);
    }

    static boolean hitThumb(double mouseX, double mouseY, int trackX, int trackY, int trackH,
                            int viewportH, int contentH, int scrollY, int maxScrollY) {
        if (maxScrollY <= 0 || trackH <= 0) {
            return false;
        }
        Thumb thumb = thumb(trackY, trackH, viewportH, contentH, scrollY, maxScrollY);
        int hitX = trackX + 2;
        int hitW = TRACK_SLOT_W;
        return mouseX >= hitX && mouseX <= hitX + hitW
                && mouseY >= thumb.y() && mouseY <= thumb.y() + thumb.h();
    }

    static final class DragState {
        boolean dragging;
        double grabOffsetY;

        void stop() {
            dragging = false;
            grabOffsetY = 0.0;
        }
    }

    static boolean beginDrag(DragState state, double mouseX, double mouseY, Layout layout,
                             int viewportH, int contentH, int scrollY, int maxScrollY) {
        if (state.dragging || !hitThumb(mouseX, mouseY, layout.trackX(), layout.trackY(), layout.trackH(),
                viewportH, contentH, scrollY, maxScrollY)) {
            return false;
        }
        Thumb thumb = thumb(layout.trackY(), layout.trackH(), viewportH, contentH, scrollY, maxScrollY);
        state.dragging = true;
        state.grabOffsetY = mouseY - thumb.y();
        return true;
    }

    static boolean updateDrag(DragState state, double mouseY, Layout layout,
                              int viewportH, int contentH, int maxScrollY, int[] outScrollY) {
        if (!state.dragging) {
            return false;
        }
        outScrollY[0] = scrollFromThumbDrag(mouseY, state.grabOffsetY, layout.trackY(), layout.trackH(),
                viewportH, contentH, maxScrollY);
        return true;
    }

    record Layout(int contentW, int trackX, int trackY, int trackH) {
    }

    record Thumb(int y, int h) {
    }
}
