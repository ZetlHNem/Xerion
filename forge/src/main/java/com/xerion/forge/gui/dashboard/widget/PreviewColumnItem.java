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

record PreviewColumnItem(PreviewTextItem text, EspItem equipment) {
    static PreviewColumnItem text(PreviewTextItem text) {
        return new PreviewColumnItem(text, null);
    }

    static PreviewColumnItem equipment(EspItem equipment) {
        return new PreviewColumnItem(null, equipment);
    }
}
