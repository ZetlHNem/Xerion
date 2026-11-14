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

import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

import java.util.Map;

public final class AutoWeaponService {
    private static final Minecraft MC = Minecraft.getInstance();

    private AutoWeaponService() {
    }

    public static int beforeAttack(LocalPlayer player) {
        if (!CombatSettings.autoWeaponEnabled() || player == null || MC.getConnection() == null) {
            return -1;
        }

        int selected = player.getInventory().selected;
        int bestSlot = findBestWeaponSlot(player);
        if (bestSlot < 0 || bestSlot == selected) {
            return -1;
        }

        HotbarSlotActions.select(player, bestSlot);
        return selected;
    }

    public static void afterAttack(LocalPlayer player, int previousSlot) {
        if (!CombatSettings.autoWeaponRestoreSlot() || player == null || previousSlot < 0) {
            return;
        }
        HotbarSlotActions.select(player, previousSlot);
    }

    public static boolean hasWeaponSlot(LocalPlayer player) {
        return player != null && findBestWeaponSlot(player) >= 0;
    }

    private static int findBestWeaponSlot(LocalPlayer player) {
        int bestSlot = -1;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!allowedWeapon(stack.getItem())) {
                continue;
            }
            double score = weaponScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    private static boolean allowedWeapon(Item item) {
        return CombatEquipmentRules.allowedWeapon(
                CombatSettings.autoWeaponMode(),
                CombatSettings.AUTO_WEAPON_MODE_SWORD,
                CombatSettings.AUTO_WEAPON_MODE_AXE,
                item instanceof SwordItem,
                item instanceof AxeItem
        );
    }

    private static double weaponScore(ItemStack stack) {
        return CombatEquipmentRules.weaponScore(
                attackDamage(stack),
                stack.getEnchantmentTags().size(),
                stack.isDamageableItem(),
                stack.getMaxDamage(),
                stack.getDamageValue(),
                stack.getItem() instanceof SwordItem
        );
    }

    private static double attackDamage(ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
        double damage = 1.0D;
        for (Map.Entry<Attribute, AttributeModifier> entry : modifiers.entries()) {
            if (entry.getKey() == Attributes.ATTACK_DAMAGE) {
                damage += entry.getValue().getAmount();
            }
        }
        return damage;
    }

}
