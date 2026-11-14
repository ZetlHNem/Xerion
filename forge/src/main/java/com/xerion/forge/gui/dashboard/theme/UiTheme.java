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

final class UiTheme {
    static final int SHADOW = 0x66000000;
    static final int SURFACE = 0xEF141218;
    static final int SURFACE_DIM = 0xF01D1B20;
    static final int SURFACE_CONTAINER_LOW = 0xF01D1B20;
    static final int SURFACE_CONTAINER = 0xF4211F26;
    static final int SURFACE_CONTAINER_HIGH = 0xF82B2930;
    static final int SURFACE_CONTAINER_HIGHEST = 0xFC36343B;
    static final int OUTLINE = 0xB4938F99;
    static final int OUTLINE_SOFT = 0x60938F99;
    static final int PRIMARY = 0xFFD0BCFF;
    static final int ON_PRIMARY = 0xFF381E72;
    static final int PRIMARY_CONTAINER = 0xEC4F378B;
    static final int ON_PRIMARY_CONTAINER = 0xFFEADDFF;
    static final int SECONDARY_CONTAINER = 0xEC4A4458;
    static final int ON_SECONDARY_CONTAINER = 0xFFE8DEF8;
    static final int TEXT_PRIMARY = 0xFFECE6F0;
    static final int TEXT_SECONDARY = 0xFFCAC4D0;
    static final int TEXT_MUTED = 0xFF938F99;
    static final int ERROR = 0xFFF2B8B5;

    static final int PANEL_RADIUS = 17;
    static final int SECTION_RADIUS = 13;
    static final int CARD_RADIUS = 9;

    private UiTheme() {
    }

    static int rowSurface(boolean hovered) {
        return hovered ? SURFACE_CONTAINER_HIGH : SURFACE_CONTAINER;
    }
}
