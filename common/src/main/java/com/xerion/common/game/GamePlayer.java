package com.xerion.common.game;

public interface GamePlayer extends GameLivingEntity {
    GameVec3 eyePosition();

    GameRotation rotation();

    GameInventory inventory();

    boolean onGround();

    boolean sprinting();

    boolean sneaking();

    float attackCooldown(float partialTicks);
}
