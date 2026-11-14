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

import com.xerion.forge.config.VisualSettings;

final class EspItemCatalog {
    private static final EspItem[] ENEMY_MAIN_ITEMS = {
            new EspItem("Bounding Box", 0L, VisualSettings::mobBoxEnabled,
                    VisualSettings::setMobBoxEnabled, PreviewSettings.MOB_BOX),
            new EspItem("Name", 0L, VisualSettings::mobNameEnabled,
                    VisualSettings::setMobNameEnabled, PreviewSettings.MOB_NAME),
            new EspItem("Distance", 0L, VisualSettings::mobNameDistance,
                    VisualSettings::setMobNameDistance, PreviewSettings.MOB_DISTANCE),
            new EspItem("Health Bar", 0L, VisualSettings::mobHealthEnabled,
                    VisualSettings::setMobHealthEnabled, PreviewSettings.MOB_HEALTH),
            enemyItem("Armor Bar", VisualSettings.ESP_ITEM_ARMOR_BAR),
            enemyItem("Absorption", VisualSettings.ESP_ITEM_ABSORPTION),
            enemyItem("Hurt Time", VisualSettings.ESP_ITEM_HURT_TIME)
    };
    private static final EspItem[] ENEMY_FLAG_ITEMS = {
            enemyItem("Sneaking", VisualSettings.ESP_ITEM_SNEAKING),
            enemyItem("Sprinting", VisualSettings.ESP_ITEM_SPRINTING),
            enemyItem("Invisible", VisualSettings.ESP_ITEM_INVISIBLE),
            enemyItem("Burning", VisualSettings.ESP_ITEM_BURNING),
            enemyItem("Blocking", VisualSettings.ESP_ITEM_BLOCKING),
            enemyItem("Using Item", VisualSettings.ESP_ITEM_USING_ITEM),
            enemyItem("Eating", VisualSettings.ESP_ITEM_EATING),
            enemyItem("Drinking", VisualSettings.ESP_ITEM_DRINKING),
            enemyItem("Bow Charging", VisualSettings.ESP_ITEM_BOW_CHARGING),
            enemyItem("Crossbow Charged", VisualSettings.ESP_ITEM_CROSSBOW_CHARGED),
            enemyItem("Elytra", VisualSettings.ESP_ITEM_ELYTRA),
            enemyItem("Swimming", VisualSettings.ESP_ITEM_SWIMMING),
            enemyItem("Glowing", VisualSettings.ESP_ITEM_GLOWING),
            enemyItem("Potion", VisualSettings.ESP_ITEM_POTION),
            enemyItem("Low Health", VisualSettings.ESP_ITEM_LOW_HEALTH),
            enemyItem("Bot", VisualSettings.ESP_ITEM_BOT),
            enemyItem("Friend/Team", VisualSettings.ESP_ITEM_FRIEND_TEAM),
            enemyItem("Ping", VisualSettings.ESP_ITEM_PING),
            enemyItem("Gamemode", VisualSettings.ESP_ITEM_GAMEMODE)
    };
    private static final EspItem[] ENEMY_EQUIPMENT_ITEMS = {
            enemyItem("Held Item Text", VisualSettings.ESP_ITEM_HELD_ITEM_TEXT),
            enemyItem("Held Item Icon", VisualSettings.ESP_ITEM_HELD_ITEM_ICON),
            enemyItem("Armor Text", VisualSettings.ESP_ITEM_ARMOR_TEXT),
            enemyItem("Armor Icons", VisualSettings.ESP_ITEM_ARMOR_ICONS),
            enemyItem("Durability", VisualSettings.ESP_ITEM_DURABILITY),
            enemyItem("Enchants", VisualSettings.ESP_ITEM_ENCHANTS),
            enemyItem("Totem", VisualSettings.ESP_ITEM_TOTEM),
            enemyItem("Shield", VisualSettings.ESP_ITEM_SHIELD),
            enemyItem("Bow/Crossbow", VisualSettings.ESP_ITEM_BOW_CROSSBOW)
    };
    private static final EspItem[] TEAM_MAIN_ITEMS = {
            new EspItem("Bounding Box", 0L, VisualSettings::visualTeamMobBox,
                    VisualSettings::setVisualTeamMobBox, PreviewSettings.MOB_BOX),
            new EspItem("Name", 0L, VisualSettings::visualTeamMobName,
                    VisualSettings::setVisualTeamMobName, PreviewSettings.MOB_NAME),
            new EspItem("Distance", 0L, VisualSettings::visualTeamMobDistance,
                    VisualSettings::setVisualTeamMobDistance, PreviewSettings.MOB_DISTANCE),
            new EspItem("Health Bar", 0L, VisualSettings::visualTeamMobHealth,
                    VisualSettings::setVisualTeamMobHealth, PreviewSettings.MOB_HEALTH),
            teamItem("Armor Bar", VisualSettings.ESP_ITEM_ARMOR_BAR),
            teamItem("Absorption", VisualSettings.ESP_ITEM_ABSORPTION),
            teamItem("Hurt Time", VisualSettings.ESP_ITEM_HURT_TIME)
    };
    private static final EspItem[] TEAM_FLAG_ITEMS = {
            teamItem("Sneaking", VisualSettings.ESP_ITEM_SNEAKING),
            teamItem("Sprinting", VisualSettings.ESP_ITEM_SPRINTING),
            teamItem("Invisible", VisualSettings.ESP_ITEM_INVISIBLE),
            teamItem("Burning", VisualSettings.ESP_ITEM_BURNING),
            teamItem("Blocking", VisualSettings.ESP_ITEM_BLOCKING),
            teamItem("Using Item", VisualSettings.ESP_ITEM_USING_ITEM),
            teamItem("Eating", VisualSettings.ESP_ITEM_EATING),
            teamItem("Drinking", VisualSettings.ESP_ITEM_DRINKING),
            teamItem("Bow Charging", VisualSettings.ESP_ITEM_BOW_CHARGING),
            teamItem("Crossbow Charged", VisualSettings.ESP_ITEM_CROSSBOW_CHARGED),
            teamItem("Elytra", VisualSettings.ESP_ITEM_ELYTRA),
            teamItem("Swimming", VisualSettings.ESP_ITEM_SWIMMING),
            teamItem("Glowing", VisualSettings.ESP_ITEM_GLOWING),
            teamItem("Potion", VisualSettings.ESP_ITEM_POTION),
            teamItem("Low Health", VisualSettings.ESP_ITEM_LOW_HEALTH),
            teamItem("Bot", VisualSettings.ESP_ITEM_BOT),
            teamItem("Friend/Team", VisualSettings.ESP_ITEM_FRIEND_TEAM),
            teamItem("Ping", VisualSettings.ESP_ITEM_PING),
            teamItem("Gamemode", VisualSettings.ESP_ITEM_GAMEMODE)
    };
    private static final EspItem[] TEAM_EQUIPMENT_ITEMS = {
            teamItem("Held Item Text", VisualSettings.ESP_ITEM_HELD_ITEM_TEXT),
            teamItem("Held Item Icon", VisualSettings.ESP_ITEM_HELD_ITEM_ICON),
            teamItem("Armor Text", VisualSettings.ESP_ITEM_ARMOR_TEXT),
            teamItem("Armor Icons", VisualSettings.ESP_ITEM_ARMOR_ICONS),
            teamItem("Durability", VisualSettings.ESP_ITEM_DURABILITY),
            teamItem("Enchants", VisualSettings.ESP_ITEM_ENCHANTS),
            teamItem("Totem", VisualSettings.ESP_ITEM_TOTEM),
            teamItem("Shield", VisualSettings.ESP_ITEM_SHIELD),
            teamItem("Bow/Crossbow", VisualSettings.ESP_ITEM_BOW_CROSSBOW)
    };
    private static final EspItem[] LOCAL_MAIN_ITEMS = {
            new EspItem("Bounding Box", 0L, VisualSettings::visualLocalMobBox,
                    VisualSettings::setVisualLocalMobBox, PreviewSettings.MOB_BOX),
            new EspItem("Name", 0L, VisualSettings::visualLocalMobName,
                    VisualSettings::setVisualLocalMobName, PreviewSettings.MOB_NAME),
            new EspItem("Distance", 0L, VisualSettings::visualLocalMobDistance,
                    VisualSettings::setVisualLocalMobDistance, PreviewSettings.MOB_DISTANCE),
            new EspItem("Health Bar", 0L, VisualSettings::visualLocalMobHealth,
                    VisualSettings::setVisualLocalMobHealth, PreviewSettings.MOB_HEALTH),
            localItem("Armor Bar", VisualSettings.ESP_ITEM_ARMOR_BAR),
            localItem("Absorption", VisualSettings.ESP_ITEM_ABSORPTION),
            localItem("Hurt Time", VisualSettings.ESP_ITEM_HURT_TIME)
    };
    private static final EspItem[] LOCAL_FLAG_ITEMS = {
            localItem("Sneaking", VisualSettings.ESP_ITEM_SNEAKING),
            localItem("Sprinting", VisualSettings.ESP_ITEM_SPRINTING),
            localItem("Invisible", VisualSettings.ESP_ITEM_INVISIBLE),
            localItem("Burning", VisualSettings.ESP_ITEM_BURNING),
            localItem("Blocking", VisualSettings.ESP_ITEM_BLOCKING),
            localItem("Using Item", VisualSettings.ESP_ITEM_USING_ITEM),
            localItem("Eating", VisualSettings.ESP_ITEM_EATING),
            localItem("Drinking", VisualSettings.ESP_ITEM_DRINKING),
            localItem("Bow Charging", VisualSettings.ESP_ITEM_BOW_CHARGING),
            localItem("Crossbow Charged", VisualSettings.ESP_ITEM_CROSSBOW_CHARGED),
            localItem("Elytra", VisualSettings.ESP_ITEM_ELYTRA),
            localItem("Swimming", VisualSettings.ESP_ITEM_SWIMMING),
            localItem("Glowing", VisualSettings.ESP_ITEM_GLOWING),
            localItem("Potion", VisualSettings.ESP_ITEM_POTION),
            localItem("Low Health", VisualSettings.ESP_ITEM_LOW_HEALTH),
            localItem("Bot", VisualSettings.ESP_ITEM_BOT),
            localItem("Friend/Team", VisualSettings.ESP_ITEM_FRIEND_TEAM),
            localItem("Ping", VisualSettings.ESP_ITEM_PING),
            localItem("Gamemode", VisualSettings.ESP_ITEM_GAMEMODE)
    };
    private static final EspItem[] LOCAL_EQUIPMENT_ITEMS = {
            localItem("Held Item Text", VisualSettings.ESP_ITEM_HELD_ITEM_TEXT),
            localItem("Held Item Icon", VisualSettings.ESP_ITEM_HELD_ITEM_ICON),
            localItem("Armor Text", VisualSettings.ESP_ITEM_ARMOR_TEXT),
            localItem("Armor Icons", VisualSettings.ESP_ITEM_ARMOR_ICONS),
            localItem("Durability", VisualSettings.ESP_ITEM_DURABILITY),
            localItem("Enchants", VisualSettings.ESP_ITEM_ENCHANTS),
            localItem("Totem", VisualSettings.ESP_ITEM_TOTEM),
            localItem("Shield", VisualSettings.ESP_ITEM_SHIELD),
            localItem("Bow/Crossbow", VisualSettings.ESP_ITEM_BOW_CROSSBOW)
    };

