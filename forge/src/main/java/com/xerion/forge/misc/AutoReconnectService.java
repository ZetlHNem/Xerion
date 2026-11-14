package com.xerion.forge.misc;

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
import com.xerion.common.runtime.MiscRuntimeRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class AutoReconnectService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static ServerData lastServer;
    private static Screen lastParent;
    private static DisconnectedScreen activeDisconnectScreen;
    private static long disconnectSeenMillis;
    private static boolean reconnecting;

    private AutoReconnectService() {
    }

    public static void tick() {
        captureServer();
        if (!VisualSettings.autoReconnectEnabled()) {
            resetDisconnectState();
            return;
        }
        if (!(MC.screen instanceof DisconnectedScreen disconnectedScreen)) {
            resetDisconnectState();
            return;
        }
        if (lastServer == null || lastServer.ip == null || lastServer.ip.isBlank()) {
            return;
        }
        if (activeDisconnectScreen != disconnectedScreen) {
            activeDisconnectScreen = disconnectedScreen;
            disconnectSeenMillis = System.currentTimeMillis();
            reconnecting = false;
        }
        if (reconnecting) {
            return;
        }
        long now = System.currentTimeMillis();
        long delayMillis = MiscRuntimeRules.reconnectDelayMillis(VisualSettings.autoReconnectDelay());
        if (!MiscRuntimeRules.reconnectDelayElapsed(now, disconnectSeenMillis, delayMillis)) {
            return;
        }
        reconnecting = true;
        ServerData target = lastServer;
        Screen parent = lastParent != null ? lastParent : new JoinMultiplayerScreen(null);
        ConnectScreen.startConnecting(parent, MC, ServerAddress.parseString(target.ip), target, false);
    }

    private static void captureServer() {
        if (MC.level == null || MC.player == null || MC.hasSingleplayerServer()) {
            return;
        }
        ServerData current = MC.getCurrentServer();
        if (current != null && current.ip != null && !current.ip.isBlank()) {
            lastServer = current;
        }
    }

    private static void resetDisconnectState() {
        activeDisconnectScreen = null;
        disconnectSeenMillis = 0L;
        reconnecting = false;
    }
}
