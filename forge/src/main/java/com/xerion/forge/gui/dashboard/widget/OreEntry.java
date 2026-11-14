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

import com.xerion.forge.config.VisualSettings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

record OreEntry(String label, int mask, ItemStack stack, String id, boolean modded) implements SelectionWindowEntry {
    static OreEntry vanilla(Block block) {
        String id = ForgeRegistries.BLOCKS.getKey(block) == null ? "" : ForgeRegistries.BLOCKS.getKey(block).toString();
        String label = SelectionEntryIds.oreLabelFromId(id);
        return new OreEntry(label, 0, new ItemStack(block), id, false);
    }

    public boolean selected() {
        if (mask != 0) {
            return VisualSettings.oreGlowOreEnabled(mask);
        }
        return VisualSettings.oreGlowModOreEnabled(id);
    }
}
