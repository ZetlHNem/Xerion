package com.xerion.common.world;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class WorldMiningRules {
    public static final long DAY_LENGTH = 24000L;

    private WorldMiningRules() {
    }

    public static int scanRadius(double range) {
        return (int) Math.ceil(range);
    }

    public static double rangeDistanceSq(double range) {
        return range * range;
    }

    public static int compareNukerCandidates(String sortMode,
                                             int leftY, double leftDistanceSq,
                                             int rightY, double rightDistanceSq) {
        return switch (sortMode) {
            case "Farthest" -> Double.compare(rightDistanceSq, leftDistanceSq);
            case "Top Down" -> compareYThenDistance(rightY, rightDistanceSq, leftY, leftDistanceSq);
            case "Bottom Up" -> compareYThenDistance(leftY, leftDistanceSq, rightY, rightDistanceSq);
            default -> Double.compare(leftDistanceSq, rightDistanceSq);
        };
    }

    public static boolean nukerTargetAllowed(String targetMode, boolean listedBlockAllowed,
                                             boolean sameBlockMatches, boolean exposed) {
        return switch (targetMode) {
            case "Whitelist" -> listedBlockAllowed;
            case "Same Block" -> sameBlockMatches;
            default -> exposed;
        };
    }

    public static int veinMinerMaxBlocks(int configuredMaxBlocks) {
        return Math.max(1, configuredMaxBlocks);
    }

    public static int veinMinerScanLimit(int maxBlocks) {
        return Math.max(maxBlocks * 4, maxBlocks);
    }

    public static boolean twentySixWayAdjacent(int dx, int dy, int dz) {
        return dx <= 1 && dy <= 1 && dz <= 1 && (dx != 0 || dy != 0 || dz != 0);
    }

    public static double axisDistance(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0D;
    }

    public static int tunnelRadius(int limit) {
        return Math.max(1, Math.min(2, (int) Math.floor(Math.sqrt(limit / 3.0D)) / 2));
    }

    public static int tunnelDepth(int limit, int radius) {
        return Math.max(1, limit / ((radius * 2 + 1) * (radius * 2 + 1)));
    }

    public static int sphereRadius(int limit) {
        return Math.max(1, (int) Math.ceil(Math.cbrt(limit) * 0.75D));
    }

    public static int planeRadius(int limit) {
        return Math.max(1, (int) Math.ceil(Math.sqrt(limit)) / 2);
    }

    public static int stairWidth(int limit) {
        return Math.max(1, Math.min(3, (int) Math.ceil(Math.sqrt(limit / 4.0D))));
    }

    public static int stairSteps(int limit, int width) {
        return Math.max(1, limit / width);
    }

    public static boolean timeChangerOverridesWorld(String configuredTime) {
        return !"World".equals(configuredTime);
    }

    public static long fixedTimeOfDay(String configuredTime) {
        return switch (configuredTime) {
            case "Sunset" -> 12000L;
            case "Night" -> 13000L;
            case "Midnight" -> 18000L;
            default -> 1000L;
        };
    }

    public static long fixedDayTime(long original, String configuredTime) {
        long dayStart = original - Math.floorMod(original, DAY_LENGTH);
        return dayStart + fixedTimeOfDay(configuredTime);
    }

    public static boolean treeBlockId(String id) {
        return id.contains("_log")
                || id.endsWith(":log")
                || id.contains("_wood")
                || id.contains("_stem")
                || id.contains("_hyphae");
    }

    public static boolean leafLikeId(String id) {
        return id.contains("_leaves") || id.contains("wart_block");
    }

    public static String treeFamilyPath(String path) {
        return path.replace("_stripped", "")
                .replace("stripped_", "")
                .replace("_log", "")
                .replace("_wood", "")
                .replace("_stem", "")
                .replace("_hyphae", "")
                .replace("_leaves", "")
                .replace("_wart_block", "");
    }

    public static boolean looksLikeStorageBlock(String id, String tags) {
        String value = id + " " + tags;
        if (looksLikeNonStorageBlock(value)) {
            return false;
        }
        if (containsAny(id,
                "sophisticatedstorage",
                "ironchest",
                "iron_chest",
                "functionalstorage",
                "lootr")) {
            return true;
        }
        return containsAny(value,
                "chest",
                "barrel",
                "shulker",
                "drawer",
                "crate",
                "safe",
                "locker");
    }

    public static boolean looksLikeOreBlock(String id, String tags) {
        if (looksLikeNonOreBlock(id, tags)) {
            return false;
        }
        if (id.contains("_ore") || id.endsWith("_ore") || id.contains("ore_")) {
            return true;
        }
        return tags.contains("ores") || tags.contains("_ore") || tags.contains("ore/");
    }

    public static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> xraySelectedIds(String rawIds) {
        Set<String> ids = new HashSet<>();
        if (rawIds != null && !rawIds.isBlank()) {
            for (String part : rawIds.split("\\|")) {
                String id = normalizeBlockId(part);
                if (!id.isBlank()) {
                    ids.add(id);
                }
            }
        }
        return Set.copyOf(ids);
    }

    public static String normalizeBlockId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static int compareYThenDistance(int leftY, double leftDistanceSq, int rightY, double rightDistanceSq) {
        int y = Integer.compare(leftY, rightY);
        return y != 0 ? y : Double.compare(leftDistanceSq, rightDistanceSq);
    }

    private static boolean looksLikeNonStorageBlock(String value) {
        return containsAny(value,
                "furnace",
                "smoker",
                "blast_furnace",
                "sign",
                "hanging_sign",
                "lectern",
                "brewing_stand",
                "hopper",
                "dispenser",
                "dropper",
                "crafting_table",
                "anvil",
                "loom",
                "cartography_table",
                "smithing_table",
                "grindstone",
                "stonecutter",
                "enchanting_table",
                "beacon",
                "jukebox",
                "chiseled_bookshelf",
                "decorated_pot",
                "command_block",
                "jigsaw",
                "structure_block",
                "campfire",
                "beehive",
                "bee_nest");
    }

    private static boolean looksLikeNonOreBlock(String id, String tags) {
        String value = id + " " + tags;
        return containsAny(value,
                "log",
                "wood",
                "planks",
                "stem",
                "hyphae",
                "leaves",
                "sapling",
                "stripped",
                "slab",
                "stairs",
                "fence",
                "door",
                "trapdoor",
                "button",
                "pressure_plate",
                "sign",
                "hanging_sign",
                "boat",
                "chest_boat");
    }
}
