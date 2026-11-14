package com.xerion.common.config;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class ConfigMigrationRules {
    private static final String LEGACY_DASHBOARD_SCALE_KEY = "cl" + "ientGuiScale";
    private static final String LEGACY_WORLD_TIME_VALUE = "Cl" + "ient";

    private static final Map<String, String> RENAMED_KEYS = Map.ofEntries(
            Map.entry("ForgeVisualSettings.watermarkElementOrder", "VisualSettings.watermarkElementOrderSerialized"),
            Map.entry("VisualSettings.watermarkElementOrder", "VisualSettings.watermarkElementOrderSerialized"),
            Map.entry("VisualSettings.soundManagerEnabled", "VisualSettings.soundControlEnabled"),
            Map.entry("VisualSettings.soundManagerMuteMask", "VisualSettings.soundControlMuteMask"),
            Map.entry("ForgeVisualSettings.soundManagerEnabled", "VisualSettings.soundControlEnabled"),
            Map.entry("ForgeVisualSettings.soundManagerMuteMask", "VisualSettings.soundControlMuteMask"),
            Map.entry("VisualSettings." + LEGACY_DASHBOARD_SCALE_KEY, "VisualSettings.dashboardGuiScale"),
            Map.entry("ForgeVisualSettings." + LEGACY_DASHBOARD_SCALE_KEY, "VisualSettings.dashboardGuiScale")
    );

    private static final Map<String, String> OWNER_ALIASES = Map.ofEntries(
            Map.entry("ForgeVisualSettings", "VisualSettings"),
            Map.entry("ForgeCombatSettings", "CombatSettings"),
            Map.entry("ForgeUiSettings", "UiSettings"),
            Map.entry("ForgeHudPositionSettings", "HudPositionSettings")
    );

    private static final Set<String> HUD_POSITION_KEYS = Set.of(
            "watermarkX",
            "watermarkY",
            "hotkeyHudX",
            "hotkeyHudY",
            "arrayListHudX",
            "arrayListHudY",
            "arrayListHudRightOffset",
            "arrayListHudRightAnchored",
            "targetsHudX",
            "targetsHudY",
            "backpackHudX",
            "backpackHudY",
            "potionBuffHudX",
            "potionBuffHudY",
            "speedHudX",
            "speedHudY",
            "musicLyricsHudX",
            "musicLyricsHudY"
    );

    private ConfigMigrationRules() {
    }

    public static Result migrate(Properties properties, Collection<String> currentKeys) {
        if (properties == null) {
            return new Result(0, 0);
        }
        int migrated = 0;
        int deprecated = 0;

        for (Map.Entry<String, String> alias : RENAMED_KEYS.entrySet()) {
            migrated += copyAlias(properties, alias.getKey(), alias.getValue());
        }

        for (String key : currentKeys) {
            migrated += migrateOwnerAliases(properties, key);
            migrated += migrateGenericAliases(properties, key);
            migrated += migrateMovedHudAliases(properties, key);
        }

        if (!properties.containsKey("UiSettings.theme") && properties.containsKey("ForgeUiSettings.darkTheme")) {
            properties.setProperty("UiSettings.theme",
                    Boolean.parseBoolean(properties.getProperty("ForgeUiSettings.darkTheme")) ? "Dark" : "Light");
            migrated++;
        } else if (!properties.containsKey("UiSettings.theme") && properties.containsKey("UiSettings.darkTheme")) {
            properties.setProperty("UiSettings.theme",
                    Boolean.parseBoolean(properties.getProperty("UiSettings.darkTheme")) ? "Dark" : "Light");
            migrated++;
        }

        migrated += migrateLegacyValues(properties);
        deprecated += removeDeprecatedRuntimeOnlyKeys(properties);
        properties.setProperty(CommonConfigSchema.VERSION_KEY, Integer.toString(CommonConfigSchema.CURRENT_VERSION));
        return new Result(migrated, deprecated);
    }

    private static int migrateOwnerAliases(Properties properties, String key) {
        if (properties.containsKey(key)) {
            return 0;
        }
        int split = key.indexOf('.');
        if (split <= 0 || split + 1 >= key.length()) {
            return 0;
        }
        String owner = key.substring(0, split);
        String legacyOwner = null;
        for (Map.Entry<String, String> alias : OWNER_ALIASES.entrySet()) {
            if (alias.getValue().equals(owner)) {
                legacyOwner = alias.getKey();
                break;
            }
        }
        if (legacyOwner == null) {
            return 0;
        }
        return copyAlias(properties, legacyOwner + key.substring(split), key);
    }

    private static int migrateGenericAliases(Properties properties, String key) {
        if (properties.containsKey(key)) {
            return 0;
        }
        int split = key.indexOf('.');
        if (split <= 0 || split + 1 >= key.length()) {
            return 0;
        }
        String owner = key.substring(0, split);
        String getter = key.substring(split + 1);
        String setter = "set" + Character.toUpperCase(getter.charAt(0)) + getter.substring(1);
        if (copyAlias(properties, getter, key) > 0) {
            return 1;
        }
        if (copyAlias(properties, owner + "." + setter, key) > 0) {
            return 1;
        }
        return copyAlias(properties, setter, key);
    }

    private static int migrateMovedHudAliases(Properties properties, String key) {
        if (properties.containsKey(key) || !key.startsWith("VisualSettings.")) {
            return 0;
        }
        String getter = key.substring("VisualSettings.".length());
        if (!HUD_POSITION_KEYS.contains(getter)) {
            return 0;
        }
        return copyAlias(properties, "HudPositionSettings." + getter, key)
                + copyAlias(properties, "ForgeHudPositionSettings." + getter, key)
                + copyAlias(properties, "ForgeVisualSettings." + getter, key);
    }

    private static int migrateLegacyValues(Properties properties) {
        int migrated = 0;
        if (LEGACY_WORLD_TIME_VALUE.equals(properties.getProperty("VisualSettings.timeChangerTime"))) {
            properties.setProperty("VisualSettings.timeChangerTime", "World");
            migrated++;
        }
        if (LEGACY_WORLD_TIME_VALUE.equals(properties.getProperty("ForgeVisualSettings.timeChangerTime"))) {
            properties.setProperty("ForgeVisualSettings.timeChangerTime", "World");
            migrated++;
        }
        return migrated;
    }
    private static int removeDeprecatedRuntimeOnlyKeys(Properties properties) {
        int removed = 0;
        for (String key : Set.of("UiSettings.darkTheme", "ForgeUiSettings.darkTheme")) {
            if (properties.remove(key) != null) {
                removed++;
            }
        }
        return removed;
    }

    private static int copyAlias(Properties properties, String oldKey, String newKey) {
        if (properties.containsKey(newKey) || !properties.containsKey(oldKey)) {
            return 0;
        }
        properties.setProperty(newKey, properties.getProperty(oldKey));
        return 1;
    }

    public record Result(int migrated, int deprecated) {
    }
}
