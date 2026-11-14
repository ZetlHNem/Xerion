package com.xerion.forge.world;

import com.xerion.forge.misc.*;

import com.xerion.forge.render.*;

import com.xerion.forge.world.*;

import com.xerion.forge.player.*;

import com.xerion.forge.movement.*;

import com.xerion.forge.combat.*;

import com.xerion.forge.*;

import com.xerion.forge.runtime.*;

import com.xerion.forge.event.*;

import com.xerion.forge.util.*;

import com.xerion.forge.config.VisualSettings;
import com.xerion.common.world.WorldMiningRules;

import net.minecraft.world.level.LevelTimeAccess;

public final class TimeChangerService {
    private TimeChangerService() {
    }

    public static boolean enabled() {
        return VisualSettings.timeChangerEnabled()
                && WorldMiningRules.timeChangerOverridesWorld(VisualSettings.timeChangerTime());
    }

    public static long dayTime(long original) {
        if (!enabled()) {
            return original;
        }
        return WorldMiningRules.fixedDayTime(original, VisualSettings.timeChangerTime());
    }

    public static float timeOfDay(LevelTimeAccess access, float partialTick) {
        return access.dimensionType().timeOfDay(dayTime(access.dayTime()));
    }

    public static int moonPhase(LevelTimeAccess access) {
        return access.dimensionType().moonPhase(dayTime(access.dayTime()));
    }

}
