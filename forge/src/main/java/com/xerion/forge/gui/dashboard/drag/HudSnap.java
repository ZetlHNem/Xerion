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

final class HudSnap {
    private static final int SNAP_DISTANCE = 8;
    private static final int INNER_GUIDE = 10;
    private static Integer activeGuideX;
    private static Integer activeGuideY;

    private HudSnap() {
    }

    static Position snap(int x, int y, int w, int h, int screenW, int screenH) {
        int maxX = Math.max(0, screenW - w);
        int maxY = Math.max(0, screenH - h);
        AxisSnap snappedX = snapAxis(x, w, screenW, maxX);
        AxisSnap snappedY = snapAxis(y, h, screenH, maxY);
        activeGuideX = snappedX.guide();
        activeGuideY = snappedY.guide();
        return new Position(snappedX.value(), snappedY.value());
    }

    static void renderGuides(GuiGraphics graphics, int screenW, int screenH, int widgetW, int widgetH) {
        int color = UiColor.withAlpha(UiPalette.accent(), 100);
        int halfW = Math.max(0, Math.round(widgetW * 0.5f));
        int halfH = Math.max(0, Math.round(widgetH * 0.5f));
        drawVerticalGuide(graphics, 0, screenW, screenH, color);
        drawVerticalGuide(graphics, INNER_GUIDE + halfW, screenW, screenH, color);
        drawVerticalGuide(graphics, Math.round(screenW * 0.5f), screenW, screenH, color);
        drawVerticalGuide(graphics, screenW - INNER_GUIDE - halfW, screenW, screenH, color);
        drawVerticalGuide(graphics, screenW - 1, screenW, screenH, color);
        drawHorizontalGuide(graphics, 0, screenW, screenH, color);
        drawHorizontalGuide(graphics, INNER_GUIDE + halfH, screenW, screenH, color);
        drawHorizontalGuide(graphics, Math.round(screenH * 0.5f), screenW, screenH, color);
        drawHorizontalGuide(graphics, screenH - INNER_GUIDE - halfH, screenW, screenH, color);
        drawHorizontalGuide(graphics, screenH - 1, screenW, screenH, color);
    }

    static void clearGuides() {
        activeGuideX = null;
        activeGuideY = null;
    }

    record Position(int x, int y) {
    }

    private static void drawVerticalGuide(GuiGraphics graphics, int x, int screenW, int screenH, int color) {
        int clampedX = UiMath.clamp(x, 0, Math.max(0, screenW - 1));
        graphics.fill(clampedX, 0, clampedX + 1, screenH, color);
    }

    private static void drawHorizontalGuide(GuiGraphics graphics, int y, int screenW, int screenH, int color) {
        int clampedY = UiMath.clamp(y, 0, Math.max(0, screenH - 1));
        graphics.fill(0, clampedY, screenW, clampedY + 1, color);
    }

    private static AxisSnap snapAxis(int value, int size, int screenSize, int max) {
        int clamped = UiMath.clamp(value, 0, max);
        AxisSnap best = new AxisSnap(clamped, null, SNAP_DISTANCE + 1);
        int halfSize = Math.max(0, Math.round(size * 0.5f));
        best = choose(best, clamped, 0, 0);
        best = choose(best, clamped, max, Math.max(0, screenSize - 1));
        best = choose(best, clamped, INNER_GUIDE, INNER_GUIDE + halfSize);
        best = choose(best, clamped, Math.max(0, screenSize - INNER_GUIDE - size),
                Math.max(0, screenSize - INNER_GUIDE - halfSize));
        best = choose(best, clamped, Math.round(screenSize * 0.5f - size * 0.5f),
                Math.round(screenSize * 0.5f));
        return new AxisSnap(UiMath.clamp(best.value(), 0, max), best.guide(), best.distance());
    }

    private static AxisSnap choose(AxisSnap current, int value, int target, int guide) {
        int distance = Math.abs(value - target);
        if (distance <= SNAP_DISTANCE && distance < current.distance()) {
            return new AxisSnap(target, guide, distance);
        }
        return current;
    }

    private record AxisSnap(int value, Integer guide, int distance) {
    }
}
