package com.xerion.forge.packet;

import com.xerion.forge.mixin.ClientPacketListenerAccessor;
import com.xerion.forge.mixin.ConnectionAccessor;
import io.netty.channel.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

/**
 * Static front door for the PacketBlockage subsystem.
 *
 * <p>Outbound packets are intercepted in {@code Connection#send}; inbound
 * packets are intercepted in {@code PacketUtils#ensureRunningOnSameThread}.
 * {@link #sendDirect} and {@link #channelReadSilent} bypass both blockages so
 * queued packets can be flushed without being re-queued.</p>
 */
public final class PacketBlockageService {
    private static final ThreadLocal<Boolean> REPLAYING_INBOUND = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Boolean> DIRECT_SEND = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private PacketBlockageService() {
    }

    public static DirectionalNetworkBlockage outbound() {
        return OutboundNetworkBlockage.get();
    }

    public static DirectionalNetworkBlockage inbound() {
        return InboundNetworkBlockage.get();
    }

    public static boolean isOutboundBlocked(Packet<?> packet) {
        return packet != null && OutboundNetworkBlockage.get().isBlocked(packet);
    }

    public static boolean isInboundBlocked(Packet<?> packet) {
        if (packet == null || REPLAYING_INBOUND.get()) {
            return false;
        }
        return InboundNetworkBlockage.get().isBlocked(packet);
    }

    public static boolean hasAnyBlockage() {
        return OutboundNetworkBlockage.get().isAnyBlockages()
                || InboundNetworkBlockage.get().isAnyBlockages();
    }

    public static boolean isDirectSendActive() {
        return DIRECT_SEND.get();
    }

    /**
     * Marks the current thread as flushing queued packets so the outbound
     * blockage does not re-queue them.
     */
    public static void runDirectSend(Runnable action) {
        DIRECT_SEND.set(Boolean.TRUE);
        try {
            action.run();
        } finally {
            DIRECT_SEND.set(Boolean.FALSE);
        }
    }

    /**
     * Writes a packet straight to the channel, bypassing all blockages.
     */
    public static void sendDirect(Packet<?> packet) {
        if (packet == null) {
            return;
        }
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        Connection connection = listener == null ? null : ((ClientPacketListenerAccessor) listener).xerion$connection();
        if (!(connection instanceof ConnectionAccessor accessor)) {
            return;
        }
        Channel channel = accessor.xerion$channel();
        if (channel != null && channel.isOpen()) {
            channel.writeAndFlush(packet);
        }
    }

    /**
     * Feeds a queued inbound packet back into the vanilla handler, bypassing
     * the inbound blockage.
     */
    public static void channelReadSilent(Packet<?> packet) {
        if (packet == null) {
            return;
        }
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener == null) {
            return;
        }
        REPLAYING_INBOUND.set(Boolean.TRUE);
        try {
            handleUnchecked(packet, listener);
        } finally {
            REPLAYING_INBOUND.set(Boolean.FALSE);
        }
    }

    public static void reset() {
        OutboundNetworkBlockage.get().reset();
        InboundNetworkBlockage.get().reset();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void handleUnchecked(Packet<?> packet, ClientPacketListener listener) {
        ((Packet) packet).handle(listener);
    }
}
