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

public final class AnimationClock {
    private static final float MIN_DELTA_SECONDS = 1.0f / 240.0f;
    private static final float MAX_DELTA_SECONDS = 1.0f / 20.0f;
    private static long lastFrameNanos;
    private static int frameToken;
    private static float deltaSeconds = 1.0f / 60.0f;

    private AnimationClock() {
    }

    public static void beginFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos == 0L) {
            deltaSeconds = 1.0f / 60.0f;
        } else {
            float rawDelta = (now - lastFrameNanos) / 1_000_000_000.0f;
            deltaSeconds = UiMath.clamp(rawDelta, MIN_DELTA_SECONDS, MAX_DELTA_SECONDS);
        }
        lastFrameNanos = now;
        frameToken++;
    }

    static int frameToken() {
        return frameToken;
    }

    static float deltaSeconds() {
        return deltaSeconds;
    }

    static float factor(float perFrameFactorAt60Fps) {
        float clamped = UiMath.clamp01(perFrameFactorAt60Fps);
        if (clamped >= 1.0f) {
            return 1.0f;
        }
        float framesAt60 = deltaSeconds * 60.0f;
        return 1.0f - (float) Math.pow(1.0f - clamped, framesAt60);
    }

    static float lerp(float from, float to, float perFrameFactorAt60Fps) {
        return from + (to - from) * factor(perFrameFactorAt60Fps);
    }
}
