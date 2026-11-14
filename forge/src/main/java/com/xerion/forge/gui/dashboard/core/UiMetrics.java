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

final class UiMetrics {
    private UiMetrics() {
    }

    static final class Screen {
        static final int WIDTH = 800;
        static final int HEIGHT = 500;
        static final int RAIL_WIDTH = 58;
        static final int TAB_SIZE = 34;
        static final int TAB_GAP = 12;
        static final int TAB_TOP_PADDING = 16;
        static final int TAB_BOTTOM_PADDING = 12;
        static final int CARD_GAP = 12;
        static final int SCROLL_STEP = 32;

        private Screen() {
        }
    }

    static final class Control {
        static final int CARD_PAD_X = 12;
        static final int CARD_CONTENT_TOP = 40;
        static final int CARD_BOTTOM_PAD = 12;
        static final int FONT_SIZE = 12;
        static final int FRAME_PADDING_X = 4;
        static final int FRAME_PADDING_Y = 3;
        static final int ITEM_SPACING_X = 8;
        static final int ITEM_SPACING_Y = 6;
        static final int ITEM_INNER_SPACING_Y = 4;
        static final int SELECT_HEIGHT = 24;
        static final int SELECT_LABEL_GAP = 8;
        static final int SELECT_POPUP_GAP = 4;
        static final int SELECT_OPTION_HEIGHT = 22;
        static final int SELECT_OPTION_GAP = 2;
        static final int SELECT_POPUP_PAD_Y = 4;
        static final int GEAR_BUTTON_SIZE = 18;
        static final int SETTINGS_POPUP_W = 188;
        static final int SETTINGS_POPUP_PAD_X = 10;
        static final int SETTINGS_POPUP_PAD_Y = 10;

        private Control() {
        }
    }

    static final class HotkeyPopup {
        static final int POPUP_W = 146;
        static final int DETAIL_W = 200;
        static final int DETAIL_OVERLAP = 8;
        static final int POPUP_PAD = 4;
        static final int ROW_H = 28;
        static final int DIVIDER_GAP = 6;
        static final int RIGHT_PAD = 12;
        static final int SWITCH_W = 24;
        static final int SWITCH_H = 14;
        static final int VALUE_W = 92;
        static final int VALUE_H = 20;
        static final int VALUE_POPUP_W = 112;
        static final float VALUE_TEXT_SIZE = 10.0f;
        static final float ICON_SIZE = 10.5f;
        static final float ARROW_ICON_SIZE = 8.0f;
        static final int TEXT_Y_OFFSET = 7;

        private HotkeyPopup() {
        }
    }

    static final class ColorPopup {
        static final int BUTTON_W = 24;
        static final int POPUP_W = 168;
        static final int POPUP_PAD_X = 10;
        static final int POPUP_PAD_Y = 9;
        static final int PREVIEW_H = 18;
        static final int ROW_H = 13;
        static final int ROW_GAP = 5;

        private ColorPopup() {
        }
    }

    static final class HotkeyHud {
        static final int PAD_X = 4;
        static final int TITLE_H = 14;
        static final int ITEM_PAD = 3;
        static final int ITEM_GAP = 3;
        static final int COLUMN_GAP = 4;
        static final int MIN_W = 58;
        static final float TITLE_SIZE = 6.0f;
        static final float TEXT_SIZE = 6.0f;
        static final float ICON_SIZE = 6.0f;

        private HotkeyHud() {
        }
    }
}
