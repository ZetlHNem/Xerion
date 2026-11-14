package com.xerion.forge.movement;

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.mixin.MultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;

/**
 * Port of OpenOpal's Fast Break (speed + cooldown). The air / water / mining
 * fatigue slowdown overrides need deeper hooks into vanilla's speed formula and
 * are not ported.
 */
public final class FastBreakService {
    private FastBreakService() {
    }

    public static float modifyDestroySpeed(float original) {
        if (!VisualSettings.fastBreakEnabled()) {
            return original;
        }
        float capped = Math.min(VisualSettings.fastBreakSpeedPercent(), 99.0F);
        float multiplier = 1.0F / (1.0F - capped / 100.0F);
        return original * multiplier;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!VisualSettings.fastBreakEnabled()
                || !VisualSettings.fastBreakCooldownEnabled()
                || minecraft.gameMode == null) {
            return;
        }
        ((MultiPlayerGameModeAccessor) minecraft.gameMode)
                .xerion$setDestroyDelay(VisualSettings.fastBreakCooldown());
    }

    public static void clearRuntimeState() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            ((MultiPlayerGameModeAccessor) minecraft.gameMode).xerion$setDestroyDelay(0);
        }
    }
}
