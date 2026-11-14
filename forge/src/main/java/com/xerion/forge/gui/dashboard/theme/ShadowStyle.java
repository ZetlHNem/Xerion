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

final class ShadowStyle {
    private ShadowStyle() {
    }

    static Style shell() {
        return UiPalette.dark()
                ? new Style(0x2A000000, 24.0f, 0.0f, 0.0f)
                : new Style(0x2A0E1730, 24.0f, 0.0f, 0.0f);
    }

    static Style card() {
        return UiPalette.dark()
                ? new Style(0x20000000, 14.0f, 0.0f, 0.0f)
                : new Style(0x201A2450, 14.0f, 0.0f, 0.0f);
    }

    static Style popup() {
        return UiPalette.dark()
                ? new Style(0x24000000, 16.0f, 0.0f, 0.0f)
                : new Style(0x24162148, 16.0f, 0.0f, 0.0f);
    }

    static Style glow() {
        return UiPalette.dark()
                ? new Style(0x1E6E72FF, 16.0f, 0.0f, 0.0f)
                : new Style(0x1E6E72FF, 16.0f, 0.0f, 0.0f);
    }

    static final class Style {
        private final int color;
        private final float spread;
        private final float offsetX;
        private final float offsetY;

        private Style(int color, float spread, float offsetX, float offsetY) {
            this.color = color;
            this.spread = spread;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        int color() {
            return color;
        }

        float spread() {
            return spread;
        }

        float offsetX() {
            return offsetX;
        }

        float offsetY() {
            return offsetY;
        }
    }
}
