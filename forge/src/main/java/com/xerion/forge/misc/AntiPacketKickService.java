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

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AntiPacketKickService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XerionForge.MOD_ID);
    private static long lastLogMillis;

    private AntiPacketKickService() {
    }

    public static boolean enabled() {
        return VisualSettings.antiPacketKickEnabled();
    }

    public static void handle(Packet<?> packet, PacketListener listener) {
        try {
            handleUnchecked(packet, listener);
        } catch (Throwable throwable) {
            if (!enabled()) {
                rethrow(throwable);
            }
            log(packet, throwable);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void handleUnchecked(Packet<?> packet, PacketListener listener) {
        ((Packet) packet).handle(listener);
    }

    private static void log(Packet<?> packet, Throwable throwable) {
        long now = System.currentTimeMillis();
        if (now - lastLogMillis < 1000L) {
            return;
        }
        lastLogMillis = now;
        String packetName = packet == null ? "unknown" : packet.getClass().getName();
        LOGGER.warn("[AntiPacketKick] Blocked bad packet {}", packetName, throwable);
    }

    private static void rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (throwable instanceof Error error) {
            throw error;
        }
        throw new RuntimeException(throwable);
    }
}
