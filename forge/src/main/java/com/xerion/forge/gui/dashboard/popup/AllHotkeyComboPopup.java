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

final class AllHotkeyComboPopup {
    final String controlId;
    final int bindingId;
    final AllHotkeyComboKind kind;
    final int x;
    final int y;
    final int w;
    final String[] options;

    AllHotkeyComboPopup(String controlId, int bindingId, AllHotkeyComboKind kind,
                        int x, int y, int w, String[] options) {
        this.controlId = controlId;
        this.bindingId = bindingId;
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.w = w;
        this.options = options;
    }
}
