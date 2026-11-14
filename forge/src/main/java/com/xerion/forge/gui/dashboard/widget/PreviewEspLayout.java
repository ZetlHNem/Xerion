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

import com.xerion.forge.config.VisualSettings;

import java.util.EnumMap;

final class PreviewEspLayout {
    private static final int GAP = 2;

    private final int boxX;
    private final int boxY;
    private final int boxW;
    private final int boxH;
    private final EnumMap<VisualSettings.EspAnchor, Integer> offsets =
            new EnumMap<>(VisualSettings.EspAnchor.class);

    PreviewEspLayout(int boxX, int boxY, int boxW, int boxH) {
        this.boxX = boxX;
        this.boxY = boxY;
        this.boxW = boxW;
        this.boxH = boxH;
        for (VisualSettings.EspAnchor anchor : VisualSettings.EspAnchor.values()) {
            offsets.put(anchor, 0);
        }
    }

    PreviewBounds place(VisualSettings.EspAnchor anchor, PreviewSize size) {
        int offset = offsets.getOrDefault(anchor, 0);
        PreviewBounds bounds;
        if (anchor == VisualSettings.EspAnchor.BOTTOM) {
            int x = Math.round(boxX + boxW * 0.5f - size.w() * 0.5f);
            int y = boxY + boxH + GAP + offset;
            bounds = new PreviewBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + size.h() + GAP);
        } else if (anchor == VisualSettings.EspAnchor.LEFT) {
            int x = boxX - size.w() - GAP - offset;
            int y = boxY;
            bounds = new PreviewBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + size.w() + GAP);
        } else if (anchor == VisualSettings.EspAnchor.RIGHT) {
            int x = boxX + boxW + GAP + offset;
            int y = boxY;
            bounds = new PreviewBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + size.w() + GAP);
        } else {
            int x = Math.round(boxX + boxW * 0.5f - size.w() * 0.5f);
            int y = boxY - size.h() - GAP - offset;
            bounds = new PreviewBounds(x, y, size.w(), size.h());
            offsets.put(anchor, offset + size.h() + GAP);
        }
        return bounds;
    }
}
