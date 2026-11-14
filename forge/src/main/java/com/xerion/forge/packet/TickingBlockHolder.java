package com.xerion.forge.packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds one blockage per tick and releases them one by one, used for short
 * packet-delay patterns such as queueing movements for N ticks.
 */
public final class TickingBlockHolder {
    private final DirectionalNetworkBlockage networkBlockage;
    private final PacketValidator packetValidator;
    private final List<NetworkBlock> networkBlockList = new ArrayList<>();

    public TickingBlockHolder(DirectionalNetworkBlockage networkBlockage) {
        this(networkBlockage, null);
    }

    public TickingBlockHolder(DirectionalNetworkBlockage networkBlockage, PacketValidator packetValidator) {
        this.networkBlockage = networkBlockage;
        this.packetValidator = packetValidator;
    }

    public void tick() {
        synchronized (networkBlockList) {
            networkBlockList.add(networkBlockage.newBlockage(null, packetValidator));
        }
    }

    public void release(int count) {
        synchronized (networkBlockList) {
            while (!networkBlockList.isEmpty() && count > 0) {
                networkBlockage.releaseBlockage(networkBlockList.remove(0));
                count--;
            }
        }
    }

    public void release() {
        synchronized (networkBlockList) {
            while (!networkBlockList.isEmpty()) {
                networkBlockage.releaseBlockage(networkBlockList.remove(0));
            }
        }
    }

    public boolean isBlocking() {
        synchronized (networkBlockList) {
            return !networkBlockList.isEmpty();
        }
    }

    public int getTickCount() {
        synchronized (networkBlockList) {
            return networkBlockList.size();
        }
    }
}
