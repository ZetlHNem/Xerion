package com.xerion.forge.movement;

import com.xerion.common.movement.MovementSpeedRules;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.forge.config.VisualSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class MovementSpeedService {
    private MovementSpeedService() {
    }

    public static void applySpeed(Minecraft minecraft) {
        if (BypassProtocolService.correctionRecoveryActive()) {
            return;
        }
        if (!VisualSettings.speedEnabled()) {
            return;
        }
        if (VisualSettings.flightEnabled() || VisualSettings.phaseEnabled()
                || FlightService.elytraFlyActive(minecraft.player)) {
            return;
        }
        if (SpeedModeService.handles(VisualSettings.speedMode())) {
            SpeedModeService.apply(minecraft);
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.input == null || player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying || player.isShiftKeyDown() || player.horizontalCollision) {
            return;
        }
        if (minecraft.screen != null) {
            return;
        }
        if (player.isUsingItem() && !VisualSettings.noSlowEnabled()) {
            return;
        }
        if (VisualSettings.speedOnlySprint()
                && !player.isSprinting()
                && !VisualSettings.keepSprintEnabled()
                && !minecraft.options.keySprint.isDown()) {
            return;
        }

        com.xerion.common.movement.MovementMath.MoveInput moveInput = MovementMath.movementInput(player);
        if (!moveInput.moving()) {
            return;
        }

        String mode = VisualSettings.speedMode();
        if (VisualSettings.speedAutoJump() && ("BHop".equals(mode) || "Strafe".equals(mode))
                && player.onGround() && !minecraft.options.keyJump.isDown()) {
            player.jumpFromGround();
        }

        Vec3 velocity = player.getDeltaMovement();
        double currentSpeed = horizontalSpeed(velocity);
        double targetSpeed = MovementSpeedRules.speedTarget(player.onGround(), mode, currentSpeed,
                VisualSettings.speedGroundMultiplier(), VisualSettings.speedAirMultiplier());
        if (targetSpeed <= 1.0E-5D) {
            return;
        }

        double[] motion = MovementMath.directionalMotion(player.getYRot(), moveInput.forward(), moveInput.strafe(), targetSpeed);
        player.setDeltaMovement(motion[0], velocity.y, motion[1]);
        player.hurtMarked = true;
    }

    public static void applyNoSlowSpeedCorrection(Minecraft minecraft) {
        if (BypassProtocolService.correctionRecoveryActive()) {
            return;
        }
        if (!VisualSettings.noSlowEnabled()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger() || player.isSpectator() || player.getAbilities().flying
                || !player.isUsingItem() || minecraft.screen != null) {
            return;
        }

        com.xerion.common.movement.MovementMath.MoveInput moveInput = keyMoveInput(minecraft);
        if (!moveInput.moving()) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double targetSpeed = MovementSpeedRules.noSlowTargetSpeed(
                player.isSprinting() || VisualSettings.keepSprintEnabled());
        if (horizontalSpeed(velocity) >= targetSpeed * 0.96D) {
            return;
        }

        double[] motion = MovementMath.directionalMotion(player.getYRot(), moveInput.forward(), moveInput.strafe(), targetSpeed);
        player.setDeltaMovement(motion[0], velocity.y, motion[1]);
        player.hurtMarked = true;
    }

    public static double keepSprintGroundTargetSpeed() {
        return MovementSpeedRules.keepSprintGroundTargetSpeed(VisualSettings.keepSprintScale());
    }

    public static double keepSprintAirTargetSpeed(int keepSprintAirTicks) {
        return MovementSpeedRules.keepSprintAirTargetSpeed(keepSprintAirTicks, VisualSettings.keepSprintScale());
    }

    public static double longJumpHorizontalSpeed(double power) {
        return MovementSpeedRules.longJumpHorizontalSpeed(power);
    }

    private static double horizontalSpeed(Vec3 velocity) {
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    private static com.xerion.common.movement.MovementMath.MoveInput keyMoveInput(Minecraft minecraft) {
        float forward = 0.0F;
        float strafe = 0.0F;
        if (minecraft.options.keyUp.isDown()) {
            forward += 1.0F;
        }
        if (minecraft.options.keyDown.isDown()) {
            forward -= 1.0F;
        }
        if (minecraft.options.keyLeft.isDown()) {
            strafe += 1.0F;
        }
        if (minecraft.options.keyRight.isDown()) {
            strafe -= 1.0F;
        }
        return MovementMath.normalizedMoveInput(forward, strafe);
    }
}
