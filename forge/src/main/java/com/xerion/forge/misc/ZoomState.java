package com.xerion.forge.misc;

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
import com.xerion.common.visual.ZoomRules;

import net.minecraft.client.Minecraft;

public final class ZoomState {
    private static final Minecraft MC = Minecraft.getInstance();
    private static float renderedFactor = 1.0f;
    private static boolean cinematicApplied;
    private static boolean previousSmoothCamera;

    private ZoomState() {
    }

    public static void tick() {
        if (!VisualSettings.zoomEnabled() || !VisualSettings.zoomCinematic() || MC.options == null) {
            restoreCinematic();
            return;
        }
        if (!cinematicApplied) {
            previousSmoothCamera = MC.options.smoothCamera;
            cinematicApplied = true;
        }
        MC.options.smoothCamera = true;
    }

    public static void reset() {
        renderedFactor = 1.0f;
        restoreCinematic();
    }

    public static void stop() {
        restoreCinematic();
    }

    public static boolean active() {
        return VisualSettings.zoomEnabled();
    }

    public static boolean rendering() {
        return ZoomRules.rendering(active(), renderedFactor);
    }

    public static boolean shouldRenderHands() {
        return !active() || VisualSettings.zoomShowHands();
    }

    public static double applyFov(double originalFov) {
        float target = ZoomRules.targetFactor(active(), VisualSettings.zoomFactor());
        renderedFactor = ZoomRules.nextRenderedFactor(renderedFactor, target, VisualSettings.zoomSmooth());
        return ZoomRules.applyFov(originalFov, renderedFactor);
    }

    private static void restoreCinematic() {
        if (cinematicApplied && MC.options != null) {
            MC.options.smoothCamera = previousSmoothCamera;
        }
        cinematicApplied = false;
    }
}
