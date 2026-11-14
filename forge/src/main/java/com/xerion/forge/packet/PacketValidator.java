package com.xerion.forge.packet;

import net.minecraft.network.protocol.Packet;

@FunctionalInterface
public interface PacketValidator {
    boolean isValid(Packet<?> packet);
}
