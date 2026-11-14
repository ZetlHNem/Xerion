package com.xerion.forge.packet;

import net.minecraft.network.protocol.Packet;

@FunctionalInterface
public interface PacketTransformer {
    Packet<?> transform(Packet<?> packet);
}
