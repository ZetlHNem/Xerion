package com.xerion.forge.misc;

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
import com.xerion.common.movement.FreecamRules;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FreecamService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static Vec3 cameraPos;
    private static Vec3 playerFreezePos;
    private static CameraType previousCameraType;
    private static float playerFreezeYaw;
    private static float playerFreezePitch;
    private static float cameraYaw;
    private static float cameraPitch;
    private static long lastFrameMoveNanos;

    public void tick() {
        LocalPlayer player = MC.player;
        if (!VisualSettings.freecamEnabled() || player == null || MC.level == null) {
            disable();
            return;
        }

        if (cameraPos == null) {
            cameraPos = new Vec3(player.getX(), player.getEyeY(), player.getZ());
            playerFreezePos = player.position();
            playerFreezeYaw = player.getYRot();
            playerFreezePitch = player.getXRot();
            cameraYaw = playerFreezeYaw;
            cameraPitch = playerFreezePitch;
            previousCameraType = MC.options.getCameraType();
            MC.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            lastFrameMoveNanos = System.nanoTime();
        }

        if (VisualSettings.freecamFreezePlayer() && playerFreezePos != null) {
            freezePlayer(player);
        }
    }

    public void disable() {
        resetState();
    }

    public static void resetState() {
        if (previousCameraType != null) {
            MC.options.setCameraType(previousCameraType);
        }
        cameraPos = null;
        playerFreezePos = null;
        previousCameraType = null;
        lastFrameMoveNanos = 0L;
    }

    public static boolean active() {
        return VisualSettings.freecamEnabled() && MC.player != null && cameraPos != null;
    }

    public static Vec3 cameraPosition() {
        return active() ? cameraPos : null;
    }

    public static void resetFreezeAnchor() {
        LocalPlayer player = MC.player;
        if (player == null) {
            playerFreezePos = null;
            return;
        }
        playerFreezePos = player.position();
        playerFreezeYaw = player.getYRot();
        playerFreezePitch = player.getXRot();
    }

    public static Vec3 renderCameraPosition() {
        if (!active()) {
            return null;
        }
        LocalPlayer player = MC.player;
        if (player != null) {
            moveCamera(player);
            if (VisualSettings.freecamFreezePlayer() && playerFreezePos != null) {
                freezePlayer(player);
            } else if (VisualSettings.freecamFreezePlayer()) {
                resetFreezeAnchor();
            }
        }
        return cameraPos;
    }

    public static float cameraYaw() {
        return cameraYaw;
    }

    public static float cameraPitch() {
        return cameraPitch;
    }

    public static void suppressPlayerInput(Input input) {
        if (!active() || !VisualSettings.freecamFreezePlayer()) {
            return;
        }
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
        input.forwardImpulse = 0.0F;
        input.leftImpulse = 0.0F;
    }

    public static boolean handleTurn(Entity entity, double yawDelta, double pitchDelta) {
        if (!active() || entity != MC.player) {
            return false;
        }
        cameraYaw = FreecamRules.nextYaw(cameraYaw, yawDelta);
        cameraPitch = FreecamRules.nextPitch(cameraPitch, pitchDelta);
        return true;
    }

    private static void moveCamera(LocalPlayer player) {
        long now = System.nanoTime();
        double frameSeconds = FreecamRules.frameSeconds(now, lastFrameMoveNanos);
        lastFrameMoveNanos = now;

        double speed = FreecamRules.movementSpeed(VisualSettings.freecamSpeed(), frameSeconds, MC.options.keySprint.isDown());

        double forward = 0.0D;
        double strafe = 0.0D;
        double vertical = 0.0D;
        if (MC.options.keyUp.isDown()) {
            forward += 1.0D;
        }
        if (MC.options.keyDown.isDown()) {
            forward -= 1.0D;
        }
        if (MC.options.keyLeft.isDown()) {
            strafe += 1.0D;
        }
        if (MC.options.keyRight.isDown()) {
            strafe -= 1.0D;
        }
        if (MC.options.keyJump.isDown()) {
            vertical += 1.0D;
        }
        if (MC.options.keyShift.isDown()) {
            vertical -= 1.0D;
        }

        if (forward == 0.0D && strafe == 0.0D && vertical == 0.0D) {
            return;
        }

        FreecamRules.Motion motion = FreecamRules.movement(cameraYaw, forward, strafe, vertical, speed);
        Vec3 movement = new Vec3(motion.x(), motion.y(), motion.z());
        if (!VisualSettings.freecamNoClip()) {
            movement = clipCameraMovement(player, movement);
        }
        cameraPos = cameraPos.add(movement);
    }

    private static Vec3 clipCameraMovement(LocalPlayer player, Vec3 movement) {
        if (MC.level == null || movement.lengthSqr() <= 1.0E-9D) {
            return movement;
        }
        AABB box = player.getBoundingBox().move(cameraPos.subtract(player.position()));
        double x = movement.x;
        double y = movement.y;
        double z = movement.z;

        if (x != 0.0D && !MC.level.noCollision(box.move(x, 0.0D, 0.0D))) {
            x = 0.0D;
        }
        box = box.move(x, 0.0D, 0.0D);
        if (y != 0.0D && !MC.level.noCollision(box.move(0.0D, y, 0.0D))) {
            y = 0.0D;
        }
        box = box.move(0.0D, y, 0.0D);
        if (z != 0.0D && !MC.level.noCollision(box.move(0.0D, 0.0D, z))) {
            z = 0.0D;
        }
        return new Vec3(x, y, z);
    }

    private static void freezePlayer(LocalPlayer player) {
        Vec3 velocity = player.getDeltaMovement();
        if (player.onGround()) {
            playerFreezePos = new Vec3(playerFreezePos.x, player.getY(), playerFreezePos.z);
            player.setDeltaMovement(0.0D, 0.0D, 0.0D);
            player.setPos(playerFreezePos.x, playerFreezePos.y, playerFreezePos.z);
            player.xOld = playerFreezePos.x;
            player.yOld = playerFreezePos.y;
            player.zOld = playerFreezePos.z;
            player.xo = playerFreezePos.x;
            player.yo = playerFreezePos.y;
            player.zo = playerFreezePos.z;
        } else {
            player.setDeltaMovement(0.0D, velocity.y, 0.0D);
            player.setPos(playerFreezePos.x, player.getY(), playerFreezePos.z);
            player.xOld = playerFreezePos.x;
            player.zOld = playerFreezePos.z;
            player.xo = playerFreezePos.x;
            player.zo = playerFreezePos.z;
        }
        player.setYRot(playerFreezeYaw);
        player.setXRot(playerFreezePitch);
        player.yRotO = playerFreezeYaw;
        player.xRotO = playerFreezePitch;
        player.yHeadRot = playerFreezeYaw;
        player.yHeadRotO = playerFreezeYaw;
        player.yBodyRot = playerFreezeYaw;
        player.yBodyRotO = playerFreezeYaw;
    }
}
