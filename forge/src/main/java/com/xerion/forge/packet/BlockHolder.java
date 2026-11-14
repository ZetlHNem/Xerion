package com.xerion.forge.packet;

/**
 * Owns a single {@link NetworkBlock}. {@link #block()} activates the block,
 * {@link #release()} flushes queued packets and deactivates it, and
 * {@link #flush()} does both in sequence.
 */
public final class BlockHolder {
    private final DirectionalNetworkBlockage networkBlockage;
    private final boolean priority;
    private NetworkBlock networkBlock;

    public BlockHolder(DirectionalNetworkBlockage networkBlockage) {
        this(networkBlockage, false);
    }

    public BlockHolder(DirectionalNetworkBlockage networkBlockage, boolean priority) {
        this.networkBlockage = networkBlockage;
        this.priority = priority;
    }

    public void block(PacketTransformer packetTransformer, PacketValidator packetValidator) {
        if (networkBlock == null) {
            networkBlock = networkBlockage.newBlockage(packetTransformer, packetValidator, priority);
        }
    }

    public void block(PacketTransformer packetTransformer) {
        block(packetTransformer, null);
    }

    public void block() {
        block(null, null);
    }

    public void setPacketTransformer(PacketTransformer packetTransformer) {
        if (networkBlock != null) {
            networkBlock.setPacketTransformer(packetTransformer);
        }
    }

    public void release() {
        if (networkBlock != null) {
            networkBlockage.releaseBlockage(networkBlock);
            networkBlock = null;
        }
    }

    public void flush() {
        release();
        block();
    }

    public void tickBlockedPackets() {
        if (networkBlock != null) {
            networkBlockage.tickBlockedPackets(networkBlock);
        }
    }

    public void releasePacketsOlderThan(int maxAgeTicks) {
        if (networkBlock != null) {
            networkBlockage.releasePacketsOlderThan(networkBlock, maxAgeTicks,
                    networkBlock.getPacketTransformer());
        }
    }

    public boolean isBlocking() {
        return networkBlock != null;
    }
}
