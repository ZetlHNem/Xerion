package com.xerion.common.runtime;

public final class HitboxPickContext {
    private static final ThreadLocal<Boolean> PICKING = ThreadLocal.withInitial(() -> false);

    private HitboxPickContext() {
    }

    public static void begin() {
        PICKING.set(true);
    }

    public static void end() {
        PICKING.set(false);
    }

    public static boolean active() {
        return PICKING.get();
    }
}
