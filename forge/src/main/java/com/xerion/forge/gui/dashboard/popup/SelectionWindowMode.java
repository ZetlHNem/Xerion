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

enum SelectionWindowMode {
    ORE("Ore List"),
    XRAY("X-Ray Blocks"),
    NUKER("Nuker Blocks"),
    INVENTORY_CLEANER("Inventory Cleaner");

    private final String title;

    SelectionWindowMode(String title) {
        this.title = title;
    }

    String title() {
        return title;
    }
}
