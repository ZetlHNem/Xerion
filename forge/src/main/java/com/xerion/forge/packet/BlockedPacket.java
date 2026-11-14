package com.xerion.forge.packet;

import net.minecraft.network.protocol.Packet;

public final class BlockedPacket {
    private final Packet<?> packet;
    private final long id;
    private final long blockageId;
    private int ageTicks;

    BlockedPacket(Packet<?> packet, long id, long blockageId) {
        this.packet = packet;
        this.id = id;
        this.blockageId = blockageId;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public long getId() {
        return id;
    }

    public long getBlockageId() {
        return blockageId;
    }

    public int getAgeTicks() {
        return ageTicks;
    }

    void tickAge() {
        ageTicks++;
    }
}
