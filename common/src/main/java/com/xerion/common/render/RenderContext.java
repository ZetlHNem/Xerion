package com.xerion.common.render;

public interface RenderContext {
    int screenWidth();

    int screenHeight();

    float tickDelta();

    void drawText(String text, int x, int y, Color color, boolean shadow);
}
