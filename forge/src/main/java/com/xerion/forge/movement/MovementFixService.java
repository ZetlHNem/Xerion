package com.xerion.forge.movement;

import com.xerion.common.movement.ScaffoldRotation;
import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.MovementInputUpdateEvent;

/**
 * Port of OpenOpal's Movement Fix: remaps WASD input so movement follows the
 * silent rotation instead of the raw client yaw, plus a sprint-only variant
 * that stops sprinting when the movement and rotation yaw differ too much.
 */
public final class MovementFixService {
    private MovementFixService() {
    }

    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!VisualSettings.movementFixEnabled()
                || event.getInput() == null
                || !(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        if ("SprintOnly".equals(VisualSettings.movementFixMode())) {
            if (!VisualSettings.movementFixPacketOnly() && shouldResetSprint(player)) {
                player.setSprinting(false);
            }
            return;
        }
        float forward = event.getInput().forwardImpulse;
        float strafe = event.getInput().leftImpulse;
        if (forward == 0.0F && strafe == 0.0F) {
            return;
        }
        float referenceYaw = ScaffoldRotation.active() ? ScaffoldRotation.yaw() : player.getYRot();
        float desiredAngle = movementAngle(referenceYaw, forward, strafe);
        float bestForward = 0.0F;
        float bestStrafe = 0.0F;
        float bestDifference = Float.MAX_VALUE;
        for (float predictedForward = -1.0F; predictedForward <= 1.0F; predictedForward += 1.0F) {
            for (float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; predictedStrafe += 1.0F) {
                if (predictedForward == 0.0F && predictedStrafe == 0.0F) {
                    continue;
                }
                float predictedAngle = movementAngle(player.getYRot(), predictedForward, predictedStrafe);
                float difference = Math.abs(Mth.wrapDegrees(desiredAngle - predictedAngle));
                if (difference < bestDifference) {
                    bestDifference = difference;
                    bestForward = predictedForward;
                    bestStrafe = predictedStrafe;
                }
            }
        }
        event.getInput().forwardImpulse = bestForward;
        event.getInput().leftImpulse = bestStrafe;
    }

    public static boolean isFixMovement() {
        return VisualSettings.movementFixEnabled() && "Normal".equals(VisualSettings.movementFixMode());
    }

    public static void clearRuntimeState() {
    }

    private static boolean shouldResetSprint(LocalPlayer player) {
        float referenceYaw = ScaffoldRotation.active() ? ScaffoldRotation.yaw() : player.getYRot();
        float movementYaw = referenceYaw;
        float difference = Math.abs(Mth.wrapDegrees(movementYaw - player.getYRot()));
        return difference > 45.0F + 0.005F;
    }

    private static float movementAngle(float yaw, float forward, float strafe) {
        float moveYaw = yaw;
        if (forward < 0.0F) {
            moveYaw += 180.0F;
        }
        float factor = forward < 0.0F ? -0.5F : forward > 0.0F ? 0.5F : 1.0F;
        if (strafe > 0.0F) {
            moveYaw -= 90.0F * factor;
        }
        if (strafe < 0.0F) {
            moveYaw += 90.0F * factor;
        }
        return Mth.wrapDegrees(moveYaw);
    }
}
