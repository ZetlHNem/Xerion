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

final class UiMath {
    private UiMath() {
    }

    static boolean contains(double px, double py, int x, int y, int w, int h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    static float clamp01(float value) {
        return clamp(value, 0.0f, 1.0f);
    }

    static float smoothstep(float value) {
        float t = clamp01(value);
        return t * t * (3.0f - 2.0f * t);
    }

    static float lerp(float from, float to, float speed) {
        return AnimationClock.lerp(from, to, speed);
    }

    static float centeredTextY(int height, float size) {
        return (height - size) * 0.5f - 1.0f;
    }
}
