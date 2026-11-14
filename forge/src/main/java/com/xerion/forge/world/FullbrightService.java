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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class FullbrightService {
    private static final int NIGHT_VISION_DURATION = MobEffectInstance.INFINITE_DURATION;
    private static boolean appliedNightVision;

    private FullbrightService() {
    }

    public static void tick() {
        if (!VisualSettings.fullbrightEnabled() || !"Night Vision".equals(VisualSettings.fullbrightMode())) {
            reset();
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            appliedNightVision = false;
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current == null || !current.isInfiniteDuration()) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, false, false, false));
            appliedNightVision = true;
        }
    }

    public static void reset() {
        LocalPlayer player = Minecraft.getInstance().player;
        MobEffectInstance current = player == null ? null : player.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && appliedNightVision && current.getAmplifier() == 0
                && current.isInfiniteDuration() && !current.isVisible()) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        appliedNightVision = false;
    }
}
