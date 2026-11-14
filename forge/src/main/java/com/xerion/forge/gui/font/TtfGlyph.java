package com.xerion.forge.gui.font;

record TtfGlyph(int codePoint, float advance, Bounds planeBounds, Bounds atlasBounds) {
    boolean drawable() {
        return planeBounds != null && atlasBounds != null;
    }

    record Bounds(float left, float top, float right, float bottom) {
    }
}
