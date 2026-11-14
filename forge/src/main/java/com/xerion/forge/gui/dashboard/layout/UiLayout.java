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

final class UiLayout {
    final int originX;
    final int originY;
    final int x;
    final int w;
    final int contentStartY;
    int cursorY;
    int idCursorY;
    int lastIdY;

    UiLayout(int originX, int originY, int x, int y, int idY, int w) {
        this.originX = originX;
        this.originY = originY;
        this.x = x;
        this.contentStartY = y;
        this.cursorY = y;
        this.idCursorY = idY;
        this.lastIdY = idY;
        this.w = w;
    }

    int next(int height) {
        int y = cursorY;
        lastIdY = idCursorY;
        cursorY += height + UiMetrics.Control.ITEM_SPACING_Y;
        idCursorY += height + UiMetrics.Control.ITEM_SPACING_Y;
        return y;
    }

    int idY() {
        return lastIdY;
    }

    int nextFrame() {
        return next(frameHeight());
    }

    int nextLabeledFrame() {
        return next(labeledFrameHeight());
    }

    int nextSelect() {
        return next(UiMetrics.Control.SELECT_HEIGHT);
    }

    int nextLabeledSelect() {
        return next(UiMetrics.Control.FONT_SIZE + UiMetrics.Control.SELECT_LABEL_GAP
                + UiMetrics.Control.SELECT_HEIGHT);
    }

    void sectionGap() {
        cursorY += UiMetrics.Control.ITEM_SPACING_Y * 2;
        idCursorY += UiMetrics.Control.ITEM_SPACING_Y * 2;
    }

    int contentHeight() {
        return Math.max(0, cursorY - contentStartY - UiMetrics.Control.ITEM_SPACING_Y);
    }

    private static int frameHeight() {
        return UiMetrics.Control.FONT_SIZE + UiMetrics.Control.FRAME_PADDING_Y * 2;
    }

    private static int labeledFrameHeight() {
        return UiMetrics.Control.FONT_SIZE + UiMetrics.Control.ITEM_INNER_SPACING_Y + frameHeight();
    }
}
