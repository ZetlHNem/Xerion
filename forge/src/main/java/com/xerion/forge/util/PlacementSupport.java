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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class PlacementSupport {
    private static final Direction[] PLACE_DIRECTIONS = {
            Direction.DOWN,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST,
            Direction.UP
    };

    private PlacementSupport() {
    }

    public static PlacementTarget supportedPlacement(ClientLevel level, BlockPos placePos) {
        if (!level.isEmptyBlock(placePos)) {
            return null;
        }
        return supportedPlacementUnchecked(level, placePos);
    }

    public static PlacementTarget supportedPlacement(ClientLevel level, LocalPlayer player, BlockPos placePos,
                                                  boolean rayTrace) {
        if (!level.isEmptyBlock(placePos)) {
            return null;
        }
        if (rayTrace && !WorldGeometry.visible(player, level, Vec3.atCenterOf(placePos))) {
            return null;
        }
        return supportedPlacementUnchecked(level, placePos);
    }

    private static PlacementTarget supportedPlacementUnchecked(ClientLevel level, BlockPos placePos) {
        for (Direction direction : PLACE_DIRECTIONS) {
            BlockPos clicked = placePos.relative(direction);
            Direction face = direction.getOpposite();
            BlockState state = level.getBlockState(clicked);
            if (!state.isFaceSturdy(level, clicked, face)) {
                continue;
            }
            Vec3 hit = Vec3.atCenterOf(clicked).add(face.getStepX() * 0.5D, face.getStepY() * 0.5D, face.getStepZ() * 0.5D);
            return new PlacementTarget(placePos.immutable(), clicked.immutable(), face, hit);
        }
        return null;
    }
}
