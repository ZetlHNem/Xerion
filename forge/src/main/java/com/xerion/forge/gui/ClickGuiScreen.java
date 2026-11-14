package com.xerion.forge.gui;

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

import com.xerion.common.XerionCore;
import com.xerion.common.module.Module;
import com.xerion.common.module.ModuleCategory;
import com.xerion.forge.gui.dashboard.ModuleDashboardScreen;
import net.minecraft.network.chat.Component;

public final class ClickGuiScreen extends ModuleDashboardScreen {
    public ClickGuiScreen(XerionCore core) {
        super(Component.literal("WidgetSurface"), core);
    }
}
