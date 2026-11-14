package com.xerion.common.render;

import com.xerion.common.module.HudModule;
import com.xerion.common.module.Module;
import com.xerion.common.module.ModuleManager;

public final class HudRenderer {
    private final ModuleManager moduleManager;

    public HudRenderer(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void render(RenderContext context) {
        for (Module module : moduleManager.modules()) {
            if (module.enabled() && module instanceof HudModule hudModule) {
                hudModule.renderHud(context);
            }
        }
    }
}
