package com.xerion.forge.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lightweight port of OpenOpal's KnownServerManager. OpenOpal uses the Hypixel
 * Mod API for location data; Xerion has no such dependency, so detection is
 * based on the server address, the server brand and a scoreboard heuristic for
 * lobby vs in-game state.
 */
public final class ServerDetector {
    public enum Kind {
        UNKNOWN,
        HYPIXEL,
        CUBECRAFT
    }

    private static final Pattern HYPIXEL_BRAND = Pattern.compile("Hypixel BungeeCord \\(.+\\) <- .+");

    private static Kind kind = Kind.UNKNOWN;
    private static String address = "";
    private static String brand = "";

    private ServerDetector() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ServerData data = minecraft.getCurrentServer();
        String nextAddress = data == null || data.ip == null
                ? ""
                : data.ip.toLowerCase(Locale.ROOT);
        String nextBrand = minecraft.player == null ? "" : String.valueOf(minecraft.player.getServerBrand());
        if (nextAddress.equals(address) && nextBrand.equals(brand)) {
            return;
        }
        address = nextAddress;
        brand = nextBrand;
        detect();
    }

    public static Kind kind() {
        return kind;
    }

    public static boolean isHypixel() {
        return kind == Kind.HYPIXEL;
    }

    public static boolean isCubeCraft() {
        return kind == Kind.CUBECRAFT;
    }

    public static String address() {
        return address;
    }

    public static String brand() {
        return brand;
    }

    /**
     * Heuristic lobby check: Hypixel lobbies show "HYPIXEL" as the sidebar
     * title while in-game scoreboards show the game name.
     */
    public static boolean isHypixelLobby() {
        if (!isHypixel()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return true;
        }
        Objective objective = minecraft.level.getScoreboard().getDisplayObjective(Scoreboard.DISPLAY_SLOT_SIDEBAR);
        if (objective == null) {
            return true;
        }
        return objective.getDisplayName().getString().toUpperCase(Locale.ROOT).contains("HYPIXEL");
    }

    public static void reset() {
        kind = Kind.UNKNOWN;
        address = "";
        brand = "";
    }

    private static void detect() {
        Kind next = Kind.UNKNOWN;
        if (isDomain("hypixel.net") || isDomain("hypixel.io") || isDomain("technoblade.club")) {
            next = Kind.HYPIXEL;
        } else if (isDomain("cubecraft.net")) {
            next = Kind.CUBECRAFT;
        }
        if (next == Kind.HYPIXEL && !brand.isEmpty() && !HYPIXEL_BRAND.matcher(brand).matches()) {
            // A different brand behind a Hypixel-like address means a fake server.
            next = Kind.UNKNOWN;
        }
        kind = next;
    }

    private static boolean isDomain(String domain) {
        String host = address;
        int colon = host.indexOf(':');
        if (colon >= 0) {
            host = host.substring(0, colon);
        }
        return host.equals(domain) || host.endsWith("." + domain);
    }
}
