package com.xerion.forge.gui.dashboard;

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

import com.xerion.forge.compat.ModCompatibility;
import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class SelectionCatalog {
    private static final OreEntry[] VANILLA_ORE_ENTRIES = {
            OreEntry.vanilla(Blocks.DIAMOND_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_DIAMOND_ORE),
            OreEntry.vanilla(Blocks.EMERALD_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_EMERALD_ORE),
            OreEntry.vanilla(Blocks.GOLD_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_GOLD_ORE),
            OreEntry.vanilla(Blocks.NETHER_GOLD_ORE),
            OreEntry.vanilla(Blocks.IRON_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_IRON_ORE),
            OreEntry.vanilla(Blocks.REDSTONE_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_REDSTONE_ORE),
            OreEntry.vanilla(Blocks.LAPIS_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_LAPIS_ORE),
            OreEntry.vanilla(Blocks.COAL_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_COAL_ORE),
            OreEntry.vanilla(Blocks.COPPER_ORE),
            OreEntry.vanilla(Blocks.DEEPSLATE_COPPER_ORE),
            OreEntry.vanilla(Blocks.NETHER_QUARTZ_ORE),
            OreEntry.vanilla(Blocks.ANCIENT_DEBRIS)
    };

    private List<OreEntry> cachedOreEntries = List.of();
    private int cachedOreEntryCount = -1;
    private List<SelectionWindowEntry> cachedXrayEntries = List.of();
    private int cachedXrayEntryCount = -1;
    private List<SelectionWindowEntry> cachedInventoryCleanerEntries = List.of();
    private int cachedInventoryCleanerEntryCount = -1;

    void invalidate(SelectionWindowMode mode) {
        switch (mode) {
            case ORE -> {
                cachedOreEntries = List.of();
                cachedOreEntryCount = -1;
            }
            case XRAY, NUKER -> {
                cachedXrayEntries = List.of();
                cachedXrayEntryCount = -1;
            }
            case INVENTORY_CLEANER -> {
                cachedInventoryCleanerEntries = List.of();
                cachedInventoryCleanerEntryCount = -1;
            }
        }
    }

    List<SelectionWindowEntry> filteredEntries(SelectionWindowMode mode, String searchText) {
        List<? extends SelectionWindowEntry> source = switch (mode) {
            case XRAY, NUKER -> xrayEntries();
            case INVENTORY_CLEANER -> inventoryCleanerEntries();
            default -> oreEntries();
        };
        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        if (query.isBlank()) {
            return new ArrayList<>(source);
        }
        List<SelectionWindowEntry> filtered = new ArrayList<>();
        for (SelectionWindowEntry entry : source) {
            if (entry.label().toLowerCase(Locale.ROOT).contains(query)
                    || entry.id().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    boolean selected(SelectionWindowMode mode, SelectionWindowEntry entry) {
        if (mode == SelectionWindowMode.NUKER) {
            return VisualSettings.nukerBlockEnabled(entry.id());
        }
        if (mode == SelectionWindowMode.INVENTORY_CLEANER) {
            return SelectionEntryIds.inventoryCleanerTrashSet().contains(entry.id());
        }
        return entry.selected();
    }

    void toggle(SelectionWindowMode mode, SelectionWindowEntry entry, boolean selected) {
        if (entry instanceof OreEntry ore) {
            toggleOreEntry(ore, selected);
        } else if (mode == SelectionWindowMode.NUKER) {
            toggleNukerEntry(entry, selected);
        } else if (mode == SelectionWindowMode.INVENTORY_CLEANER) {
            toggleInventoryCleanerEntry(entry, selected);
        } else {
            toggleXrayEntry(entry, selected);
        }
    }

    private List<OreEntry> oreEntries() {
        int registryCount = ForgeRegistries.BLOCKS.getValues().size();
        if (cachedOreEntryCount == registryCount && !cachedOreEntries.isEmpty()) {
            return cachedOreEntries;
        }
        List<OreEntry> entries = new ArrayList<>();
        entries.addAll(List.of(VANILLA_ORE_ENTRIES));
        Set<String> vanillaIds = new HashSet<>();
        for (OreEntry entry : VANILLA_ORE_ENTRIES) {
            vanillaIds.add(entry.id());
        }
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            String id = ForgeRegistries.BLOCKS.getKey(block) == null ? "" : ForgeRegistries.BLOCKS.getKey(block).toString();
            if (id.isBlank() || vanillaIds.contains(id)) {
                continue;
            }
            ModCompatibility.OreInfo oreInfo = ModCompatibility.ore(block.defaultBlockState());
            if (oreInfo == null) {
                continue;
            }
            ItemStack stack = new ItemStack(block);
            if (stack.isEmpty()) {
                continue;
            }
            String label = SelectionEntryIds.oreLabelFromId(id);
            entries.add(new OreEntry(label, 0, stack, id, true));
        }
        cachedOreEntryCount = registryCount;
        cachedOreEntries = List.copyOf(entries);
        return cachedOreEntries;
    }

    private List<SelectionWindowEntry> xrayEntries() {
        int registryCount = ForgeRegistries.BLOCKS.getValues().size();
        if (cachedXrayEntryCount == registryCount && !cachedXrayEntries.isEmpty()) {
            return cachedXrayEntries;
        }
        List<SelectionWindowEntry> entries = new ArrayList<>();
        Set<Block> orderedBlocks = new LinkedHashSet<>();
        addCreativeTabBlocks(orderedBlocks);
        orderedBlocks.addAll(ForgeRegistries.BLOCKS.getValues());
        for (Block block : orderedBlocks) {
            addXrayEntry(entries, block);
        }
        entries.sort(Comparator.comparingInt((SelectionWindowEntry entry) -> SelectionEntryRanker.xrayEntryRank(entry.id())));
        cachedXrayEntryCount = registryCount;
        cachedXrayEntries = List.copyOf(entries);
        return cachedXrayEntries;
    }

    private List<SelectionWindowEntry> inventoryCleanerEntries() {
        int registryCount = ForgeRegistries.ITEMS.getValues().size();
        if (cachedInventoryCleanerEntryCount == registryCount && !cachedInventoryCleanerEntries.isEmpty()) {
            return cachedInventoryCleanerEntries;
        }
        List<SelectionWindowEntry> entries = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            String id = ForgeRegistries.ITEMS.getKey(item) == null ? "" : ForgeRegistries.ITEMS.getKey(item).toString();
            if (id.isBlank()) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) {
                continue;
            }
            String label = SelectionEntryIds.itemLabelFromId(id);
            entries.add(new InventoryCleanerEntry(label, stack, id));
        }
        entries.sort(Comparator
                .comparingInt((SelectionWindowEntry entry) -> SelectionEntryRanker.inventoryCleanerTrashRank(
                        entry.id(),
                        entry.label()
                ))
                .thenComparing(entry -> entry.label().toLowerCase(Locale.ROOT))
                .thenComparing(SelectionWindowEntry::id));
        cachedInventoryCleanerEntryCount = registryCount;
        cachedInventoryCleanerEntries = List.copyOf(entries);
        return cachedInventoryCleanerEntries;
    }

    private void addCreativeTabBlocks(Set<Block> blocks) {
        rebuildCreativeTabs();
        addCreativeTabBlocks(blocks, CreativeModeTabs.NATURAL_BLOCKS);
        addCreativeTabBlocks(blocks, CreativeModeTabs.BUILDING_BLOCKS);
        addCreativeTabBlocks(blocks, CreativeModeTabs.COLORED_BLOCKS);
        addCreativeTabBlocks(blocks, CreativeModeTabs.FUNCTIONAL_BLOCKS);
        addCreativeTabBlocks(blocks, CreativeModeTabs.REDSTONE_BLOCKS);
        for (CreativeModeTab tab : CreativeModeTabs.tabs()) {
            for (ItemStack stack : tab.getDisplayItems()) {
                Block block = Block.byItem(stack.getItem());
                if (block != Blocks.AIR) {
                    blocks.add(block);
                }
            }
        }
    }

    private void rebuildCreativeTabs() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.connection == null) {
            return;
        }
        try {
            CreativeModeTabs.tryRebuildTabContents(
                    minecraft.player.connection.enabledFeatures(),
                    minecraft.player.hasPermissions(2),
                    minecraft.player.connection.registryAccess());
        } catch (RuntimeException ignored) {
        }
    }

    private void addCreativeTabBlocks(Set<Block> blocks, net.minecraft.resources.ResourceKey<CreativeModeTab> key) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(key);
        if (tab == null) {
            return;
        }
        for (ItemStack stack : tab.getDisplayItems()) {
            Block block = Block.byItem(stack.getItem());
            if (block != Blocks.AIR) {
                blocks.add(block);
            }
        }
    }

    private void addXrayEntry(List<SelectionWindowEntry> entries, Block block) {
        if (block == Blocks.AIR) {
            return;
        }
        String id = ForgeRegistries.BLOCKS.getKey(block) == null ? "" : ForgeRegistries.BLOCKS.getKey(block).toString();
        if (id.isBlank()) {
            return;
        }
        ItemStack stack = new ItemStack(block);
        if (stack.isEmpty()) {
            return;
        }
        String label = SelectionEntryIds.blockLabelFromId(id);
        entries.add(new XrayEntry(label, stack, id));
    }

    private void toggleOreEntry(OreEntry ore, boolean selected) {
        Set<String> selectedIds = new HashSet<>();
        String raw = VisualSettings.oreGlowModOreIds();
        if (!raw.isBlank()) {
            for (String part : raw.split("\\|")) {
                String id = SelectionEntryIds.normalize(part);
                if (!id.isBlank()) {
                    selectedIds.add(id);
                }
            }
        }
        int selectedMask = VisualSettings.oreGlowOreMask();
        if (selected) {
            if (ore.mask() != 0) {
                selectedMask |= ore.mask();
            } else if (!ore.id().isBlank()) {
                selectedIds.add(SelectionEntryIds.normalize(ore.id()));
            }
        } else {
            if (ore.mask() != 0) {
                selectedMask &= ~ore.mask();
            } else {
                selectedIds.remove(SelectionEntryIds.normalize(ore.id()));
            }
        }
        VisualSettings.setOreGlowModOreIds(SelectionEntryIds.join(selectedIds));
        VisualSettings.setOreGlowOreMask(selectedMask);
    }

    private void toggleXrayEntry(SelectionWindowEntry entry, boolean selected) {
        Set<String> selectedIds = new HashSet<>();
        String raw = VisualSettings.xrayBlockIds();
        if (!raw.isBlank()) {
            for (String part : raw.split("\\|")) {
                String id = SelectionEntryIds.normalize(part);
                if (!id.isBlank()) {
                    selectedIds.add(id);
                }
            }
        }
        if (selected) {
            selectedIds.add(SelectionEntryIds.normalize(entry.id()));
        } else {
            selectedIds.remove(SelectionEntryIds.normalize(entry.id()));
        }
        VisualSettings.setXrayBlockIds(SelectionEntryIds.join(selectedIds));
    }

    private void toggleInventoryCleanerEntry(SelectionWindowEntry entry, boolean selected) {
        Set<String> selectedIds = SelectionEntryIds.inventoryCleanerTrashSet();
        if (selected) {
            selectedIds.add(entry.id());
        } else {
            selectedIds.remove(entry.id());
        }
        VisualSettings.setInventoryCleanerTrashItems(selectedIds.isEmpty() ? "" : String.join("|", selectedIds));
    }

    private void toggleNukerEntry(SelectionWindowEntry entry, boolean selected) {
        Set<String> selectedIds = new HashSet<>();
        String raw = VisualSettings.nukerBlockIds();
        if (!raw.isBlank()) {
            for (String part : raw.split("\\|")) {
                if (!part.isBlank()) {
                    selectedIds.add(part);
                }
            }
        }
        if (selected) {
            selectedIds.add(entry.id());
        } else {
            selectedIds.remove(entry.id());
        }
        VisualSettings.setNukerBlockIds(selectedIds.isEmpty() ? "" : String.join("|", selectedIds));
    }
}
