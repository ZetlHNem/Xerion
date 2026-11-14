package com.xerion.forge.util;

import com.xerion.common.config.PingSpoofSettings;
import com.xerion.forge.packet.PacketBlockageService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Port of OpenOpal's PingSpoof: delays keep-alive / pong responses so the
 * server's latency estimate stays higher, then replays them after the delay.
 * Responses are rebuilt from the 1.20.1 equivalent inbound keep-alive/ping
 * packets, so a cancelled handler never loses the reply.
 */
public final class PingSpoofService {
    private static final Logger LOGGER = LoggerFactory.getLogger("xerion-pingspoof");

    private static final Queue<QueuedPacket> QUEUE = new ConcurrentLinkedQueue<>();
    private static boolean flushing;
    private static boolean lastActive;

    private PingSpoofService() {
    }

    public static boolean captureOutbound(Packet<?> packet) {
        if (flushing || !canProcess(Minecraft.getInstance())) {
            return false;
        }
        if (!(packet instanceof ServerboundKeepAlivePacket)
                && !(packet instanceof ServerboundPongPacket)) {
            return false;
        }
        QUEUE.add(new QueuedPacket(packet, System.currentTimeMillis()));
        return true;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean active = canProcess(minecraft);
        if (!active) {
            if (lastActive) {
                flush();
            }
            lastActive = false;
            return;
        }
        lastActive = true;

        long now = System.currentTimeMillis();
        long delayMs = Math.round(PingSpoofSettings.delayMs());
        QueuedPacket entry;
        while ((entry = QUEUE.peek()) != null && now - entry.queuedAt >= delayMs) {
            QUEUE.poll();
            sendDirect(entry.packet);
        }
    }

    public static void clearRuntimeState() {
        flush();
        lastActive = false;
    }

    private static boolean canProcess(Minecraft minecraft) {
        if (!PingSpoofSettings.enabled() || minecraft == null) {
            return false;
        }
        if (minecraft.hasSingleplayerServer()) {
            return false;
        }
        LocalPlayer player = minecraft.player;
        return player != null && minecraft.getConnection() != null;
    }

    private static void flush() {
        QueuedPacket entry;
        while ((entry = QUEUE.poll()) != null) {
            sendDirect(entry.packet);
        }
    }

    private static void sendDirect(Packet<?> packet) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }
        flushing = true;
        try {
            PacketBlockageService.runDirectSend(() -> connection.send(packet));
        } catch (RuntimeException exception) {
            LOGGER.debug("[PingSpoof] failed to flush queued packet", exception);
        } finally {
            flushing = false;
        }
    }

    private record QueuedPacket(Packet<?> packet, long queuedAt) {
    }
}
