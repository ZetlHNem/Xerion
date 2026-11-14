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

import com.xerion.common.movement.MovementSurfaceRules;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SafeWalkSupport {
    private static final double OPEN_OPAL_EDGE_OFFSET_Y = -0.5D;
    private static final double OPEN_OPAL_EDGE_INSET = 0.3D;
    private static final double GROUND_PROBE = 0.08D;
    private static final double INPUT_MIN_PROBE = 0.055D;
    private static final double INPUT_EXTRA_PROBE = 0.018D;
    private static final double CENTER_OVERHANG = 0.150D;
    private static final double SUPPORT_SEARCH_MARGIN = 0.02D;

    private SafeWalkSupport() {
    }

    public static boolean isOnOpenOpalEdge(LocalPlayer player) {
        if (player == null || player.level() == null) {
            return false;
        }
        AABB probe = player.getBoundingBox()
                .move(0.0D, OPEN_OPAL_EDGE_OFFSET_Y, 0.0D)
                .inflate(-OPEN_OPAL_EDGE_INSET, 0.0D, -OPEN_OPAL_EDGE_INSET);
        return !player.level().getBlockCollisions(player, probe).iterator().hasNext();
    }

    public static void applyInputLock(LocalPlayer player, Input input) {
        if (Math.abs(input.forwardImpulse) < 1.0E-5F && Math.abs(input.leftImpulse) < 1.0E-5F) {
            return;
        }

        Vec3 velocity = player.getDeltaMovement();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        double probe = MovementSurfaceRules.safeWalkInputProbe(speed, INPUT_MIN_PROBE, INPUT_EXTRA_PROBE);
        double[] full = inputOffset(player.getYRot(), input.forwardImpulse, input.leftImpulse, probe);
        if (hasSupport(player, full[0], full[1])) {
            return;
        }

        boolean blockedForward = false;
        boolean blockedLeft = false;
        if (Math.abs(input.forwardImpulse) > 1.0E-5F) {
            double[] forwardOnly = inputOffset(player.getYRot(), input.forwardImpulse, 0.0F, probe);
            if (!hasSupport(player, forwardOnly[0], forwardOnly[1])) {
                input.forwardImpulse = 0.0F;
                blockedForward = true;
            }
        }
        if (Math.abs(input.leftImpulse) > 1.0E-5F) {
            double[] leftOnly = inputOffset(player.getYRot(), 0.0F, input.leftImpulse, probe);
            if (!hasSupport(player, leftOnly[0], leftOnly[1])) {
                input.leftImpulse = 0.0F;
                blockedLeft = true;
            }
        }

        if (!blockedForward && !blockedLeft) {
            input.forwardImpulse = 0.0F;
            input.leftImpulse = 0.0F;
        }
    }

    public static Vec3 clipVelocity(LocalPlayer player, Vec3 movement) {
        double x = movement.x;
        double z = movement.z;
        if (hasSupport(player, x, z)) {
            return movement;
        }
        if (x != 0.0D && !hasSupport(player, x, 0.0D)) {
            x = 0.0D;
        }
        if (z != 0.0D && !hasSupport(player, 0.0D, z)) {
            z = 0.0D;
        }
        if (x != 0.0D && z != 0.0D && !hasSupport(player, x, z)) {
            x = 0.0D;
            z = 0.0D;
        }
        return new Vec3(x, movement.y, z);
    }

    private static double[] inputOffset(float yaw, float forward, float left, double probe) {
        com.xerion.common.movement.MovementMath.MoveInput input = MovementMath.normalizedMoveInput(forward, left);
        return MovementMath.directionalMotion(yaw, input.forward(), input.strafe(), probe);
    }

    private static boolean hasSupport(LocalPlayer player, double offsetX, double offsetZ) {
        SupportBounds support = supportBounds(player);
        if (support == null) {
            return true;
        }

        double currentX = player.getX();
        double currentZ = player.getZ();
        double targetX = currentX + offsetX;
        double targetZ = currentZ + offsetZ;
        return axisAllowed(currentX, targetX, support.minX, support.maxX)
                && axisAllowed(currentZ, targetZ, support.minZ, support.maxZ);
    }

    private static boolean axisAllowed(double current, double target, double supportMin, double supportMax) {
        return MovementSurfaceRules.supportAxisAllowed(current, target, supportMin, supportMax, CENTER_OVERHANG);
    }

    private static SupportBounds supportBounds(LocalPlayer player) {
        AABB box = player.getBoundingBox();
        int y = (int) Math.floor(box.minY - GROUND_PROBE);
        int minX = (int) Math.floor(box.minX - SUPPORT_SEARCH_MARGIN);
        int maxX = (int) Math.floor(box.maxX + SUPPORT_SEARCH_MARGIN);
        int minZ = (int) Math.floor(box.minZ - SUPPORT_SEARCH_MARGIN);
        int maxZ = (int) Math.floor(box.maxZ + SUPPORT_SEARCH_MARGIN);

        double supportMinX = Double.POSITIVE_INFINITY;
        double supportMaxX = Double.NEGATIVE_INFINITY;
        double supportMinZ = Double.POSITIVE_INFINITY;
        double supportMaxZ = Double.NEGATIVE_INFINITY;
        boolean found = false;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockState state = player.level().getBlockState(pos);
                if (state.getCollisionShape(player.level(), pos).isEmpty()) {
                    continue;
                }
                found = true;
                supportMinX = Math.min(supportMinX, x);
                supportMaxX = Math.max(supportMaxX, x + 1.0D);
                supportMinZ = Math.min(supportMinZ, z);
                supportMaxZ = Math.max(supportMaxZ, z + 1.0D);
            }
        }
        return found ? new SupportBounds(supportMinX, supportMaxX, supportMinZ, supportMaxZ) : null;
    }

    private record SupportBounds(double minX, double maxX, double minZ, double maxZ) {
    }
}
