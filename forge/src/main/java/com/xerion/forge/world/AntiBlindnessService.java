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

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public final class AntiBlindnessService {
    private AntiBlindnessService() {
    }

    public static boolean hideEffect(MobEffect effect) {
        if (!VisualSettings.antiBlindnessEnabled() || effect == null) {
            return false;
        }
        if (effect == MobEffects.BLINDNESS) {
            return VisualSettings.antiBlindnessBlindness();
        }
        if (effect == MobEffects.DARKNESS) {
            return VisualSettings.antiBlindnessDarkness();
        }
        if (effect == MobEffects.CONFUSION) {
            return VisualSettings.antiBlindnessNausea();
        }
        return false;
    }

    public static boolean hideBlindnessOrDarkness(LivingEntity entity) {
        if (entity == null || !VisualSettings.antiBlindnessEnabled()) {
            return false;
        }
        return VisualSettings.antiBlindnessBlindness() && entity.hasEffect(MobEffects.BLINDNESS)
                || VisualSettings.antiBlindnessDarkness() && entity.hasEffect(MobEffects.DARKNESS);
    }

    public static boolean hideNausea() {
        return VisualSettings.antiBlindnessEnabled()
                && VisualSettings.antiBlindnessNausea();
    }
}
