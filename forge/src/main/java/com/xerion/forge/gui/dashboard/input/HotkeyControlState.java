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

import java.util.ArrayList;
import java.util.List;

final class HotkeyControlState {
    final List<HotkeyBinding> bindings = new ArrayList<>();
    int nextBindingId = 1;
    HotkeyAnchor anchor;
}
