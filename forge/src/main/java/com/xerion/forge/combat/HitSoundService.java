package com.xerion.forge.combat;

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

import com.xerion.common.combat.CombatActionRules;
import com.xerion.forge.config.VisualSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

public final class HitSoundService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static int lastEntityId = Integer.MIN_VALUE;
    private static long lastGameTime = Long.MIN_VALUE;

    private HitSoundService() {
    }

    public static void clearRuntimeState() {
        lastEntityId = Integer.MIN_VALUE;
        lastGameTime = Long.MIN_VALUE;
    }

    public static void play(LivingEntity target) {
        if (!VisualSettings.hitSoundsEnabled()) {
            return;
        }
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || target == null || target == player) {
            return;
        }
        long gameTime = level.getGameTime();
        if (CombatActionRules.hitSoundAlreadyPlayed(target.getId(), gameTime, lastEntityId, lastGameTime)) {
            return;
        }
        lastEntityId = target.getId();
        lastGameTime = gameTime;

        level.playLocalSound(
                target.getX(),
                target.getY() + target.getBbHeight() * 0.5D,
                target.getZ(),
                sound(),
                SoundSource.PLAYERS,
                VisualSettings.hitSoundsVolume(),
                VisualSettings.hitSoundsPitch(),
                false
        );
    }

    private static SoundEvent sound() {
        return switch (CombatActionRules.hitSoundKey(VisualSettings.hitSoundsMode())) {
            case "pling" -> SoundEvents.NOTE_BLOCK_PLING.get();
            case "bell" -> SoundEvents.BELL_BLOCK;
            case "click" -> SoundEvents.UI_BUTTON_CLICK.get();
            case "pop" -> SoundEvents.ITEM_PICKUP;
            case "crit" -> SoundEvents.PLAYER_ATTACK_CRIT;
            default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
        };
    }
}
