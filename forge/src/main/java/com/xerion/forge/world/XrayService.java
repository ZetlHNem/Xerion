package com.xerion.forge.world;

import com.xerion.common.world.WorldMiningRules;
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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public final class XrayService {
    private static String cachedRawIds = null;
    private static Set<String> cachedIds = Set.of();

    private XrayService() {
    }

    public static boolean enabled() {
        return VisualSettings.xrayEnabled() && !selectedIds().isEmpty();
    }

    public static boolean shouldRenderBlock(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && selectedIds().contains(id.toString());
    }

    public static boolean shouldRenderBlock(BlockGetter level, BlockPos pos, BlockState state) {
        if (!shouldRenderBlock(state)) {
            return false;
        }
        return !VisualSettings.xrayExposedOnly() || isExposed(level, pos);
    }

    public static boolean shouldRenderFace(BlockGetter level, BlockPos pos, Direction direction) {
        if (!VisualSettings.xrayExposedOnly()) {
            return true;
        }
        BlockState neighbor = level.getBlockState(pos.relative(direction));
        return neighbor.isAir() || !neighbor.canOcclude();
    }

    public static boolean isExposed(BlockGetter level, BlockPos pos) {
        if (!VisualSettings.xrayExposedOnly()) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.isAir() || !neighbor.canOcclude()) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> selectedIds() {
        String raw = VisualSettings.xrayBlockIds();
        if (raw.equals(cachedRawIds)) {
            return cachedIds;
        }
        cachedRawIds = raw;
        cachedIds = WorldMiningRules.xraySelectedIds(raw);
        return cachedIds;
    }
}
