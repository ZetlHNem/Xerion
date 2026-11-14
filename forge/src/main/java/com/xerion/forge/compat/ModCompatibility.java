package com.xerion.forge.compat;

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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public final class ModCompatibility {
    private ModCompatibility() {
    }

    public static StorageInfo storage(ClientLevel level, BlockPos pos, BlockState state, boolean includeStorageBlocks) {
        if (state.is(Blocks.CHEST)) {
            return new StorageInfo("Chest");
        }
        if (state.is(Blocks.TRAPPED_CHEST)) {
            return new StorageInfo("Trapped Chest");
        }
        if (!includeStorageBlocks) {
            return null;
        }
        if (state.is(Blocks.ENDER_CHEST)) {
            return new StorageInfo("Ender Chest");
        }
        if (state.is(Blocks.BARREL)) {
            return new StorageInfo("Barrel");
        }
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            return new StorageInfo("Shulker Box");
        }
        if (looksLikeStorage(state)) {
            return new StorageInfo(displayName(state, "Storage"));
        }
        return null;
    }

    public static OreInfo ore(BlockState state) {
        String registryId = registryId(state);
        if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
            return new OreInfo(VisualSettings.ORE_DIAMOND, 0xFF48E7FF, "Diamond Ore", registryId);
        }
        if (state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)) {
            return new OreInfo(VisualSettings.ORE_EMERALD, 0xFF37F28B, "Emerald Ore", registryId);
        }
        if (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE) || state.is(Blocks.NETHER_GOLD_ORE)) {
            return new OreInfo(VisualSettings.ORE_GOLD, 0xFFFFD45A, "Gold Ore", registryId);
        }
        if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE)) {
            return new OreInfo(VisualSettings.ORE_IRON, 0xFFFFB58A, "Iron Ore", registryId);
        }
        if (state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {
            return new OreInfo(VisualSettings.ORE_REDSTONE, 0xFFFF4B5F, "Redstone Ore", registryId);
        }
        if (state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)) {
            return new OreInfo(VisualSettings.ORE_LAPIS, 0xFF4C74FF, "Lapis Ore", registryId);
        }
        if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
            return new OreInfo(VisualSettings.ORE_COAL, 0xFF8B95A8, "Coal Ore", registryId);
        }
        if (state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE)) {
            return new OreInfo(VisualSettings.ORE_COPPER, 0xFFFF9A5A, "Copper Ore", registryId);
        }
        if (state.is(Blocks.NETHER_QUARTZ_ORE)) {
            return new OreInfo(VisualSettings.ORE_QUARTZ, 0xFFEFE9D5, "Quartz Ore", registryId);
        }
        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return new OreInfo(VisualSettings.ORE_DEBRIS, 0xFFC4745B, "Ancient Debris", registryId);
        }

        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String id = key == null ? "" : (key.getNamespace() + ":" + key.getPath()).toLowerCase(Locale.ROOT);
        String tags = tagText(state);
        String searchable = id + " " + tags;
        if (!WorldMiningRules.looksLikeOreBlock(id, tags)) {
            return null;
        }
        String name = displayName(state, "Other Ore");
        if (WorldMiningRules.containsAny(searchable, "diamond")) {
            return new OreInfo(VisualSettings.ORE_DIAMOND, 0xFF48E7FF, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "emerald")) {
            return new OreInfo(VisualSettings.ORE_EMERALD, 0xFF37F28B, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "nether_gold", "gold")) {
            return new OreInfo(VisualSettings.ORE_GOLD, 0xFFFFD45A, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "iron")) {
            return new OreInfo(VisualSettings.ORE_IRON, 0xFFFFB58A, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "redstone")) {
            return new OreInfo(VisualSettings.ORE_REDSTONE, 0xFFFF4B5F, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "lapis")) {
            return new OreInfo(VisualSettings.ORE_LAPIS, 0xFF4C74FF, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "coal")) {
            return new OreInfo(VisualSettings.ORE_COAL, 0xFF8B95A8, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "copper")) {
            return new OreInfo(VisualSettings.ORE_COPPER, 0xFFFF9A5A, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "quartz")) {
            return new OreInfo(VisualSettings.ORE_QUARTZ, 0xFFEFE9D5, name, registryId);
        }
        if (WorldMiningRules.containsAny(searchable, "ancient_debris", "debris")) {
            return new OreInfo(VisualSettings.ORE_DEBRIS, 0xFFC4745B, name, registryId);
        }
        return new OreInfo(VisualSettings.ORE_OTHER, 0xFFB9B7D9, name, registryId);
    }

    public static boolean isOre(BlockState state) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String id = key == null ? "" : (key.getNamespace() + ":" + key.getPath()).toLowerCase(Locale.ROOT);
        return WorldMiningRules.looksLikeOreBlock(id, tagText(state));
    }

    public static String registryId(BlockState state) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return key == null ? "" : key.toString();
    }

    private static boolean looksLikeStorage(BlockState state) {
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        String id = key == null ? "" : (key.getNamespace() + ":" + key.getPath()).toLowerCase(Locale.ROOT);
        String tags = tagText(state);
        return WorldMiningRules.looksLikeStorageBlock(id, tags);
    }

    private static String tagText(BlockState state) {
        StringBuilder builder = new StringBuilder();
        state.getTags().forEach(tag -> {
            ResourceLocation location = tag.location();
            builder.append(location.getNamespace())
                    .append(':')
                    .append(location.getPath())
                    .append(' ');
        });
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private static String displayName(BlockState state, String fallback) {
        String name = state.getBlock().getName().getString();
        if (name == null || name.isBlank()) {
            return fallback;
        }
        return name;
    }

    public record OreInfo(int mask, int color, String name, String id) {
    }

    public record StorageInfo(String name) {
    }
}
