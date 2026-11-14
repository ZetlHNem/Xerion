package com.xerion.common.render;

public record Color(int argb) {
    public static final Color WHITE = new Color(0xFFFFFFFF);
    public static final Color BLACK = new Color(0xFF000000);
    public static final Color ACCENT = new Color(0xFF3BC9DB);
}
