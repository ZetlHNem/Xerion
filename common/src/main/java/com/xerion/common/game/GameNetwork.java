package com.xerion.common.game;

public interface GameNetwork {
    boolean connected();

    void sendClientCommand(String command);
}
