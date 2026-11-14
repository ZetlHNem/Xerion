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
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ThrowablePotionItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class AutoSortService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static boolean sortRequested;

    private boolean keyWasDown;
    private int delayTicks;
    private boolean delayedSorting;

    public static void requestSort() {
        sortRequested = true;
    }

    public void tick() {
        boolean keyDown = ForgeModEventBridge.isAutoSortKeyDown();
        boolean keyPressed = keyDown && !keyWasDown;
        keyWasDown = keyDown;

        if (!VisualSettings.autoSortEnabled()) {
            sortRequested = false;
            return;
        }

        LocalPlayer player = MC.player;
        if (player == null || MC.gameMode == null || player.isSpectator()) {
            sortRequested = false;
            delayedSorting = false;
            return;
        }
        if (MC.screen != null && !(MC.screen instanceof InventoryScreen)
                && !(MC.screen instanceof CreativeModeInventoryScreen)) {
            return;
        }
        if (player.containerMenu != player.inventoryMenu && !(MC.screen instanceof CreativeModeInventoryScreen)) {
            return;
        }
        if (!player.inventoryMenu.getCarried().isEmpty()) {
            return;
        }

        if (keyPressed) {
            requestSort();
        }
        if (delayedSorting) {
            tickDelayed(player);
            return;
        }
        if (!sortRequested) {
            return;
        }
        sortRequested = false;
        if (PlayerItemRules.delayedSortAction(VisualSettings.autoSortAction())) {
            delayedSorting = true;
            delayTicks = 0;
            tickDelayed(player);
        } else {
            sortNow(player);
        }
    }

    private void tickDelayed(LocalPlayer player) {
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }
        if (!sortStep(player)) {
            delayedSorting = false;
            return;
        }
        delayTicks = PlayerItemRules.roundedPositiveDelayTicks(VisualSettings.autoSortDelay());
    }

    private void sortNow(LocalPlayer player) {
        int guard = PlayerItemRules.autoSortGuardLimit();
        while (PlayerItemRules.autoSortShouldContinue(guard--, player.inventoryMenu.getCarried().isEmpty())) {
            if (!sortStep(player)) {
                return;
            }
        }
    }

    private boolean sortStep(LocalPlayer player) {
        int start = PlayerItemRules.autoSortStartSlot(includeHotbar());
        int end = PlayerItemRules.autoSortEndSlot();
        Move merge = findMergeMove(player, start, end);
        if (merge != null) {
            swapNow(player, merge.from(), merge.to());
            return true;
        }
        Move sort = findSortMove(player, start, end);
        if (sort == null) {
            return false;
        }
        swapNow(player, sort.from(), sort.to());
        return true;
    }

    private static void swapNow(LocalPlayer player, int from, int to) {
        click(player, InventorySlotMapping.inventoryToMenuSlot(from), 0, ClickType.PICKUP);
        click(player, InventorySlotMapping.inventoryToMenuSlot(to), 0, ClickType.PICKUP);
        click(player, InventorySlotMapping.inventoryToMenuSlot(from), 0, ClickType.PICKUP);
    }

    private static boolean includeHotbar() {
        return VisualSettings.autoSortIncludeHotbar();
    }

    private static Move findMergeMove(LocalPlayer player, int start, int end) {
        for (int to = start; to < end; to++) {
            ItemStack target = player.getInventory().getItem(to);
            if (!PlayerItemRules.autoSortMergeTarget(
                    target.isEmpty(),
                    target.getCount(),
                    target.getMaxStackSize())) {
                continue;
            }
            for (int from = to + 1; from < end; from++) {
                ItemStack source = player.getInventory().getItem(from);
                if (PlayerItemRules.autoSortMergeSource(
                        source.isEmpty(),
                        ItemStack.isSameItemSameTags(target, source))) {
                    return new Move(from, to);
                }
            }
        }
        return null;
    }

    private static Move findSortMove(LocalPlayer player, int start, int end) {
        List<SlotStack> stacks = new ArrayList<>();
        for (int slot = start; slot < end; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                stacks.add(new SlotStack(slot, stack.copy()));
            }
        }
        stacks.sort(Comparator.comparing(SlotStack::stack, AutoSortService::compareStacks));

        for (int index = 0; index < stacks.size(); index++) {
            int desiredSlot = PlayerItemRules.autoSortDesiredSlot(start, index);
            SlotStack desired = stacks.get(index);
            if (desired.slot() != desiredSlot) {
                return new Move(desired.slot(), desiredSlot);
            }
        }
        return null;
    }

    private static int compareStacks(ItemStack left, ItemStack right) {
        return PlayerItemRules.compareSortedStacks(
                VisualSettings.autoSortMode(),
                category(left),
                id(left),
                left.getCount(),
                category(right),
                id(right),
                right.getCount()
        );
    }

    private static int category(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem || item == Items.TRIDENT) {
            return 0;
        }
        if (item instanceof DiggerItem) {
            return 1;
        }
        if (item instanceof BowItem || item instanceof CrossbowItem || item == Items.ARROW || item == Items.SPECTRAL_ARROW) {
            return 2;
        }
        if (item instanceof ArmorItem || item == Items.SHIELD || item == Items.TOTEM_OF_UNDYING) {
            return 3;
        }
        FoodProperties food = item.getFoodProperties();
        if (food != null) {
            return 4;
        }
        if (item instanceof PotionItem || item instanceof ThrowablePotionItem || item == Items.ENDER_PEARL) {
            return 5;
        }
        if (item instanceof BlockItem) {
            return 6;
        }
        return 7;
    }

    private static String id(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().toLowerCase(Locale.ROOT);
    }

    private static void click(LocalPlayer player, int slot, int button, ClickType type) {
        if (MC.gameMode == null) {
            return;
        }
        MC.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId,
                slot, button, type, player);
    }

    private record Move(int from, int to) {
    }

    private record SlotStack(int slot, ItemStack stack) {
    }

}

