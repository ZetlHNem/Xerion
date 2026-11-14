package com.xerion.common.game;

public interface GameLivingEntity extends GameEntity {
    float health();

    float maxHealth();

    int armorValue();

    boolean alive();

    boolean teammate();
}
