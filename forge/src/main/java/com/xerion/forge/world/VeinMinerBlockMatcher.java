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

import com.xerion.forge.compat.ModCompatibility;
import com.xerion.forge.config.VisualSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public final class VeinMinerBlockMatcher {
    private VeinMinerBlockMatcher() {
    }

    public static boolean matchesEnabledFilter(BlockState state) {
        int mask = VisualSettings.veinMinerFilterMask();
        return ((mask & VisualSettings.VEIN_MINER_FILTER_ORES) != 0 && isOre(state))
                || ((mask & VisualSettings.VEIN_MINER_FILTER_TREES) != 0 && isTreeBlock(state));
    }

    public static boolean isPlantLike(BlockState state) {
        return isTreeBlock(state);
    }

    public static boolean sameBlockFamily(BlockState originalState, BlockState state) {
        if (originalState.is(state.getBlock())) {
            return true;
        }
        if (isTreeBlock(originalState) && isTreeBlock(state)) {
            return treeFamily(originalState).equals(treeFamily(state));
        }
        return false;
    }

    private static boolean isOre(BlockState state) {
        return ModCompatibility.ore(state) != null || ModCompatibility.isOre(state);
    }

    private static boolean isTreeBlock(BlockState state) {
        if (isLeafLike(state)) {
            return false;
        }
        if (state.is(BlockTags.LOGS) || state.is(BlockTags.LOGS_THAT_BURN)) {
            return true;
        }
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String id = key == null ? "" : (key.getNamespace() + ":" + key.getPath()).toLowerCase(Locale.ROOT);
        return WorldMiningRules.treeBlockId(id);
    }

    private static boolean isLeafLike(BlockState state) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String id = key == null ? "" : (key.getNamespace() + ":" + key.getPath()).toLowerCase(Locale.ROOT);
        return state.getBlock() instanceof LeavesBlock
                || WorldMiningRules.leafLikeId(id);
    }

    private static String treeFamily(BlockState state) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String path = key == null ? "" : key.getPath().toLowerCase(Locale.ROOT);
        return WorldMiningRules.treeFamilyPath(path);
    }
}
