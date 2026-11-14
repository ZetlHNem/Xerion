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

import com.xerion.common.config.CombatSettings;
import com.xerion.common.player.PlayerItemRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;

public final class FastUseService {
    private static final Minecraft MC = Minecraft.getInstance();

    private int cooldownTicks;
    private boolean forcedUseKey;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }

        LocalPlayer player = MC.player;
        if (!CombatSettings.fastUseEnabled()) {
            reset();
            return;
        }
        if (player == null || MC.gameMode == null || MC.getConnection() == null || MC.screen != null || player.isSpectator()) {
            reset();
            return;
        }

        if (player.isUsingItem()) {
            ItemStack useStack = player.getUseItem();
            if (!isChargeProjectile(useStack)) {
                reset();
                return;
            }
            forceUseKey();
            int charge = useStack.getUseDuration() - player.getUseItemRemainingTicks();
            if (PlayerItemRules.fastUseChargeReady(charge, CombatSettings.fastUseChargeTicks())) {
                releaseProjectile(player);
            }
            return;
        }

        releaseUseKey();
        if (cooldownTicks > 0 || !CombatSettings.fastUseAutoUse()) {
            return;
        }

        UseTarget target = findUseTarget(player);
        if (target == null) {
            return;
        }
        if (isThrowable(target.stack()) && player.getCooldowns().isOnCooldown(target.stack().getItem())) {
            return;
        }

        InteractionResult result = MC.gameMode.useItem(player, target.hand());
        if (isThrowable(target.stack())) {
            if (result.consumesAction()) {
                player.swing(target.hand());
                cooldownTicks = PlayerItemRules.nonNegativeDelayTicks(CombatSettings.fastUseDelayTicks());
            }
            return;
        }
        if (result.consumesAction() || player.isUsingItem()) {
            forceUseKey();
        }
    }

    private void releaseProjectile(LocalPlayer player) {
        releaseUseKey();
        sendBoostPackets(player);
        MC.gameMode.releaseUsingItem(player);
        cooldownTicks = PlayerItemRules.nonNegativeDelayTicks(CombatSettings.fastUseDelayTicks());
    }

    private static UseTarget findUseTarget(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isAllowed(mainHand)) {
            return new UseTarget(InteractionHand.MAIN_HAND, mainHand);
        }
        ItemStack offHand = player.getOffhandItem();
        if (isAllowed(offHand)) {
            return new UseTarget(InteractionHand.OFF_HAND, offHand);
        }
        return null;
    }

    private static boolean isAllowed(ItemStack stack) {
        return isChargeProjectile(stack) || isThrowable(stack);
    }

    private static boolean isChargeProjectile(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BowItem) {
            return CombatSettings.fastUseBow();
        }
        if (item instanceof TridentItem) {
            return CombatSettings.fastUseTrident();
        }
        if (item instanceof CrossbowItem) {
            return CombatSettings.fastUseCrossbow();
        }
        return false;
    }

    private static boolean isThrowable(ItemStack stack) {
        boolean enderPearl = stack.is(Items.ENDER_PEARL);
        boolean experienceBottle = stack.is(Items.EXPERIENCE_BOTTLE);
        if (!PlayerItemRules.fastUseThrowableAllowed(
                CombatSettings.fastUseThrowables(),
                enderPearl,
                CombatSettings.autoPearlEnabled(),
                experienceBottle,
                CombatSettings.autoExpEnabled())) {
            return false;
        }
        return stack.is(Items.SNOWBALL)
                || stack.is(Items.EGG)
                || enderPearl
                || experienceBottle
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION);
    }

    private static void sendBoostPackets(LocalPlayer player) {
        if (!CombatSettings.fastUsePacketBoost() || MC.getConnection() == null
                || BypassProtocolService.correctionRecoveryActive()) {
            return;
        }
        int packets = CombatSettings.fastUsePackets();
        for (int i = 0; i < packets; i++) {
            MC.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.onGround()
            ));
        }
    }

    private void reset() {
        releaseUseKey();
        cooldownTicks = 0;
    }

    private void forceUseKey() {
        MC.options.keyUse.setDown(true);
        forcedUseKey = true;
    }

    private void releaseUseKey() {
        if (!forcedUseKey) {
            return;
        }
        MC.options.keyUse.setDown(false);
        forcedUseKey = false;
    }

    private record UseTarget(InteractionHand hand, ItemStack stack) {
    }
}
