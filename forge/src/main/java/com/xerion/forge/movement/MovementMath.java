package com.xerion.forge.movement;

import net.minecraft.client.player.LocalPlayer;

public final class MovementMath {
    private MovementMath() {
    }

    public static com.xerion.common.movement.MovementMath.MoveInput movementInput(LocalPlayer player) {
        float forward = player.input == null ? 0.0F : player.input.forwardImpulse;
        float strafe = player.input == null ? 0.0F : player.input.leftImpulse;
        return normalizedMoveInput(forward, strafe);
    }

    public static com.xerion.common.movement.MovementMath.MoveInput normalizedMoveInput(float forward, float strafe) {
        return com.xerion.common.movement.MovementMath.normalizedMoveInput(forward, strafe);
    }

    public static double[] directionalMotion(float yaw, float forward, float strafe, double speed) {
        return com.xerion.common.movement.MovementMath.directionalMotion(yaw, forward, strafe, speed);
    }
}