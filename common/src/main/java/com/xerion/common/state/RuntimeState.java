package com.xerion.common.state;

import java.util.Optional;

public final class RuntimeState {
    private volatile boolean inWorld;
    private volatile PlayerPosition playerPosition;
    private volatile PlayerVelocity playerVelocity;

    public boolean inWorld() {
        return inWorld;
    }

    public void setInWorld(boolean inWorld) {
        this.inWorld = inWorld;
    }

    public Optional<PlayerPosition> playerPosition() {
        return Optional.ofNullable(playerPosition);
    }

    public void setPlayerPosition(PlayerPosition playerPosition) {
        this.playerPosition = playerPosition;
    }

    public void clearPlayerPosition() {
        this.playerPosition = null;
    }

    public Optional<PlayerVelocity> playerVelocity() {
        return Optional.ofNullable(playerVelocity);
    }

    public void setPlayerVelocity(PlayerVelocity playerVelocity) {
        this.playerVelocity = playerVelocity;
    }

    public void clearPlayerVelocity() {
        this.playerVelocity = null;
    }
}
