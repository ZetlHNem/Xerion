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

import com.xerion.common.combat.CombatEquipmentRules;
import com.xerion.common.config.CombatSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

public final class ShieldBreakerService {
    private static final Minecraft MC = Minecraft.getInstance();

    private ShieldBreakerService() {
    }

    public static int beforeAttack(LocalPlayer player, LivingEntity target) {
        if ((!CombatSettings.shieldBreakerEnabled() && !CombatSettings.killAuraSmartWeapon())
                || player == null || target == null
                || MC.getConnection() == null || !blockingWithShield(target)) {
            return -1;
        }

        int axeSlot = findAxeSlot(player);
        int selected = player.getInventory().selected;
        if (axeSlot < 0 || axeSlot == selected) {
            return -1;
        }

        HotbarSlotActions.select(player, axeSlot);
        return selected;
    }

    public static void afterAttack(LocalPlayer player, int previousSlot) {
        if ((!CombatSettings.shieldBreakerRestoreSlot() && !CombatSettings.killAuraSmartWeapon())
                || player == null || previousSlot < 0) {
            return;
        }
        HotbarSlotActions.select(player, previousSlot);
    }

    private static boolean blockingWithShield(LivingEntity target) {
        if (!target.isBlocking()) {
            return false;
        }
        ItemStack using = target.getUseItem();
        if (using.getItem() instanceof ShieldItem) {
            return true;
        }
        return target.getOffhandItem().getItem() instanceof ShieldItem
                || target.getMainHandItem().getItem() instanceof ShieldItem;
    }

    private static int findAxeSlot(LocalPlayer player) {
        int bestSlot = -1;
        int bestScore = Integer.MIN_VALUE;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!(stack.getItem() instanceof AxeItem)) {
                continue;
            }
            int score = axeScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    private static int axeScore(ItemStack stack) {
        return CombatEquipmentRules.axeScore(
                stack.isDamageableItem(),
                stack.getMaxDamage(),
                stack.getDamageValue(),
                stack.getEnchantmentTags().size()
        );
    }

}
