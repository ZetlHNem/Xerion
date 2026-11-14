package com.xerion.forge.gui.font;

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

public enum FontAwesomeIcon {
    USER(0xF007),
    GEAR(0xF013),
    ROTATE_RIGHT(0xF01E),
    LIST(0xF03A),
    EYE(0xF06E),
    GAMEPAD(0xF11B);

    private final int codepoint;

    FontAwesomeIcon(int codepoint) {
        this.codepoint = codepoint;
    }

    public int codepoint() {
        return codepoint;
    }
}
