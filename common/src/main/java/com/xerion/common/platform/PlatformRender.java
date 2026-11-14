package com.xerion.common.platform;

public interface PlatformRender {
    int windowWidth();

    int windowHeight();

    double guiScale();

    void requestReloadResources();
}