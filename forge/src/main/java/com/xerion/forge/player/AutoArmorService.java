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
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class AutoArmorService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final EquipmentSlot[] ARMOR_ORDER = {
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.HEAD
    };
    private static final double MIN_UPGRADE_SCORE = 0.05D;

    private int cooldownTicks;

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
        if (!CombatSettings.autoArmorEnabled() || cooldownTicks > 0) {
            return;
        }

        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || player.isSpectator()) {
            return;
        }
        if (MC.screen instanceof AbstractContainerScreen<?>) {
            return;
        }

        SwapPlan plan = bestSwap(player);
        if (plan == null) {
            return;
        }

        swapArmor(player, plan);
        cooldownTicks = PlayerItemRules.positiveDelayTicks(CombatSettings.autoArmorDelayTicks());
    }

    private static SwapPlan bestSwap(LocalPlayer player) {
        SwapPlan best = null;
        double bestGain = MIN_UPGRADE_SCORE;
        for (EquipmentSlot armorSlot : ARMOR_ORDER) {
            ItemStack current = player.getItemBySlot(armorSlot);
            if (!CombatSettings.autoArmorAllowBindingCurse() && hasBindingCurse(current)) {
                continue;
            }
            double currentScore = armorScore(current);
            for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
                ItemStack candidate = player.getInventory().getItem(inventorySlot);
                if (!matchesSlot(candidate, armorSlot)
                        || (!CombatSettings.autoArmorAllowBindingCurse() && hasBindingCurse(candidate))) {
                    continue;
                }
                double score = armorScore(candidate);
                if (PlayerItemRules.isArmorUpgrade(score, currentScore, bestGain)) {
                    bestGain = score - currentScore;
                    best = new SwapPlan(inventorySlot, InventorySlotMapping.armorMenuSlot(armorSlot));
                }
            }
        }
        return best;
    }

    private static boolean matchesSlot(ItemStack stack, EquipmentSlot slot) {
        return stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == slot;
    }

    private static double armorScore(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armorItem)) {
            return 0.0D;
        }
        return PlayerItemRules.armorScore(
                armorItem.getDefense(),
                armorItem.getToughness(),
                stack.getEnchantmentTags().size(),
                CombatSettings.autoArmorPreferDurability(),
                stack.isDamageableItem(),
                stack.getMaxDamage(),
                stack.getDamageValue()
        );
    }

    private static boolean hasBindingCurse(ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BINDING_CURSE, stack) > 0;
    }

    private static void swapArmor(LocalPlayer player, SwapPlan plan) {
        int containerId = player.inventoryMenu.containerId;
        int sourceSlot = InventorySlotMapping.inventoryToMenuSlot(plan.inventorySlot());
        MC.gameMode.handleInventoryMouseClick(containerId, sourceSlot, 0, ClickType.PICKUP, player);
        MC.gameMode.handleInventoryMouseClick(containerId, plan.armorMenuSlot(), 0, ClickType.PICKUP, player);
        if (!player.inventoryMenu.getCarried().isEmpty()) {
            MC.gameMode.handleInventoryMouseClick(containerId, sourceSlot, 0, ClickType.PICKUP, player);
        }
    }

    private record SwapPlan(int inventorySlot, int armorMenuSlot) {
    }
}
