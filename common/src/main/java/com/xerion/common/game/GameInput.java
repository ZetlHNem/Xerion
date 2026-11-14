package com.xerion.common.game;

public interface GameInput {
    boolean keyDown(int keyCode);

    boolean mouseDown(int button);

    float forwardImpulse();

    float leftImpulse();

    boolean jumping();

    boolean shiftKeyDown();
}
