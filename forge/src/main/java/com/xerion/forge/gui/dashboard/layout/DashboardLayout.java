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

record DashboardLayout(Rect panel, Rect rail, Rect modules, Rect detail) {
    static DashboardLayout compute(int screenWidth, int screenHeight) {
        int dashboardW = Math.min(760, screenWidth - 56);
        int dashboardH = Math.min(468, screenHeight - 56);
        int dashboardX = (screenWidth - dashboardW) / 2;
        int dashboardY = (screenHeight - dashboardH) / 2;

        Rect panel = new Rect(dashboardX, dashboardY, dashboardW, dashboardH);
        Rect rail = new Rect(dashboardX + 8, dashboardY + 8, 118, dashboardH - 16);
        Rect modules = new Rect(rail.right() + 6, rail.y(), 244, rail.height());
        Rect detail = new Rect(modules.right() + 6, rail.y(), panel.right() - 8 - (modules.right() + 6), rail.height());
        return new DashboardLayout(panel, rail, modules, detail);
    }

    record Rect(int x, int y, int width, int height) {
        int right() {
            return x + width;
        }

        int bottom() {
            return y + height;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= right() && mouseY >= y && mouseY <= bottom();
        }
    }
}
