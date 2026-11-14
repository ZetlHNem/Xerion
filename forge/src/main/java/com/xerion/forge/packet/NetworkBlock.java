package com.xerion.forge.packet;

/**
 * One active blockage. Packets matching {@link #packetValidator} are queued
 * until this block is released. The optional transformer can rewrite packets
 * while they are flushed.
 */
public final class NetworkBlock {
    private PacketTransformer packetTransformer;
    private final PacketValidator packetValidator;
    private final boolean priority;
    private final long id;
    private final long creationTime = System.currentTimeMillis();

    NetworkBlock(PacketTransformer packetTransformer, PacketValidator packetValidator,
                 boolean priority, long id) {
        this.packetTransformer = packetTransformer;
        this.packetValidator = packetValidator;
        this.priority = priority;
        this.id = id;
    }

    public PacketTransformer getPacketTransformer() {
        return packetTransformer;
    }

    public void setPacketTransformer(PacketTransformer packetTransformer) {
        this.packetTransformer = packetTransformer;
    }

    public PacketValidator getPacketValidator() {
        return packetValidator;
    }

    public long getId() {
        return id;
    }

    public boolean isPriority() {
        return priority;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NetworkBlock block && block.id == id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
