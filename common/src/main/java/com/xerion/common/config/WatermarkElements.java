package com.xerion.common.config;

public final class WatermarkElements {
    public static final int LOGO = 1;
    public static final int PLAYER = 1 << 1;
    public static final int CONFIG = 1 << 2;
    public static final int PING = 1 << 3;
    public static final int FPS = 1 << 4;
    public static final int SERVER = 1 << 5;
    public static final int TIME = 1 << 6;
    public static final int ALL = LOGO | PLAYER | CONFIG | PING | FPS | SERVER | TIME;

    private WatermarkElements() {
    }
}