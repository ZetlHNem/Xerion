package com.xerion.forge.packet;

import net.minecraft.network.protocol.Packet;

public final class InboundNetworkBlockage extends DirectionalNetworkBlockage {
    private static final InboundNetworkBlockage INSTANCE = new InboundNetworkBlockage();

    private InboundNetworkBlockage() {
    }

    public static InboundNetworkBlockage get() {
        return INSTANCE;
    }

    @Override
    protected void flushPacket(Packet<?> packet) {
        PacketBlockageService.channelReadSilent(packet);
    }
}
