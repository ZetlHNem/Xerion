package com.xerion.forge.misc;

import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Port of OpenOpal's Auto Hypixel (chat part): automatic "gg" after a game and
 * leaving the game when a player in it gets banned. The auto-play branch needs
 * the Hypixel Mod API location mode and is not ported.
 */
public final class AutoHypixelService {
    private static final List<Pattern> KARMA_PATTERNS = List.of(
            Pattern.compile("^ +1st Killer - ?\\[?\\w*\\+*\\]? \\w+ - \\d+(?: Kills?)?$"),
            Pattern.compile("^ *1st (?:Place ?)?(?:-|:)? ?\\[?\\w*\\+*\\]? \\w+(?: : \\d+| - \\d+(?: Points?)?| - \\d+(?: x .)?| \\(\\w+ .{1,6}\\) - \\d+ Kills?|: \\d+:\\d+| - \\d+ (?:Zombie )?(?:Kills?|Blocks? Destroyed)| - \\[LINK\\])?$"),
            Pattern.compile("^ +Winn(?:er #1 \\(\\d+ Kills\\): \\w+ \\(\\w+\\)|er(?::| - )(?:Hiders|Seekers|Defenders|Attackers|PLAYERS?|MURDERERS?|Red|Blue|RED|BLU|\\w+)(?: Team)?|ers?: ?\\[?\\w*\\+*\\]? \\w+(?:, ?\\[?\\w*\\+*\\]? \\w+)?|ing Team ?[\\:-] (?:Animals|Hunters|Red|Green|Blue|Yellow|RED|BLU|Survivors|Vampires))$"),
            Pattern.compile("^ +Alpha Infected: \\w+ \\(\\d+ infections?\\)$"),
            Pattern.compile("^ +Murderer: \\w+ \\(\\d+ Kills?\\)$"),
            Pattern.compile("^ +You survived \\d+ rounds!$"),
            Pattern.compile("^ +(?:UHC|SkyWars|Bridge|Sumo|Classic|OP|MegaWalls|Bow|NoDebuff|Blitz|Combo|Bow Spleef) (?:Duel|Doubles|3v3|4v4|Teams|Deathmatch|2v2v2v2|3v3v3v3)? ?- \\d+:\\d+$"),
            Pattern.compile("^ +They captured all wools!$"),
            Pattern.compile("^ +Game over!$"),
            Pattern.compile("^ +[\\d\\.]+k?/[\\d\\.]+k? \\w+$"),
            Pattern.compile("^ +(?:Criminal|Cop)s won the game!$"),
            Pattern.compile("^ +\\[?\\w*\\+*\\]? \\w+ - \\d+ Final Kills$"),
            Pattern.compile("^ +Zombies - \\d*:?\\d+:\\d+ \\(Round \\d+\\)$"),
            Pattern.compile("^ +. YOUR STATISTICS .$"),
            Pattern.compile("^ {36}Winner(s?)$"),
            Pattern.compile("^ {21}Bridge CTF [a-zA-Z]+ - \\d\\d:\\d\\d$")
    );

    private static long lastAutoGG;

    private AutoHypixelService() {
    }

    public static void onChat(Component message) {
        if (message == null || !ServerDetector.isHypixel()) {
            return;
        }
        String text = message.getString();
        if (VisualSettings.autoLeaveOnBanEnabled()
                && text.equals("A player has been removed from your game.")) {
            sendCommand("l");
            return;
        }
        if (VisualSettings.autoGGEnabled()
                && System.currentTimeMillis() - lastAutoGG > 5_000L
                && KARMA_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(text).matches())) {
            sendCommand("ac " + VisualSettings.autoGGMessage());
            lastAutoGG = System.currentTimeMillis();
        }
    }

    public static void clearRuntimeState() {
        lastAutoGG = 0L;
    }

    private static void sendCommand(String command) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.sendCommand(command);
        }
    }
}
