package com.xerion.common.platform;

public interface PlatformInput {
    boolean isKeyDown(int keyCode);

    String displayName(int keyCode, int scanCode);

    void registerKeyBinding(String id, int defaultKeyCode, Runnable action);
}