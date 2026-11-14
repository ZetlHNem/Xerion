package com.xerion.forge.misc;

/**
 * Port of OpenOpal's SkipTickUtility: requests that the local player's next
 * client tick(s) are skipped entirely.
 */
public final class SkipTickService {
    private static int skipTicks;

    private SkipTickService() {
    }

    public static void addSkipTicks(int ticks) {
        if (ticks > 0) {
            skipTicks += ticks;
        }
    }

    public static boolean consumeSkipTick() {
        if (skipTicks > 0) {
            skipTicks--;
            return true;
        }
        return false;
    }

    public static void reset() {
        skipTicks = 0;
    }
}
