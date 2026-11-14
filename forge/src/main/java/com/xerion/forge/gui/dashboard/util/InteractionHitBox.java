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

import com.xerion.common.module.Module;
import com.xerion.common.module.ModuleCategory;

record InteractionHitBox(Type type, ModuleCategory category, Module module, DashboardLayout.Rect bounds) {
    enum Type {
        CATEGORY,
        MODULE,
        SETTINGS_SETTINGS,
        CLOSE_OUTSIDE
    }

    boolean contains(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY);
    }
}
