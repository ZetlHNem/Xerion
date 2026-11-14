package com.xerion.forge.movement;

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
import com.xerion.common.movement.MovementSurfaceRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public final class EdgeJumpService {
    private static final double GROUND_PROBE = 0.10D;
    private static final double MIN_LOOKAHEAD = 0.28D;
    private static final double EXTRA_LOOKAHEAD = 0.20D;
    private static boolean jumpedOnThisGround;

    private EdgeJumpService() {
    }

    public static void tick(Minecraft minecraft) {
        if (!VisualSettings.edgeJumpEnabled()) {
            jumpedOnThisGround = false;
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || player.isPassenger() || player.isSpectator()
                || player.getAbilities().flying || minecraft.options.keyJump.isDown()) {
            jumpedOnThisGround = false;
            return;
        }
        if (!player.onGround()) {
            jumpedOnThisGround = false;
            return;
        }
        if (jumpedOnThisGround) {
            return;
        }

        float forward = 0.0F;
        float strafe = 0.0F;
        if (MenuMovementService.keyDown(minecraft, minecraft.options.keyUp)) {
            forward += 1.0F;
        }
        if (MenuMovementService.keyDown(minecraft, minecraft.options.keyDown)) {
            forward -= 1.0F;
        }
        if (MenuMovementService.keyDown(minecraft, minecraft.options.keyLeft)) {
            strafe += 1.0F;
        }
        if (MenuMovementService.keyDown(minecraft, minecraft.options.keyRight)) {
            strafe -= 1.0F;
        }
        if (Math.abs(forward) < 1.0E-5F && Math.abs(strafe) < 1.0E-5F) {
            return;
        }

        if (!hasGroundAt(player, player.getX(), player.getZ())) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        double lookahead = MovementSurfaceRules.edgeJumpLookahead(speed, MIN_LOOKAHEAD, EXTRA_LOOKAHEAD);
        com.xerion.common.movement.MovementMath.MoveInput input = MovementMath.normalizedMoveInput(forward, strafe);
        double[] offset = MovementMath.directionalMotion(player.getYRot(), input.forward(), input.strafe(), lookahead);
        double targetX = player.getX() + offset[0];
        double targetZ = player.getZ() + offset[1];
        if (hasGroundAt(player, targetX, targetZ)) {
            return;
        }

        jump(player);
        jumpedOnThisGround = true;
    }

    private static void jump(LocalPlayer player) {
        Vec3 velocity = player.getDeltaMovement();
        double jumpY = 0.42D;
        if (player.hasEffect(MobEffects.JUMP)) {
            jumpY += (player.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1D;
        }

        double motionX = velocity.x;
        double motionZ = velocity.z;
        if (player.isSprinting()) {
            float yaw = player.getYRot() * ((float) Math.PI / 180.0F);
            motionX -= Math.sin(yaw) * 0.2F;
            motionZ += Math.cos(yaw) * 0.2F;
        }
        player.setDeltaMovement(motionX, jumpY, motionZ);
        player.hasImpulse = true;
    }

    private static boolean hasGroundAt(LocalPlayer player, double x, double z) {
        BlockPos pos = BlockPos.containing(x, player.getBoundingBox().minY - GROUND_PROBE, z);
        return !player.level().getBlockState(pos).getCollisionShape(player.level(), pos).isEmpty();
    }
}
