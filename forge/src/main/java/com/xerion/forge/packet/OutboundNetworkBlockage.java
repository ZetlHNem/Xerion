package com.xerion.forge.packet;

import net.minecraft.network.protocol.Packet;

public final class OutboundNetworkBlockage extends DirectionalNetworkBlockage {
    private static final OutboundNetworkBlockage INSTANCE = new OutboundNetworkBlockage();

    private OutboundNetworkBlockage() {
    }

    public static OutboundNetworkBlockage get() {
        return INSTANCE;
    }

    @Override
    protected void flushPacket(Packet<?> packet) {
        PacketBlockageService.sendDirect(packet);
    }
}
