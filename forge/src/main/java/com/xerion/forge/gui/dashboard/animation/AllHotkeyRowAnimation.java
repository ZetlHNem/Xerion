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

final class AllHotkeyRowAnimation {
    AllHotkeyDraft draft;
    float y;
    float alpha;
    int targetY;
    boolean deleting;

    AllHotkeyRowAnimation(AllHotkeyDraft draft, int startY) {
        this.draft = draft;
        this.y = startY;
        this.targetY = startY;
        this.alpha = 0.0f;
    }
}
