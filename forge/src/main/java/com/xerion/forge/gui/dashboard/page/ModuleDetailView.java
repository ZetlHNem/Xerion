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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class ModuleDetailView {
    void render(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds, Module module) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_DIM);
        if (module == null) {
            ShapeRenderer.text(graphics, font, "No Module Selected", bounds.x() + 16, bounds.y() + 16, UiTheme.TEXT_MUTED);
            return;
        }

        ShapeRenderer.text(graphics, font, module.displayName(), bounds.x() + 16, bounds.y() + 14, UiTheme.TEXT_PRIMARY);
        ShapeRenderer.text(graphics, font, module.id(), bounds.x() + 16, bounds.y() + 28, UiTheme.TEXT_MUTED);
        ShapeRenderer.switchControl(graphics, bounds.right() - 46, bounds.y() + 18, module.enabled());

        int cardY = bounds.y() + 58;
        drawGeneralCard(graphics, font, new DashboardLayout.Rect(bounds.x() + 10, cardY, bounds.width() - 20, 104), module);
        cardY += 118;
        drawSliderCard(graphics, font, new DashboardLayout.Rect(bounds.x() + 10, cardY, bounds.width() - 20, 72), "Animation", 0.62f, "62%");
        cardY += 86;
        drawSliderCard(graphics, font, new DashboardLayout.Rect(bounds.x() + 10, cardY, bounds.width() - 20, 72), "Opacity", module.enabled() ? 0.78f : 0.35f, module.enabled() ? "78%" : "35%");
        cardY += 86;
        drawDescriptionCard(graphics, font, new DashboardLayout.Rect(bounds.x() + 10, cardY, bounds.width() - 20, 62));
    }

    private void drawGeneralCard(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds, Module module) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_CONTAINER);
        ShapeRenderer.text(graphics, font, "General", bounds.x() + 12, bounds.y() + 10, UiTheme.TEXT_PRIMARY);
        graphics.hLine(bounds.x() + 12, bounds.right() - 12, bounds.y() + 30, UiTheme.OUTLINE_SOFT);
        drawBoolRow(graphics, font, new DashboardLayout.Rect(bounds.x() + 12, bounds.y() + 42, bounds.width() - 24, 24), "Enabled", module.enabled());
        drawBoolRow(graphics, font, new DashboardLayout.Rect(bounds.x() + 12, bounds.y() + 72, bounds.width() - 24, 24), "Visible In HUD", module.enabled());
    }

    private void drawBoolRow(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds, String label, boolean enabled) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_CONTAINER_HIGH);
        ShapeRenderer.text(graphics, font, label, bounds.x() + 10, bounds.y() + 8, UiTheme.TEXT_SECONDARY);
        ShapeRenderer.switchControl(graphics, bounds.right() - 38, bounds.y() + 4, enabled);
    }

    private void drawSliderCard(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds, String label, float value, String suffix) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_CONTAINER);
        ShapeRenderer.text(graphics, font, label, bounds.x() + 12, bounds.y() + 12, UiTheme.TEXT_PRIMARY);
        ShapeRenderer.text(graphics, font, suffix, bounds.right() - 42, bounds.y() + 12, UiTheme.PRIMARY);
        int trackX = bounds.x() + 12;
        int trackY = bounds.y() + 46;
        int trackW = bounds.width() - 24;
        graphics.hLine(trackX, trackX + trackW, trackY, UiTheme.OUTLINE_SOFT);
        int filled = Math.round(trackW * value);
        graphics.hLine(trackX, trackX + filled, trackY, UiTheme.PRIMARY);
        ShapeRenderer.roundRect(graphics, trackX + filled - 5, trackY - 5, 10, 10, UiTheme.PRIMARY);
    }

    private void drawDescriptionCard(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_CONTAINER);
        ShapeRenderer.text(graphics, font, "Description", bounds.x() + 12, bounds.y() + 12, UiTheme.TEXT_PRIMARY);
        ShapeRenderer.text(graphics, font, "Common module rendered through the Forge adapter.", bounds.x() + 12, bounds.y() + 30, UiTheme.TEXT_SECONDARY);
    }
}
