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

public final class HotkeyHudEntry {
    public final String value;
    public final String name;
    public final String key;

    public HotkeyHudEntry(String value, String name, String key) {
        this.value = value;
        this.name = name;
        this.key = key;
    }

    public String value() {
        return value;
    }

    public String name() {
        return name;
    }

    public String key() {
        return key;
    }
}
