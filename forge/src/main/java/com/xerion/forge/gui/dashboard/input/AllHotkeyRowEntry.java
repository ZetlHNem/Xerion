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

final class AllHotkeyRowEntry {
    final AllHotkeyDraft draft;
    final AllHotkeyRowAnimation animation;

    AllHotkeyRowEntry(AllHotkeyDraft draft, AllHotkeyRowAnimation animation) {
        this.draft = draft;
        this.animation = animation;
    }
}
