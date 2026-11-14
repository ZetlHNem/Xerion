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
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AutoCrystalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(XerionForge.MOD_ID);
    private static final Minecraft MC = Minecraft.getInstance();
    private static final int FAIL_RETRY_COOLDOWN_TICKS = 6;
    private static final int DEBUG_COOLDOWN_TICKS = 200;

    private int placeCooldownTicks;
    private int breakCooldownTicks;
    private int debugCooldownTicks;

    public void tick() {
        if (placeCooldownTicks > 0) {
            placeCooldownTicks--;
        }
        if (breakCooldownTicks > 0) {
            breakCooldownTicks--;
        }
        if (debugCooldownTicks > 0) {
            debugCooldownTicks--;
        }
        if (!CombatSettings.autoCrystalEnabled()) {
            return;
        }

        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null || MC.screen != null || player.isSpectator()) {
            return;
        }
        boolean canBreakNow = CombatSettings.autoCrystalBreak() && breakCooldownTicks <= 0;
        boolean canPlaceNow = CombatSettings.autoCrystalPlace() && placeCooldownTicks <= 0;
        if (!canBreakNow && !canPlaceNow) {
            return;
        }

        LivingEntity target = AutoCrystalTargeting.findTarget(player, level);
        if (target == null) {
            applyFailCooldown();
            debug("no target");
            return;
        }

        if (canBreakNow && breakCrystal(player, level, target)) {
            resetBreakCooldown();
            return;
        }

        if (canPlaceNow && placeCrystal(player, level, target)) {
            resetPlaceCooldown();
        }
    }

    private boolean breakCrystal(LocalPlayer player, ClientLevel level, LivingEntity target) {
        EndCrystal crystal = AutoCrystalTargeting.findBreakCrystal(player, level, target);
        if (crystal == null) {
            return false;
        }

        AutoCrystalTargeting.rotateTo(player, crystal.position());
        MC.gameMode.attack(player, crystal);
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    private boolean placeCrystal(LocalPlayer player, ClientLevel level, LivingEntity target) {
        CrystalHand hand = findCrystalHand(player);
        if (hand == null) {
            applyFailCooldown();
            debug("no end crystal in hotbar/offhand");
            return false;
        }

        AutoCrystalTargeting.PlaceCandidate candidate = AutoCrystalTargeting.findBestPlacement(player, level, target);
        if (candidate == null) {
            applyFailCooldown();
            debug("no valid placement target=" + target.getName().getString());
            return false;
        }

        int previousSlot = player.getInventory().selected;
        if (hand.slot() >= 0 && hand.slot() != previousSlot) {
            HotbarSlotActions.select(player, hand.slot());
        }

        AutoCrystalTargeting.rotateTo(player, candidate.hit());
        BlockHitResult hit = new BlockHitResult(
                candidate.hit(),
                Direction.UP,
                candidate.pos(),
                false
        );
        InteractionResult result = MC.gameMode.useItemOn(player, hand.hand(), hit);
        if (result.consumesAction()) {
            player.swing(hand.hand());
        } else {
            debug("useItemOn failed result=" + result + " pos=" + candidate.pos());
        }

        if (hand.slot() >= 0 && previousSlot != hand.slot()) {
            HotbarSlotActions.select(player, previousSlot);
        }
        return result.consumesAction();
    }

    private static CrystalHand findCrystalHand(LocalPlayer player) {
        if (player.getOffhandItem().is(Items.END_CRYSTAL)) {
            return new CrystalHand(InteractionHand.OFF_HAND, -1);
        }
        if (player.getMainHandItem().is(Items.END_CRYSTAL)) {
            return new CrystalHand(InteractionHand.MAIN_HAND, player.getInventory().selected);
        }
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).is(Items.END_CRYSTAL)) {
                return new CrystalHand(InteractionHand.MAIN_HAND, slot);
            }
        }
        return null;
    }

    private void resetPlaceCooldown() {
        placeCooldownTicks = CombatActionRules.roundedDelayTicks(CombatSettings.autoCrystalPlaceDelay());
    }

    private void resetBreakCooldown() {
        breakCooldownTicks = CombatActionRules.roundedDelayTicks(CombatSettings.autoCrystalBreakDelay());
    }

    private void applyFailCooldown() {
        placeCooldownTicks = CombatActionRules.raiseCooldown(placeCooldownTicks, FAIL_RETRY_COOLDOWN_TICKS);
        breakCooldownTicks = CombatActionRules.raiseCooldown(breakCooldownTicks, FAIL_RETRY_COOLDOWN_TICKS);
    }

    private void debug(String reason) {
        if (debugCooldownTicks > 0) {
            return;
        }
        debugCooldownTicks = DEBUG_COOLDOWN_TICKS;
        LOGGER.info("[Xerion][AutoCrystal] {} enabled={} place={} break={} range={} minDamage={} maxSelf={} rayTrace={} antiSuicide={}",
                reason,
                CombatSettings.autoCrystalEnabled(),
                CombatSettings.autoCrystalPlace(),
                CombatSettings.autoCrystalBreak(),
                CombatSettings.autoCrystalRange(),
                CombatSettings.autoCrystalMinDamage(),
                CombatSettings.autoCrystalMaxSelfDamage(),
                CombatSettings.autoCrystalRayTrace(),
                CombatSettings.autoCrystalAntiSuicide());
    }

    private record CrystalHand(InteractionHand hand, int slot) {
    }

}
