package com.xerion.common.game;

public interface GameRenderer {
    int windowWidth();

    int windowHeight();

    double guiScale();

    GameVec3 cameraPosition();

    GameRotation cameraRotation();
}
