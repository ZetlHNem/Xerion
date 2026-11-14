package com.xerion.forge.combat;

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

import com.xerion.common.combat.AntiBotRules;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class AntiBotService {
    private static final Minecraft MC = Minecraft.getInstance();

    private AntiBotService() {
    }

    public static boolean isBot(LocalPlayer localPlayer, Player player) {
        if (!CombatSettings.antiBotEnabled()
                || localPlayer == null
                || player == null
                || player == localPlayer
                || FakePlayerRegistry.isDummy(player)) {
            return false;
        }

        if (CombatSettings.antiBotInvalidProfileCheck() && invalidProfile(player)) {
            return true;
        }
        if (CombatSettings.antiBotTabListCheck() && missingFromTabList(player)) {
            return true;
        }
        if (CombatSettings.antiBotDuplicateCheck() && duplicatePlayer(player)) {
            return true;
        }
        return CombatSettings.antiBotZeroPingCheck() && zeroPing(player);
    }

    public static int detectedCount() {
        LocalPlayer localPlayer = MC.player;
        ClientLevel level = MC.level;
        if (localPlayer == null || level == null || !CombatSettings.antiBotEnabled()) {
            return 0;
        }
        int count = 0;
        for (Player player : level.players()) {
            if (isBot(localPlayer, player)) {
                count++;
            }
        }
        return count;
    }

    public static String statusFor(Player player) {
        LocalPlayer localPlayer = MC.player;
        if (player == null || localPlayer == null) {
            return "None";
        }
        return isBot(localPlayer, player) ? "Bot" : "Clean";
    }

    private static boolean invalidProfile(Player player) {
        return AntiBotRules.invalidProfile(player.getGameProfile().getName(), player.getUUID());
    }

    private static boolean missingFromTabList(Player player) {
        if (MC.getConnection() == null) {
            return false;
        }
        return MC.getConnection().getPlayerInfo(player.getUUID()) == null;
    }

    private static boolean zeroPing(Player player) {
        if (MC.getConnection() == null) {
            return false;
        }
        PlayerInfo info = MC.getConnection().getPlayerInfo(player.getUUID());
        return info != null && AntiBotRules.zeroPing(info.getLatency());
    }

    private static boolean duplicatePlayer(Player checked) {
        ClientLevel level = MC.level;
        if (level == null) {
            return false;
        }
        UUID checkedUuid = checked.getUUID();
        String checkedName = AntiBotRules.normalizeName(checked.getGameProfile().getName());
        int sameUuid = 0;
        int sameName = 0;
        for (Player player : level.players()) {
            if (player == MC.player || FakePlayerRegistry.isDummy(player)) {
                continue;
            }
            UUID uuid = player.getUUID();
            String name = AntiBotRules.normalizeName(player.getGameProfile().getName());
            if (AntiBotRules.sameUuid(checkedUuid, uuid)) {
                sameUuid++;
            }
            if (AntiBotRules.sameNormalizedName(checkedName, name)) {
                sameName++;
            }
            if (AntiBotRules.duplicateDetected(sameUuid, sameName)) {
                return true;
            }
        }
        return false;
    }
}
