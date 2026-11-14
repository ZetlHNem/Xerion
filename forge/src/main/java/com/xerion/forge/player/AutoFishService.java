package com.xerion.forge.player;

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
import com.xerion.common.player.PlayerItemRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import com.xerion.forge.mixin.FishingHookAccessor;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;

public final class AutoFishService {
    private static final Minecraft MC = Minecraft.getInstance();
    private int hookTicks;
    private int recastCooldown;
    private boolean waitingForRecast;

    public void tick() {
        if (recastCooldown > 0) {
            recastCooldown--;
        }
        if (hookTicks > 0) {
            hookTicks++;
        }

        LocalPlayer player = MC.player;
        if (!VisualSettings.autoFishEnabled()
                || player == null
                || MC.gameMode == null
                || MC.screen != null
                || player.isSpectator()) {
            reset();
            return;
        }

        InteractionHand hand = fishingRodHand(player);
        if (hand == null) {
            reset();
            return;
        }
        if (VisualSettings.autoFishAntiBreak() && fishingRodTooDamaged(player, hand)) {
            reset();
            return;
        }

        FishingHook hook = player.fishing;
        boolean hasHook = PlayerItemRules.autoFishHasHook(hook != null, hook != null && hook.isRemoved());

        if (!hasHook) {
            hookTicks = 0;
            if (PlayerItemRules.autoFishReadyForRecast(waitingForRecast, recastCooldown)) {
                recastCooldown = PlayerItemRules.roundedPositiveDelayTicks(VisualSettings.autoFishThrowDelay());
                waitingForRecast = false;
                return;
            }
            if (PlayerItemRules.autoFishReadyForAutoCast(VisualSettings.autoFishAutoCast(), recastCooldown)) {
                MC.gameMode.useItem(player, hand);
                hookTicks = 1;
                recastCooldown = PlayerItemRules.roundedPositiveDelayTicks(VisualSettings.autoFishThrowDelay());
            }
            return;
        }

        if (hookTicks == 0) {
            hookTicks = 1;
        }

        boolean biting = hookBiting(hook);
        if (biting) {
            if (PlayerItemRules.autoFishReadyForAutoCast(VisualSettings.autoFishAutoCast(), recastCooldown)) {
                MC.gameMode.useItem(player, hand);
                hookTicks = 0;
                waitingForRecast = true;
                recastCooldown = PlayerItemRules.roundedPositiveDelayTicks(VisualSettings.autoFishCatchDelay());
            }
            return;
        }
    }

    private void reset() {
        hookTicks = 0;
        recastCooldown = 0;
        waitingForRecast = false;
    }

    private static InteractionHand fishingRodHand(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof FishingRodItem) {
            return InteractionHand.MAIN_HAND;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof FishingRodItem) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static boolean fishingRodTooDamaged(LocalPlayer player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return PlayerItemRules.fishingRodTooDamaged(stack.isDamageableItem(), stack.getMaxDamage(), stack.getDamageValue());
    }

    private static boolean hookBiting(FishingHook hook) {
        return PlayerItemRules.autoFishHookBiting(
                ((FishingHookAccessor) hook).xerion$isBiting(),
                ((FishingHookAccessor) hook).xerion$getNibble()
        );
    }
}