    private EspItemCatalog() {
    }

    static EspItem[] equipmentItems(PreviewModelMode mode) {
        return switch (mode) {
            case ENEMIES -> ENEMY_EQUIPMENT_ITEMS;
            case TEAM -> TEAM_EQUIPMENT_ITEMS;
            case LOCAL -> LOCAL_EQUIPMENT_ITEMS;
        };
    }

    static EspItem[] flagItems(PreviewModelMode mode) {
        return switch (mode) {
            case ENEMIES -> ENEMY_FLAG_ITEMS;
            case TEAM -> TEAM_FLAG_ITEMS;
            case LOCAL -> LOCAL_FLAG_ITEMS;
        };
    }

    static EspItemGroup[] groups(PreviewModelMode mode) {
        return switch (mode) {
            case ENEMIES -> new EspItemGroup[]{
                    new EspItemGroup("Main", ENEMY_MAIN_ITEMS),
                    new EspItemGroup("Flags", ENEMY_FLAG_ITEMS),
                    new EspItemGroup("Equipment", ENEMY_EQUIPMENT_ITEMS)
            };
            case TEAM -> new EspItemGroup[]{
                    new EspItemGroup("Main", TEAM_MAIN_ITEMS),
                    new EspItemGroup("Flags", TEAM_FLAG_ITEMS),
                    new EspItemGroup("Equipment", TEAM_EQUIPMENT_ITEMS)
            };
            case LOCAL -> new EspItemGroup[]{
                    new EspItemGroup("Main", LOCAL_MAIN_ITEMS),
                    new EspItemGroup("Flags", LOCAL_FLAG_ITEMS),
                    new EspItemGroup("Equipment", LOCAL_EQUIPMENT_ITEMS)
            };
        };
    }

    private static EspItem enemyItem(String label, long item) {
        return new EspItem(label, item,
                () -> VisualSettings.visualEnemyEspItemEnabled(item),
                enabled -> VisualSettings.setVisualEnemyEspItemEnabled(item, enabled),
                PreviewSettings.NONE);
    }

    private static EspItem teamItem(String label, long item) {
        return new EspItem(label, item,
                () -> VisualSettings.visualTeamEspItemEnabled(item),
                enabled -> VisualSettings.setVisualTeamEspItemEnabled(item, enabled),
                PreviewSettings.NONE);
    }

    private static EspItem localItem(String label, long item) {
        return new EspItem(label, item,
                () -> VisualSettings.visualLocalEspItemEnabled(item),
                enabled -> VisualSettings.setVisualLocalEspItemEnabled(item, enabled),
                PreviewSettings.NONE);
    }
}
