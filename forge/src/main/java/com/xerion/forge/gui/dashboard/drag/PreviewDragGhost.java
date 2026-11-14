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

final class PreviewDragGhost {
    private float x;
    private float y;
    private float w;
    private float h;
    private boolean initialized;

    void reset() {
        initialized = false;
    }

    PreviewBounds update(PreviewBounds target, float speed) {
        if (!initialized) {
            x = target.x();
            y = target.y();
            w = target.w();
            h = target.h();
            initialized = true;
        } else {
            x = UiMath.lerp(x, target.x(), speed);
            y = UiMath.lerp(y, target.y(), speed);
            w = UiMath.lerp(w, target.w(), speed);
            h = UiMath.lerp(h, target.h(), speed);
        }
        return new PreviewBounds(Math.round(x), Math.round(y), Math.round(w), Math.round(h));
    }
}
