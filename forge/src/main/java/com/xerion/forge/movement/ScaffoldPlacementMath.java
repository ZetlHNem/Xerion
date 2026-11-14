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
import com.xerion.common.combat.CombatAimRules;
import com.xerion.common.movement.MovementSurfaceRules;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public final class ScaffoldPlacementMath {
    private ScaffoldPlacementMath() {
    }

    public static Rot smoothRotation(Rot from, Rot to, double speed) {
        if (to == null || from == null || speed >= 9.99D) {
            return to;
        }
        double factor = MovementSurfaceRules.scaffoldRotationFactor(speed);
        float yawDelta = CombatAimRules.wrapDegrees(to.yaw() - from.yaw());
        float pitchDelta = to.pitch() - from.pitch();
        return new Rot(
                from.yaw() + yawDelta * (float) factor,
                CombatAimRules.clamp(from.pitch() + pitchDelta * (float) factor, -90.0F, 90.0F)
        );
    }

    public static Rot placementRotation(ClientLevel level, LocalPlayer player, Rot current, BlockPos pos, Direction face,
                                 double reach, boolean onAir) {
        if (current == null) {
            return new Rot(CombatAimRules.wrapDegrees(player.getYRot() - 135.0F), 82.0F);
        }
        if (!onAir || pos == null || face == null) {
            return current;
        }

        Rot calculated = calculate(player.getEyePosition(), faceCenter(pos, face));
        Float[] yawArray = {
                -135F,
                -90F,
                -45F,
                0F,
                45F,
                90F,
                135F,
                180F,
                calculated.yaw()
        };
        Arrays.sort(yawArray, (a, b) -> Float.compare(
                Math.abs(CombatAimRules.wrapDegrees(player.getYRot() - 180.0F - a)),
                Math.abs(CombatAimRules.wrapDegrees(player.getYRot() - 180.0F - b))
        ));

        float[] pitchArray = {75.0F, 82.0F, 87.0F};
        for (float yaw : yawArray) {
            for (float pitch : pitchArray) {
                Rot candidate = new Rot(yaw + rotationJitter(), pitch + rotationJitter());
                if (overBlock(level, player, candidate, pos, face, reach)) {
                    return candidate;
                }
            }

            for (int pitch = -90; pitch < 90; pitch++) {
                Rot candidate = new Rot(yaw, pitch);
                if (overBlock(level, player, candidate, pos, face, reach)) {
                    return candidate;
                }
            }
        }

        return calculated;
    }

    public static Vec3 hitVec(BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        if (face != Direction.UP && face != Direction.DOWN) {
            y += 0.08;
        } else {
            x += randomOffset();
            z += randomOffset();
        }
        if (face == Direction.WEST || face == Direction.EAST) {
            z += randomOffset();
        }
        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += randomOffset();
        }

        return new Vec3(x, y, z);
    }

    public static boolean overBlock(ClientLevel level, LocalPlayer player, Rot rot, BlockPos pos, Direction face, double reach) {
        Vec3 lookVec = Vec3.directionFromRotation(rot.pitch(), rot.yaw());
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 endVec = eyePos.add(lookVec.scale(reach));
        HitResult result = level.clip(new ClipContext(
                eyePos,
                endVec,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (!(result instanceof BlockHitResult blockHit) || result.getType() == HitResult.Type.MISS) {
            return false;
        }
        if (!blockHit.getBlockPos().equals(pos)) {
            return false;
        }
        return MovementSurfaceRules.scaffoldRaytraceAllowed(
                VisualSettings.scaffoldRaytraceMode(),
                blockHit.getDirection() == face
        );
    }

    private static Vec3 faceCenter(BlockPos pos, Direction face) {
        return new Vec3(
                pos.getX() + 0.5D + face.getStepX() * 0.5D,
                pos.getY() + 0.5D + face.getStepY() * 0.5D,
                pos.getZ() + 0.5D + face.getStepZ() * 0.5D
        );
    }

    private static Rot calculate(Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        CombatAimRules.Rotation rotation = CombatAimRules.rotationBetween(0.0D, 0.0D, 0.0D, diff.x, diff.y, diff.z);
        return new Rot(rotation.yaw(), CombatAimRules.clamp(rotation.pitch(), -90.0F, 90.0F));
    }

    private static double randomOffset() {
        return MovementSurfaceRules.scaffoldRandomOffset(Math.random());
    }

    private static float rotationJitter() {
        return MovementSurfaceRules.scaffoldRotationJitter(
                VisualSettings.scaffoldRotationMode(),
                Math.random()
        );
    }

    public record Rot(float yaw, float pitch) {
    }
}
