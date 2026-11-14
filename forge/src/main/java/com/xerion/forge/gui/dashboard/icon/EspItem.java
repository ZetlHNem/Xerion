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

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

record EspItem(String label, long item, BooleanSupplier getter, Consumer<Boolean> setter, PreviewSettings settings) {
    boolean active() {
        return getter.getAsBoolean();
    }

    void toggle() {
        setter.accept(!active());
    }
}
