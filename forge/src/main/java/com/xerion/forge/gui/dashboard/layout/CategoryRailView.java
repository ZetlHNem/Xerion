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

import com.xerion.common.module.ModuleCategory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Map;

final class CategoryRailView {
    void render(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds, ModuleScreenState state,
                Map<ModuleCategory, Integer> counts, List<InteractionHitBox> hitBoxes, int mouseX, int mouseY) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_DIM);
        ShapeRenderer.text(graphics, font, "Xerion", bounds.x() + 38, bounds.y() + 10, UiTheme.TEXT_PRIMARY);
        ShapeRenderer.text(graphics, font, "Dashboard", bounds.x() + 38, bounds.y() + 23, UiTheme.TEXT_MUTED);
        drawMenuGlyph(graphics, bounds.x() + 12, bounds.y() + 13);
        graphics.hLine(bounds.x() + 10, bounds.right() - 10, bounds.y() + 42, UiTheme.OUTLINE_SOFT);

        int rowY = bounds.y() + 52;
        for (ModuleCategory category : ModuleCategory.values()) {
            DashboardLayout.Rect row = new DashboardLayout.Rect(bounds.x() + 8, rowY, bounds.width() - 16, 34);
            boolean selected = !state.settingsMode() && category == state.selectedCategory();
            boolean hovered = row.contains(mouseX, mouseY);
            int color = selected ? UiTheme.SECONDARY_CONTAINER : hovered ? UiTheme.SURFACE_CONTAINER_HIGH : 0;
            if (color != 0) {
                ShapeRenderer.roundRect(graphics, row, color);
            }

            int textColor = selected ? UiTheme.ON_SECONDARY_CONTAINER : UiTheme.TEXT_PRIMARY;
            ShapeRenderer.text(graphics, font, iconFor(category), row.x() + 12, row.y() + 12, selected ? UiTheme.ON_SECONDARY_CONTAINER : UiTheme.TEXT_SECONDARY);
            ShapeRenderer.text(graphics, font, category.name(), row.x() + 34, row.y() + 8, textColor);
            ShapeRenderer.text(graphics, font, Integer.toString(counts.getOrDefault(category, 0)), row.right() - 24, row.y() + 8, selected ? UiTheme.ON_SECONDARY_CONTAINER : UiTheme.TEXT_MUTED);
            hitBoxes.add(new InteractionHitBox(InteractionHitBox.Type.CATEGORY, category, null, row));
            rowY += 38;
        }

        DashboardLayout.Rect settingsRow = new DashboardLayout.Rect(bounds.x() + 8, bounds.bottom() - 42, bounds.width() - 16, 34);
        ShapeRenderer.roundRect(graphics, settingsRow, state.settingsMode() ? UiTheme.SECONDARY_CONTAINER : UiTheme.SURFACE_CONTAINER);
        ShapeRenderer.text(graphics, font, "S", settingsRow.x() + 12, settingsRow.y() + 12, UiTheme.TEXT_SECONDARY);
        ShapeRenderer.text(graphics, font, "Settings", settingsRow.x() + 34, settingsRow.y() + 8, state.settingsMode() ? UiTheme.ON_SECONDARY_CONTAINER : UiTheme.TEXT_PRIMARY);
        hitBoxes.add(new InteractionHitBox(InteractionHitBox.Type.SETTINGS_SETTINGS, null, null, settingsRow));
    }

    private void drawMenuGlyph(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 13, y + 2, UiTheme.TEXT_SECONDARY);
        graphics.fill(x, y + 5, x + 13, y + 7, UiTheme.TEXT_SECONDARY);
        graphics.fill(x, y + 10, x + 13, y + 12, UiTheme.TEXT_SECONDARY);
    }

    private static String iconFor(ModuleCategory category) {
        return switch (category) {
            case HUD -> "H";
            case RENDER -> "R";
            case MOVEMENT -> "M";
            case COMBAT -> "C";
            case MISC -> "X";
        };
    }
}
