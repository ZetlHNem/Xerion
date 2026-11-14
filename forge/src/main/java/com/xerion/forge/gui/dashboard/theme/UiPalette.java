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

import com.xerion.common.config.UiSettings;

final class UiPalette {
    static int shell() {
        return dark() ? DarkThemePalette.SHELL : LightThemePalette.SHELL;
    }

    static int shellBorder() {
        return dark() ? DarkThemePalette.SHELL_BORDER : LightThemePalette.SHELL_BORDER;
    }

    static int sidebar() {
        return dark() ? DarkThemePalette.SIDEBAR : LightThemePalette.SIDEBAR;
    }

    static int surface() {
        return dark() ? DarkThemePalette.SURFACE : LightThemePalette.SURFACE;
    }

    static int field() {
        return dark() ? DarkThemePalette.FIELD : LightThemePalette.FIELD;
    }

    static int track() {
        return dark() ? DarkThemePalette.TRACK : LightThemePalette.TRACK;
    }

    static int border() {
        return dark() ? DarkThemePalette.BORDER : LightThemePalette.BORDER;
    }

    static int text() {
        return dark() ? DarkThemePalette.TEXT : LightThemePalette.TEXT;
    }

    static int inputText() {
        return dark() ? DarkThemePalette.INPUT_TEXT : LightThemePalette.INPUT_TEXT;
    }

    static int muted() {
        return dark() ? DarkThemePalette.MUTED : LightThemePalette.MUTED;
    }

    static int subtle() {
        return dark() ? DarkThemePalette.SUBTLE : LightThemePalette.SUBTLE;
    }

    static int accent() {
        return dark() ? DarkThemePalette.ACCENT : LightThemePalette.ACCENT;
    }

    static int accentDark() {
        return dark() ? DarkThemePalette.ACCENT_DARK : LightThemePalette.ACCENT_DARK;
    }

    static int accentSoft() {
        return dark() ? DarkThemePalette.ACCENT_SOFT : LightThemePalette.ACCENT_SOFT;
    }

    static int danger() {
        return dark() ? DarkThemePalette.DANGER : LightThemePalette.DANGER;
    }

    static int statusGreen() {
        return dark() ? DarkThemePalette.STATUS_GREEN : LightThemePalette.STATUS_GREEN;
    }

    static int statusRed() {
        return dark() ? DarkThemePalette.STATUS_RED : LightThemePalette.STATUS_RED;
    }

    static int statusOrange() {
        return dark() ? DarkThemePalette.STATUS_ORANGE : LightThemePalette.STATUS_ORANGE;
    }

    static int previewBg() {
        return dark() ? DarkThemePalette.PREVIEW_BG : LightThemePalette.PREVIEW_BG;
    }

    static int previewBorder() {
        return dark() ? DarkThemePalette.PREVIEW_BORDER : LightThemePalette.PREVIEW_BORDER;
    }

    static boolean dark() {
        return UiSettings.darkTheme();
    }

    private UiPalette() {
    }
}
