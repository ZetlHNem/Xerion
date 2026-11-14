package com.xerion.forge.misc;

import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port of OpenOpal's AntiStaff: runs a command as soon as a known staff name
 * appears in the tab list or the world.
 */
public final class AntiStaffService {
    private static final Logger LOGGER = LoggerFactory.getLogger("xerion-antistaff");

    private static boolean triggered;

    private AntiStaffService() {
    }

    public static void tick() {
        if (!VisualSettings.antiStaffEnabled()) {
            triggered = false;
            return;
        }
        if (triggered) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientPacketListener connection = minecraft.getConnection();
        if (player == null || connection == null) {
            return;
        }
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            if (StaffNameList.contains(info.getProfile().getName())) {
                exit(connection);
                return;
            }
            Component display = info.getTabListDisplayName();
            if (display != null && StaffNameList.contains(display.getString())) {
                exit(connection);
                return;
            }
        }
        if (minecraft.level != null) {
            for (net.minecraft.world.entity.player.Player other : minecraft.level.players()) {
                if (other != player && StaffNameList.contains(other.getGameProfile().getName())) {
                    exit(connection);
                    return;
                }
            }
        }
    }

    public static void clearRuntimeState() {
        triggered = false;
    }

    private static void exit(ClientPacketListener connection) {
        triggered = true;
        LOGGER.info("[AntiStaff] staff detected, running /{}", VisualSettings.antiStaffCommand());
        connection.sendCommand(VisualSettings.antiStaffCommand());
    }
}
