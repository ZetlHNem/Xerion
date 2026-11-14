package com.xerion.forge.gui.font;

import net.minecraft.client.renderer.texture.DynamicTexture;

import java.util.Map;

final class TtfAtlas {
    private final DynamicTexture texture;
    private final int width;
    private final int height;
    private final float distanceRange;
    private final Map<Integer, TtfGlyph> glyphs;

    TtfAtlas(DynamicTexture texture, int width, int height, float distanceRange,
             Map<Integer, TtfGlyph> glyphs) {
        this.texture = texture;
        this.width = width;
        this.height = height;
        this.distanceRange = distanceRange;
        this.glyphs = glyphs;
    }

    DynamicTexture texture() {
        return texture;
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }

    float distanceRange() {
        return distanceRange;
    }

    TtfGlyph glyph(int codePoint) {
        return glyphs.get(codePoint);
    }
}
