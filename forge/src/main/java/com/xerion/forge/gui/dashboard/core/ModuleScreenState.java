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

final class ModuleScreenState {
    private ModuleCategory selectedCategory = ModuleCategory.HUD;
    private Module selectedModule;
    private boolean settingsMode;
    private boolean closeOnOutside = true;

    ModuleCategory selectedCategory() {
        return selectedCategory;
    }

    void selectCategory(ModuleCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
        this.settingsMode = false;
    }

    Module selectedModule() {
        return selectedModule;
    }

    void selectModule(Module selectedModule) {
        this.selectedModule = selectedModule;
    }

    boolean settingsMode() {
        return settingsMode;
    }

    void toggleSettingsMode() {
        this.settingsMode = !settingsMode;
    }

    boolean closeOnOutside() {
        return closeOnOutside;
    }

    void toggleCloseOnOutside() {
        closeOnOutside = !closeOnOutside;
    }
}
