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
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoToolService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static int previousSlot = -1;
    private static int silentServerSlot = -1;

    public void tick() {
        LocalPlayer player = MC.player;
        if (!VisualSettings.autoToolEnabled()
                || player == null
                || MC.level == null
                || MC.gameMode == null
                || MC.getConnection() == null
                || !MC.options.keyAttack.isDown()
                || !(MC.hitResult instanceof BlockHitResult blockHit)
                || blockHit.getType() != HitResult.Type.BLOCK) {
            reset();
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = MC.level.getBlockState(pos);
        if (state.isAir()) {
            reset();
            return;
        }

        int selected = player.getInventory().selected;
        int bestSlot = bestToolSlot(player, state);
        if (bestSlot < 0 || bestSlot == selected && !VisualSettings.autoToolSilent()) {
            reset();
            return;
        }

        if (previousSlot < 0) {
            previousSlot = selected;
        }
        if (VisualSettings.autoToolSilent()) {
            if (silentServerSlot != bestSlot) {
                HotbarSlotActions.spoofServerSlot(bestSlot);
                silentServerSlot = bestSlot;
            }
            return;
        }

        if (selected != bestSlot) {
            HotbarSlotActions.select(player, bestSlot);
        }
    }

    public static void reset() {
        LocalPlayer player = MC.player;
        if (player != null && MC.getConnection() != null) {
            if (silentServerSlot >= 0) {
                HotbarSlotActions.spoofServerSlot(player.getInventory().selected);
            } else if (previousSlot >= 0
                    && "Preserve Slot".equals(VisualSettings.autoToolMode())
                    && player.getInventory().selected != previousSlot) {
                HotbarSlotActions.select(player, previousSlot);
            }
        }
        previousSlot = -1;
        silentServerSlot = -1;
    }

    private static int bestToolSlot(LocalPlayer player, BlockState state) {
        int bestSlot = -1;
        double bestScore = score(player.getMainHandItem(), state);
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            double score = score(stack, state);
            if (PlayerItemRules.betterToolCandidate(
                    score,
                    bestScore,
                    betterDurability(stack, bestSlot < 0 ? ItemStack.EMPTY : player.getInventory().getItem(bestSlot)))) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        return PlayerItemRules.selectedToolSlot(
                bestScore,
                score(player.getMainHandItem(), state),
                bestSlot,
                player.getInventory().selected);
    }

    private static double score(ItemStack stack, BlockState state) {
        boolean empty = stack.isEmpty();
        return PlayerItemRules.toolScore(
                empty,
                empty ? 0.0D : stack.getDestroySpeed(state),
                empty ? 0 : EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack),
                !empty && stack.isCorrectToolForDrops(state),
                VisualSettings.autoToolPreferDurability(),
                stack.isDamageableItem(),
                stack.getMaxDamage(),
                stack.getDamageValue()
        );
    }

    private static boolean betterDurability(ItemStack candidate, ItemStack current) {
        return PlayerItemRules.betterDurability(
                VisualSettings.autoToolPreferDurability(),
                candidate.isDamageableItem(),
                candidate.getMaxDamage(),
                candidate.getDamageValue(),
                current.isDamageableItem(),
                current.getMaxDamage(),
                current.getDamageValue()
        );
    }
}
