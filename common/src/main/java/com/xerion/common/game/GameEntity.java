package com.xerion.common.game;

public interface GameEntity {
    int id();

    String typeId();

    String displayName();

    GameVec3 position();

    GameVec3 velocity();

    GameBox boundingBox();

    boolean removed();

    boolean invisible();
}
