package com.xerion.forge.util;

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

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class WorldGeometry {
    private static final double VISIBILITY_EPSILON = 0.09D;

    private WorldGeometry() {
    }

    public static boolean visible(LocalPlayer player, Level level, Vec3 point) {
        Vec3 eye = player.getEyePosition(1.0F);
        HitResult result = level.clip(new ClipContext(
                eye,
                point,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
        return result.getType() == HitResult.Type.MISS
                || result.getLocation().distanceToSqr(eye) + VISIBILITY_EPSILON >= point.distanceToSqr(eye);
    }

    public static Vec3 topCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
    }
}
