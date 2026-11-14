package com.xerion.common.module;

import com.xerion.common.render.RenderContext;

public abstract class HudModule extends Module {
    protected HudModule(String id, String displayName) {
        super(id, displayName, ModuleCategory.HUD);
    }

    public abstract void renderHud(RenderContext context);
}
