package com.xerion.forge.runtime;

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

import com.xerion.forge.compat.ModTeamCompatibility;

import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class SocialProfileStore {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Map<String, FriendEntry> FRIENDS = new LinkedHashMap<>();
    private static boolean loaded;

    private SocialProfileStore() {
    }

    public static boolean isProtected(LocalPlayer player, Player other) {
        if (player == null || other == null || other == player || FakePlayerRegistry.isDummy(other)) {
            return false;
        }
        if (CombatSettings.friendsCheck() && isFriend(other)) {
            return true;
        }
        return CombatSettings.teamsCheck() && isTeamMate(player, other);
    }

    public static boolean isFriend(Player player) {
        if (player == null) {
            return false;
        }
        ensureLoaded();
        UUID uuid = player.getUUID();
        String normalized = normalize(player.getGameProfile().getName());
        for (FriendEntry entry : FRIENDS.values()) {
            if (entry.uuid != null && entry.uuid.equals(uuid)) {
                return true;
            }
        }
        return FRIENDS.containsKey(normalized);
    }

    public static boolean isFriendName(String name) {
        ensureLoaded();
        return FRIENDS.containsKey(normalize(name));
    }

    public static boolean addFriend(Player player) {
        if (player == null || FakePlayerRegistry.isDummy(player)) {
            return false;
        }
        return addFriend(player.getGameProfile().getName(), player.getUUID());
    }

    public static boolean addFriend(String name) {
        return addFriend(name, null);
    }

    public static boolean removeFriend(String name) {
        ensureLoaded();
        String normalized = normalize(name);
        if (normalized.isEmpty() || FRIENDS.remove(normalized) == null) {
            return false;
        }
        save();
        return true;
    }

    public static boolean removeFriend(Player player) {
        if (player == null) {
            return false;
        }
        ensureLoaded();
        String normalized = normalize(player.getGameProfile().getName());
        boolean removed = FRIENDS.remove(normalized) != null;
        UUID uuid = player.getUUID();
        List<String> uuidMatches = new ArrayList<>();
        for (Map.Entry<String, FriendEntry> entry : FRIENDS.entrySet()) {
            if (entry.getValue().uuid != null && entry.getValue().uuid.equals(uuid)) {
                uuidMatches.add(entry.getKey());
            }
        }
        for (String key : uuidMatches) {
            FRIENDS.remove(key);
            removed = true;
        }
        if (removed) {
            save();
        }
        return removed;
    }

    public static boolean toggleFriend(Player player) {
        if (player == null || FakePlayerRegistry.isDummy(player)) {
            return false;
        }
        if (isFriend(player)) {
            return removeFriend(player);
        }
        return addFriend(player);
    }

    public static void clearFriends() {
        ensureLoaded();
        if (FRIENDS.isEmpty()) {
            return;
        }
        FRIENDS.clear();
        save();
    }

    public static int friendCount() {
        ensureLoaded();
        return FRIENDS.size();
    }

    public static List<String> friendNames() {
        ensureLoaded();
        return FRIENDS.values().stream().map(FriendEntry::name).toList();
    }

    public static boolean isTeamMate(LocalPlayer player, Player other) {
        if (player == null || other == null || other == player || FakePlayerRegistry.isDummy(other)) {
            return false;
        }
        String mode = CombatSettings.teamMode();
        if (CombatSettings.TEAM_MODE_VANILLA.equals(mode)) {
            return vanillaTeamMate(player, other);
        }
        if (CombatSettings.TEAM_MODE_SCOREBOARD.equals(mode)) {
            return scoreboardTeamMate(player, other);
        }
        if (CombatSettings.TEAM_MODE_NAME_COLOR.equals(mode)) {
            return nameColorTeamMate(player, other);
        }
        return vanillaTeamMate(player, other)
                || scoreboardTeamMate(player, other)
                || nameColorTeamMate(player, other)
                || ModTeamCompatibility.sameModTeam(player, other);
    }

    public static boolean isStructuralTeamMate(LocalPlayer player, Player other) {
        if (player == null || other == null || other == player || FakePlayerRegistry.isDummy(other)) {
            return false;
        }
        return vanillaTeamMate(player, other)
                || scoreboardTeamMate(player, other)
                || ModTeamCompatibility.sameModTeam(player, other);
    }

    public static String localTeamLabel(LocalPlayer player) {
        if (player == null) {
            return "None";
        }
        Team team = player.getTeam();
        return team == null ? "None" : team.getName();
    }

    private static boolean addFriend(String name, UUID uuid) {
        ensureLoaded();
        String normalized = normalize(name);
        if (normalized.isEmpty()) {
            return false;
        }
        String displayName = name == null ? "" : name.trim();
        FriendEntry previous = FRIENDS.put(normalized, new FriendEntry(displayName, uuid));
        if (previous != null && previous.uuid != null && previous.uuid.equals(uuid)
                && previous.name.equals(displayName)) {
            return false;
        }
        save();
        return true;
    }

    private static boolean vanillaTeamMate(Player player, Player other) {
        return player.isAlliedTo(other) || other.isAlliedTo(player);
    }

    private static boolean scoreboardTeamMate(Player player, Player other) {
        Team playerTeam = player.getTeam();
        Team otherTeam = other.getTeam();
        if (playerTeam == null || otherTeam == null) {
            return false;
        }
        return playerTeam == otherTeam
                || playerTeam.getName().equals(otherTeam.getName())
                || playerTeam.isAlliedTo(otherTeam)
                || otherTeam.isAlliedTo(playerTeam);
    }

    private static boolean nameColorTeamMate(Player player, Player other) {
        int playerColor = player.getTeamColor();
        int otherColor = other.getTeamColor();
        return playerColor != 0xFFFFFF && playerColor == otherColor;
    }

    private static String normalize(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path path = friendsPath();
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                FriendEntry entry = parseEntry(line);
                if (entry != null) {
                    FRIENDS.put(normalize(entry.name), entry);
                }
            }
        } catch (IOException ignored) {
            FRIENDS.clear();
        }
    }

    private static FriendEntry parseEntry(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }
        String[] parts = trimmed.split("\\|", 2);
        String name = parts[0].trim();
        if (normalize(name).isEmpty()) {
            return null;
        }
        UUID uuid = null;
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            try {
                uuid = UUID.fromString(parts[1].trim());
            } catch (IllegalArgumentException ignored) {
                uuid = null;
            }
        }
        return new FriendEntry(name, uuid);
    }

    private static void save() {
        Path path = friendsPath();
        if (path == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent());
            List<String> lines = new ArrayList<>();
            for (FriendEntry entry : FRIENDS.values()) {
                lines.add(entry.uuid == null ? entry.name : entry.name + "|" + entry.uuid);
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static Path friendsPath() {
        if (MC == null || MC.gameDirectory == null) {
            return null;
        }
        return MC.gameDirectory.toPath().resolve("config").resolve("xerion").resolve("friends.txt");
    }

    private record FriendEntry(String name, UUID uuid) {
    }
}
