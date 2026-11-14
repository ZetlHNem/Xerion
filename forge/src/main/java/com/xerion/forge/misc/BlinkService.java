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
import com.xerion.forge.packet.PacketBlockageService;
import com.xerion.common.runtime.MiscRuntimeRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.ArrayDeque;
import java.util.Queue;

public final class BlinkService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Queue<Packet<?>> QUEUED_PACKETS = new ArrayDeque<>();

    private static boolean releasing;
    private static boolean wasEnabled;
    private static int pulseTicks;
    private static float lastHealth = Float.NaN;

    private BlinkService() {
    }

    public static void tick() {
        if (!VisualSettings.blinkEnabled() || MC.player == null || MC.getConnection() == null) {
            if (wasEnabled) {
                releaseQueued();
            } else if (MC.player == null || MC.getConnection() == null) {
                clear();
            }
            wasEnabled = false;
            pulseTicks = 0;
            lastHealth = Float.NaN;
            return;
        }

        float health = MC.player.getHealth() + MC.player.getAbsorptionAmount();
        if (!wasEnabled) {
            pulseTicks = 0;
            lastHealth = health;
        }
        wasEnabled = true;

        if (MiscRuntimeRules.blinkCancelOnHealthDrop(
                VisualSettings.blinkCancelOnHurt(),
                !Float.isNaN(lastHealth),
                health,
                lastHealth)) {
            releaseQueued();
            VisualSettings.setBlinkEnabled(false);
            wasEnabled = false;
            pulseTicks = 0;
            lastHealth = health;
            return;
        }
        lastHealth = health;

        if (MiscRuntimeRules.blinkPulseMode(VisualSettings.blinkMode())) {
            pulseTicks++;
            if (MiscRuntimeRules.blinkPulseReady(pulseTicks, VisualSettings.blinkPulseTicks())) {
                releaseQueued();
                pulseTicks = 0;
            }
        }
    }

    public static boolean capture(Packet<?> packet) {
        if (releasing || !VisualSettings.blinkEnabled() || MC.player == null || MC.getConnection() == null) {
            return false;
        }
        if (VelocityModeService.silentSendActive() || CriticalsService.silentSendActive()) {
            return false;
        }
        if (!(packet instanceof ServerboundMovePlayerPacket)) {
            return false;
        }

        QUEUED_PACKETS.add(packet);
        int maxPackets = MiscRuntimeRules.blinkMaxPackets(VisualSettings.blinkMaxPackets());
        if (QUEUED_PACKETS.size() >= maxPackets) {
            releaseQueued();
        }
        return true;
    }

    public static int queuedPackets() {
        return QUEUED_PACKETS.size();
    }

    public static void clearRuntimeState() {
        clear();
        releasing = false;
        wasEnabled = false;
        pulseTicks = 0;
        lastHealth = Float.NaN;
    }

    private static void releaseQueued() {
        ClientPacketListener listener = MC.getConnection();
        if (listener == null || QUEUED_PACKETS.isEmpty()) {
            QUEUED_PACKETS.clear();
            return;
        }

        releasing = true;
        try {
            PacketBlockageService.runDirectSend(() -> {
                while (!QUEUED_PACKETS.isEmpty()) {
                    listener.send(QUEUED_PACKETS.poll());
                }
            });
        } finally {
            releasing = false;
        }
    }

    private static void clear() {
        QUEUED_PACKETS.clear();
    }
}
