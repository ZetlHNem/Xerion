package com.xerion.forge.gui.dashboard;

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

import java.util.HashMap;
import java.util.Map;

final class SelectionEntryRanker {
    private static final Map<String, Integer> INVENTORY_CLEANER_TRASH_RANKS = inventoryCleanerTrashRanks();

    private SelectionEntryRanker() {
    }

    static int xrayEntryRank(String id) {
        String path = registryPath(id);
        if (path.endsWith("_ore")
                || path.equals("ancient_debris")
                || path.contains("raw_")
                || containsAny(path, "diamond", "emerald", "gold", "iron", "redstone", "lapis", "coal", "copper",
                "quartz", "debris", "ruby", "sapphire", "silver", "tin", "lead", "nickel", "uranium", "osmium",
                "platinum", "zinc", "aluminum", "cobalt")) {
            return 0;
        }
        if (containsAny(path, "spawner", "chest", "barrel", "shulker", "vault")) {
            return 10;
        }
        if (containsAny(path, "grass_block", "dirt", "stone", "deepslate", "sand", "gravel", "netherrack")) {
            return 20;
        }
        if (id != null && id.startsWith("minecraft:")) {
            return 100;
        }
        return 200;
    }

    static int inventoryCleanerTrashRank(String id, String label) {
        Integer vanillaRank = INVENTORY_CLEANER_TRASH_RANKS.get(id);
        if (vanillaRank != null) {
            return vanillaRank;
        }
        String path = id == null ? "" : id.toLowerCase(java.util.Locale.ROOT);
        int colon = path.indexOf(':');
        if (colon >= 0 && colon + 1 < path.length()) {
            path = path.substring(colon + 1);
        }
        String text = (path + " " + (label == null ? "" : label)).toLowerCase(java.util.Locale.ROOT);
        if (containsAny(text, "trash", "junk", "garbage", "waste", "rotten", "spoiled", "poisonous", "rubbish")) {
            return 1;
        }
        if (containsAny(text, "seed", "stick", "twig", "flint", "feather", "bone", "hide", "flesh", "string")) {
            return 12;
        }
        if (containsAny(text, "gravel", "cobble", "pebble", "rubble", "dirt", "sand", "mud", "netherrack")) {
            return 22;
        }
        if (containsAny(text, "grass", "fern", "vine", "kelp", "moss", "bush")) {
            return 32;
        }
        if (containsAny(text, "wooden_", "stone_", "leather_")) {
            return 42;
        }
        return 1000;
    }

    private static Map<String, Integer> inventoryCleanerTrashRanks() {
        Map<String, Integer> ranks = new HashMap<>();
        addTrashRank(ranks, 0,
                "minecraft:rotten_flesh",
                "minecraft:poisonous_potato",
                "minecraft:spider_eye",
                "minecraft:dead_bush",
                "minecraft:bowl");
        addTrashRank(ranks, 10,
                "minecraft:wheat_seeds",
                "minecraft:beetroot_seeds",
                "minecraft:melon_seeds",
                "minecraft:pumpkin_seeds",
                "minecraft:stick",
                "minecraft:flint",
                "minecraft:feather",
                "minecraft:string",
                "minecraft:bone",
                "minecraft:leather",
                "minecraft:rabbit_hide",
                "minecraft:ink_sac",
                "minecraft:glow_ink_sac",
                "minecraft:egg",
                "minecraft:snowball");
        addTrashRank(ranks, 20,
                "minecraft:dirt",
                "minecraft:coarse_dirt",
                "minecraft:rooted_dirt",
                "minecraft:mud",
                "minecraft:gravel",
                "minecraft:sand",
                "minecraft:red_sand",
                "minecraft:clay_ball",
                "minecraft:clay",
                "minecraft:cobblestone",
                "minecraft:cobbled_deepslate",
                "minecraft:granite",
                "minecraft:diorite",
                "minecraft:andesite",
                "minecraft:tuff",
                "minecraft:calcite",
                "minecraft:dripstone_block",
                "minecraft:netherrack",
                "minecraft:basalt",
                "minecraft:blackstone",
                "minecraft:soul_sand",
                "minecraft:soul_soil");
        addTrashRank(ranks, 30,
                "minecraft:grass",
                "minecraft:fern",
                "minecraft:large_fern",
                "minecraft:seagrass",
                "minecraft:kelp",
                "minecraft:vine",
                "minecraft:twisting_vines",
                "minecraft:weeping_vines",
                "minecraft:lily_pad");
        addTrashRank(ranks, 40,
                "minecraft:wooden_sword",
                "minecraft:wooden_shovel",
                "minecraft:wooden_pickaxe",
                "minecraft:wooden_axe",
                "minecraft:wooden_hoe",
                "minecraft:stone_sword",
                "minecraft:stone_shovel",
                "minecraft:stone_pickaxe",
                "minecraft:stone_axe",
                "minecraft:stone_hoe",
                "minecraft:leather_helmet",
                "minecraft:leather_chestplate",
                "minecraft:leather_leggings",
                "minecraft:leather_boots");
        return Map.copyOf(ranks);
    }

    private static void addTrashRank(Map<String, Integer> ranks, int rank, String... ids) {
        for (String id : ids) {
            ranks.put(id, rank);
        }
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private static String registryPath(String id) {
        if (id == null) {
            return "";
        }
        int colon = id.indexOf(':');
        String path = colon >= 0 && colon + 1 < id.length() ? id.substring(colon + 1) : id;
        return path.toLowerCase(java.util.Locale.ROOT);
    }
}
