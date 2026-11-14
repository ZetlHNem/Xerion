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

record XrayEntry(String label, ItemStack stack, String id) implements SelectionWindowEntry {
    public boolean selected() {
        return VisualSettings.xrayBlockEnabled(id);
    }
}
