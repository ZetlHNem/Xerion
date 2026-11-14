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

import com.xerion.forge.gui.font.FontAwesomeIcons;

enum SidebarTab {
    COMBAT("Combat", FontAwesomeIcons.ICON_FA_SHIELD, false),
    RAGEBOT("Ragebot", FontAwesomeIcons.ICON_FA_CROSSHAIRS, false),
    BYPASS("Compatibility", FontAwesomeIcons.ICON_FA_FINGERPRINT, false),
    WHITELIST("Whitelist", FontAwesomeIcons.ICON_FA_USER_SHIELD, false),
    MOVEMENT("Movement", FontAwesomeIcons.ICON_FA_PERSON_RUNNING, false),
    VISUAL("Visual", FontAwesomeIcons.ICON_FA_EYE, false),
    WORLD("World", FontAwesomeIcons.ICON_FA_CUBES, false),
    PLAYER("Player", FontAwesomeIcons.ICON_FA_USER, false),
    MISC("Misc", FontAwesomeIcons.ICON_FA_WRENCH, false),
    MUSIC("Music", FontAwesomeIcons.ICON_FA_MUSIC, false),
    USER("User", FontAwesomeIcons.ICON_FA_ADDRESS_CARD, true),
    CONFIG("Config", FontAwesomeIcons.ICON_FA_FLOPPY_DISK, true);

    final String title;
    final int icon;
    final boolean bottomAligned;

    SidebarTab(String title, int icon, boolean bottomAligned) {
        this.title = title;
        this.icon = icon;
        this.bottomAligned = bottomAligned;
    }
}
