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
import java.util.function.Consumer;

final class SelectPopup {
    final String id;
    final int x;
    final int y;
    final int w;
    final String[] options;
    final String selected;
    final HotkeyApplier applier;

    SelectPopup(String id, int x, int y, int w, String[] options, String selected) {
        this(id, x, y, w, options, selected, null);
    }

    SelectPopup(String id, int x, int y, int w, String[] options, String selected, HotkeyApplier applier) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.options = options;
        this.selected = selected;
        this.applier = applier;
    }
}

final class MultiSelectPopup {
    final String id;
    final int x;
    final int y;
    final int w;
    final String[] options;
    final int selectedMask;

    MultiSelectPopup(String id, int x, int y, int w, String[] options, int selectedMask) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.options = options;
        this.selectedMask = selectedMask;
    }
}

final class SettingsPopup {
    final String id;
    final int x;
    final int y;
    final int w;
    final WidgetSurface.Frame frame;
    final WidgetSurface.PopupContent content;

    SettingsPopup(String id, int x, int y, int w, WidgetSurface.Frame frame, WidgetSurface.PopupContent content) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.frame = frame;
        this.content = content;
    }
}

final class InteractionClipState {
    final boolean enabled;
    final int x;
    final int y;
    final int w;
    final int h;

    InteractionClipState(boolean enabled, int x, int y, int w, int h) {
        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}

final class ColorPopup {
    final String id;
    final int x;
    final int y;
    final int w;
    final int buttonX;
    final int buttonY;
    final Consumer<Integer> applier;
    final boolean activeAlpha;

    ColorPopup(String id, int x, int y, int w, int buttonX, int buttonY,
               Consumer<Integer> applier, boolean activeAlpha) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.w = w;
        this.buttonX = buttonX;
        this.buttonY = buttonY;
        this.applier = applier;
        this.activeAlpha = activeAlpha;
    }
}

final class HotkeyPopup {
    final String id;
    final int x;
    final int y;

    HotkeyPopup(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}

final class HotkeyDetailPopup {
    final String controlId;
    final int bindingId;
    final int x;
    final int y;

    HotkeyDetailPopup(String controlId, int bindingId, int x, int y) {
        this.controlId = controlId;
        this.bindingId = bindingId;
        this.x = x;
        this.y = y;
    }
}

final class HotkeyValueSelectPopup {
    final String controlId;
    final int bindingId;
    final int x;
    final int y;
    final int w;
    final String[] options;

    HotkeyValueSelectPopup(String controlId, int bindingId, int x, int y, int w, String[] options) {
        this.controlId = controlId;
        this.bindingId = bindingId;
        this.x = x;
        this.y = y;
        this.w = w;
        this.options = options;
    }
}

final class HotkeyValueMultiPopup {
    final String controlId;
    final int bindingId;
    final int x;
    final int y;
    final int w;
    final String[] options;

    HotkeyValueMultiPopup(String controlId, int bindingId, int x, int y, int w, String[] options) {
        this.controlId = controlId;
        this.bindingId = bindingId;
        this.x = x;
        this.y = y;
        this.w = w;
        this.options = options;
    }
}

final class HotkeyValueColorPopup {
    final String controlId;
    final int bindingId;
    final int x;
    final int y;
    final int w;

    HotkeyValueColorPopup(String controlId, int bindingId, int x, int y, int w) {
        this.controlId = controlId;
        this.bindingId = bindingId;
        this.x = x;
        this.y = y;
        this.w = w;
    }
}

final class MenuPopup {
    final String id;
    final int x;
    final int y;
    final List<MenuItem> items;

    MenuPopup(String id, int x, int y, List<MenuItem> items) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.items = items;
    }
}

final class MenuItem {
    final String label;
    final int icon;
    final boolean danger;
    final Runnable action;

    MenuItem(String label, int icon, boolean danger, Runnable action) {
        this.label = label;
        this.icon = icon;
        this.danger = danger;
        this.action = action;
    }
}

final class MenuBuilder {
    final List<MenuItem> items = new ArrayList<>();

    void item(String label, int icon, Runnable action) {
        items.add(new MenuItem(label, icon, false, action));
    }

    void danger(String label, int icon, Runnable action) {
        items.add(new MenuItem(label, icon, true, action));
    }
}

final class PopupAnimation {
    final String id;
    final float alpha;

    PopupAnimation(String id, float alpha) {
        this.id = id;
        this.alpha = alpha;
    }
}

final class ControlTransition {
    final int y;
    final float alpha;

    ControlTransition(int y, float alpha) {
        this.y = y;
        this.alpha = alpha;
    }
}

final class ConditionalRenderState {
    final float alpha;
    final boolean visible;
    final boolean animating;
    final int depth;
    final boolean interactionDisabled;

    ConditionalRenderState(float alpha, boolean visible, boolean animating, int depth,
                           boolean interactionDisabled) {
        this.alpha = alpha;
        this.visible = visible;
        this.animating = animating;
        this.depth = depth;
        this.interactionDisabled = interactionDisabled;
    }
}

final class RenderClipState {
    final boolean enabled;
    final int x;
    final int y;
    final int w;
    final int h;

    RenderClipState(boolean enabled, int x, int y, int w, int h) {
        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}

final class ControlVisibilityTransition {
    final int reservedHeight;
    final int offsetY;
    final float alpha;

    ControlVisibilityTransition(int reservedHeight, int offsetY, float alpha) {
        this.reservedHeight = reservedHeight;
        this.offsetY = offsetY;
        this.alpha = alpha;
    }

    static ControlVisibilityTransition staticState(boolean visible, int height) {
        return new ControlVisibilityTransition(visible ? height : 0, 0, visible ? 1.0f : 0.0f);
    }
}
