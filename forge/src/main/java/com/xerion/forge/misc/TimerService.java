package com.xerion.forge.misc;

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.mixin.MinecraftAccessor;
import com.xerion.forge.mixin.TimerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;

/**
 * Port of OpenOpal's Timer module: scales the client tick length to change the
 * local game speed.
 */
public final class TimerService {
    private static final float DEFAULT_MS_PER_TICK = 50.0F;
    private static boolean wasEnabled;

    private TimerService() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        Timer timer = ((MinecraftAccessor) minecraft).xerion$timer();
        if (timer == null) {
            return;
        }
        if (VisualSettings.timerEnabled())
        {
            float speed = Math.max(0.05F, VisualSettings.timerSpeed());
            ((TimerAccessor) timer).xerion$setMsPerTick(DEFAULT_MS_PER_TICK / speed);
            wasEnabled = true;
            return;
        }
        if (wasEnabled) {
            ((TimerAccessor) timer).xerion$setMsPerTick(DEFAULT_MS_PER_TICK);
            wasEnabled = false;
        }
    }

    public static void reset() {
        Minecraft minecraft = Minecraft.getInstance();
        Timer timer = ((MinecraftAccessor) minecraft).xerion$timer();
        if (timer != null) {
            ((TimerAccessor) timer).xerion$setMsPerTick(DEFAULT_MS_PER_TICK);
        }
        wasEnabled = false;
    }
}
