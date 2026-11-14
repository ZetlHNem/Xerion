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

import java.util.List;

final class ModuleListView {
    void render(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds, ModuleScreenState state,
                List<Module> modules, List<InteractionHitBox> hitBoxes, int mouseX, int mouseY) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_DIM);
        ShapeRenderer.text(graphics, font, state.selectedCategory().name(), bounds.x() + 12, bounds.y() + 12, UiTheme.TEXT_PRIMARY);
        ShapeRenderer.text(graphics, font, "Modules", bounds.x() + 12, bounds.y() + 25, UiTheme.TEXT_MUTED);

        drawSearch(graphics, font, new DashboardLayout.Rect(bounds.x() + 10, bounds.y() + 46, bounds.width() - 20, 24));

        int rowY = bounds.y() + 80;
        if (modules.isEmpty()) {
            DashboardLayout.Rect empty = new DashboardLayout.Rect(bounds.x() + 10, rowY, bounds.width() - 20, 42);
            ShapeRenderer.roundRect(graphics, empty, UiTheme.SURFACE_CONTAINER);
            ShapeRenderer.text(graphics, font, "No Modules In This Category", empty.x() + 12, empty.y() + 15, UiTheme.TEXT_MUTED);
            return;
        }

        for (Module module : modules) {
            DashboardLayout.Rect row = new DashboardLayout.Rect(bounds.x() + 10, rowY, bounds.width() - 20, 42);
            boolean selected = module == state.selectedModule();
            boolean hovered = row.contains(mouseX, mouseY);
            int color = selected ? UiTheme.PRIMARY_CONTAINER : UiTheme.rowSurface(hovered);
            ShapeRenderer.roundRect(graphics, row, color);
            ShapeRenderer.text(graphics, font, module.displayName(), row.x() + 12, row.y() + 9, selected ? UiTheme.ON_PRIMARY_CONTAINER : UiTheme.TEXT_PRIMARY);
            ShapeRenderer.text(graphics, font, "common", row.x() + 12, row.y() + 23, selected ? UiTheme.ON_PRIMARY_CONTAINER : UiTheme.TEXT_MUTED);
            ShapeRenderer.switchControl(graphics, row.right() - 42, row.y() + 13, module.enabled());
            hitBoxes.add(new InteractionHitBox(InteractionHitBox.Type.MODULE, null, module, row));
            rowY += 46;
        }
    }

    private void drawSearch(GuiGraphics graphics, Font font, DashboardLayout.Rect bounds) {
        ShapeRenderer.roundRect(graphics, bounds, UiTheme.SURFACE_CONTAINER);
        ShapeRenderer.text(graphics, font, "Search Modules", bounds.x() + 10, bounds.y() + 8, UiTheme.TEXT_MUTED);
    }
}
