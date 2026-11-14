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

enum PreviewModelMode {
    ENEMIES("Enemies", FontAwesomeIcons.ICON_FA_CROSSHAIRS),
    TEAM("Team", FontAwesomeIcons.ICON_FA_USERS),
    LOCAL("Local", FontAwesomeIcons.ICON_FA_USER);

    private final String label;
    private final int icon;

    PreviewModelMode(String label, int icon) {
        this.label = label;
        this.icon = icon;
    }

    String label() {
        return label;
    }

    int icon() {
        return icon;
    }
}
