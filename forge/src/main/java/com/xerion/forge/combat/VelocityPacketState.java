package com.xerion.forge.combat;

public final class VelocityPacketState {
    private static int handledTick = Integer.MIN_VALUE;

    private VelocityPacketState() {
    }

    public static void markHandled(int tickCount) {
        handledTick = tickCount;
    }

    public static boolean consumeHandled(int tickCount) {
        if (handledTick == Integer.MIN_VALUE || tickCount < handledTick || tickCount > handledTick + 1) {
            return false;
        }
        handledTick = Integer.MIN_VALUE;
        return true;
    }

    public static void clear() {
        handledTick = Integer.MIN_VALUE;
    }
}
