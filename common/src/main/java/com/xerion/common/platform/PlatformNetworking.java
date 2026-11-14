package com.xerion.common.platform;

public interface PlatformNetworking {
    boolean connectedToServer();

    void sendClientCommand(String command);
}