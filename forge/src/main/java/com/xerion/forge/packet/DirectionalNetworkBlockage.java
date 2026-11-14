package com.xerion.forge.packet;

import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Queues packets for one direction of the connection while at least one
 * {@link NetworkBlock} is active. Ported from OpenOpal's blockage helper.
 */
public abstract class DirectionalNetworkBlockage {
    private final List<NetworkBlock> blockageList = new ArrayList<>();
    private final List<BlockedPacket> packetList = new ArrayList<>();
    private final Object lock = new Object();
    private long id;

    public NetworkBlock newBlockage() {
        return newBlockage(null, null);
    }

    public NetworkBlock newBlockage(PacketTransformer packetTransformer, PacketValidator packetValidator) {
        return newBlockage(packetTransformer, packetValidator, false);
    }

    public NetworkBlock newBlockage(PacketTransformer packetTransformer, PacketValidator packetValidator,
                                    boolean priority) {
        synchronized (lock) {
            NetworkBlock block = new NetworkBlock(packetTransformer, packetValidator, priority, nextBlockId());
            blockageList.add(block);
            return block;
        }
    }

    private long nextBlockId() {
        long next = id;
        for (NetworkBlock block : blockageList) {
            if (block.isPriority() && next >= block.getId()) {
                next = block.getId();
            }
        }
        return next;
    }

    public void releaseBlockage(NetworkBlock networkBlock) {
        List<Packet<?>> packetsToFlush;
        synchronized (lock) {
            if (!blockageList.remove(networkBlock)) {
                return;
            }
            sort();
            packetsToFlush = collectForFlush(blockageList.isEmpty() ? null : blockageList.get(0).getId(),
                    networkBlock.getPacketTransformer());
        }
        for (Packet<?> packet : packetsToFlush) {
            flushPacket(packet);
        }
    }

    public void tickBlockedPackets(NetworkBlock networkBlock) {
        synchronized (lock) {
            for (BlockedPacket blockedPacket : packetList) {
                if (blockedPacket.getBlockageId() == networkBlock.getId()) {
                    blockedPacket.tickAge();
                }
            }
        }
    }

    public void releasePacketsOlderThan(NetworkBlock networkBlock, int maxAgeTicks,
                                        PacketTransformer packetTransformer) {
        List<Packet<?>> packetsToFlush = new ArrayList<>();
        synchronized (lock) {
            Iterator<BlockedPacket> iterator = packetList.iterator();
            while (iterator.hasNext()) {
                BlockedPacket blockedPacket = iterator.next();
                if (blockedPacket.getBlockageId() != networkBlock.getId()
                        || blockedPacket.getAgeTicks() < maxAgeTicks) {
                    continue;
                }
                Packet<?> packet = transform(blockedPacket.getPacket(), packetTransformer);
                if (packet != null) {
                    packetsToFlush.add(packet);
                }
                iterator.remove();
            }
        }
        for (Packet<?> packet : packetsToFlush) {
            flushPacket(packet);
        }
    }

    public boolean isBlocked(Packet<?> packet) {
        synchronized (lock) {
            if (blockageList.isEmpty()) {
                return false;
            }
            sort();
            for (NetworkBlock block : blockageList) {
                PacketValidator validator = block.getPacketValidator();
                if (validator == null || validator.isValid(packet)) {
                    packetList.add(new BlockedPacket(packet, id, block.getId()));
                    id++;
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isAnyBlockages() {
        synchronized (lock) {
            return !blockageList.isEmpty();
        }
    }

    public void reset() {
        synchronized (lock) {
            blockageList.clear();
            packetList.clear();
            id = 0L;
        }
    }

    private List<Packet<?>> collectForFlush(Long maxBlockId, PacketTransformer packetTransformer) {
        List<Packet<?>> packetsToFlush = new ArrayList<>();
        Iterator<BlockedPacket> iterator = packetList.iterator();
        while (iterator.hasNext()) {
            BlockedPacket blockedPacket = iterator.next();
            if (maxBlockId != null && blockedPacket.getId() >= maxBlockId) {
                continue;
            }
            Packet<?> packet = transform(blockedPacket.getPacket(), packetTransformer);
            if (packet != null) {
                packetsToFlush.add(packet);
            }
            iterator.remove();
        }
        return packetsToFlush;
    }

    private static Packet<?> transform(Packet<?> packet, PacketTransformer transformer) {
        return transformer == null ? packet : transformer.transform(packet);
    }

    private void sort() {
        blockageList.sort(Comparator.comparingLong(NetworkBlock::getId));
        packetList.sort(Comparator.comparingLong(BlockedPacket::getId));
    }

    protected abstract void flushPacket(Packet<?> packet);
}
