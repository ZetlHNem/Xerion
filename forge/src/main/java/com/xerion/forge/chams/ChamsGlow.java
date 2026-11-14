package com.xerion.forge.chams;

import com.xerion.common.visual.ChamsRenderContext;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ChamsGlow {
    private ChamsGlow() {
    }

    public static boolean shouldGlow(Entity entity) {
        if (!(entity instanceof LivingEntity living)
                || living.isSpectator()) {
            return false;
        }
        if (ChamsRenderContext.previewRender()) {
            return VisualSettings.visualGlow();
        }
        return ChamsProfileResolver.profile(living).glowEnabled();
    }

    public static int color() {
        return VisualSettings.visualGlowColor() & 0x00FFFFFF;
    }

    public static int color(Entity entity) {
        return argbColor(entity) & 0x00FFFFFF;
    }

    public static int argbColor(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return VisualSettings.visualGlowColor();
        }
        return ChamsProfileResolver.profile(living).glowColor();
    }
}
