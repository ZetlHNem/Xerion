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

final class PopupGeometry {
    private PopupGeometry() {
    }

    static String settingsPopupId(String id) {
        return "settings/" + id;
    }

    static String selectPopupId(String id) {
        return "select/" + id;
    }

    static String multiSelectPopupId(String id) {
        return "multi/" + id;
    }

    static String colorPopupId(String id) {
        return "color/" + id;
    }

    static String hotkeyPopupId(String id) {
        return "hotkey/" + id;
    }

    static String hotkeyDetailPopupId(String controlId, int bindingId) {
        return "hotkey-detail/" + controlId + "/" + bindingId;
    }

    static String hotkeyValueSelectPopupId(String controlId, int bindingId) {
        return "hotkey-value-select/" + controlId + "/" + bindingId;
    }

    static String hotkeyValueMultiPopupId(String controlId, int bindingId) {
        return "hotkey-value-multi/" + controlId + "/" + bindingId;
    }

    static String hotkeyValueColorPopupId(String controlId, int bindingId) {
        return "hotkey-value-color/" + controlId + "/" + bindingId;
    }

    static String menuPopupId(String id) {
        return "menu/" + id;
    }

    static String hotkeyBindingKey(String controlId, int bindingId) {
        return controlId + "#" + bindingId;
    }

    static boolean contains(double px, double py, int x, int y, int w, int h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }
}
