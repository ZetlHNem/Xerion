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

final class AnimatedPreviewBounds {
    private float x;
    private float y;
    private float w;
    private float h;

    AnimatedPreviewBounds(PreviewBounds bounds) {
        this.x = bounds.x();
        this.y = bounds.y();
        this.w = bounds.w();
        this.h = bounds.h();
    }

    void update(PreviewBounds target, float speed) {
        x = animateValue(x, target.x(), speed);
        y = animateValue(y, target.y(), speed);
        w = animateValue(w, target.w(), speed);
        h = animateValue(h, target.h(), speed);
    }

    void translate(float deltaX, float deltaY) {
        x += deltaX;
        y += deltaY;
    }

    PreviewBounds bounds() {
        return new PreviewBounds(Math.round(x), Math.round(y), Math.round(w), Math.round(h));
    }

    private static float animateValue(float current, float target, float speed) {
        float next = UiMath.lerp(current, target, speed);
        if (Math.abs(next - target) < 0.35f) {
            return target;
        }
        return next;
    }
}
