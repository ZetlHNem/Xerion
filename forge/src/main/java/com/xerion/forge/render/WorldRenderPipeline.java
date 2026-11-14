package com.xerion.forge.render;

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

import com.xerion.forge.esp.MobScreenEspOverlayRenderer;
import com.xerion.forge.visual.WorldEffectRenderer;
import com.xerion.forge.visual.WorldOverlayRenderPipeline;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public final class WorldRenderPipeline {
    private final VeinMinerService VeinMinerService;
    private final WorldOverlayRenderPipeline worldOverlayRenderPipeline;

    public WorldRenderPipeline(VeinMinerService VeinMinerService,
                                      WorldEffectRenderer worldEffectRenderer) {
        this.VeinMinerService = VeinMinerService;
        this.worldOverlayRenderPipeline = new WorldOverlayRenderPipeline(worldEffectRenderer);
    }

    public void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            MobScreenEspOverlayRenderer.captureWorldMatrices(event);
        }

        boolean levelStage = event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER;
        if (!levelStage) {
            return;
        }
        VeinMinerService.renderPreview(event);
        worldOverlayRenderPipeline.render(event);
    }
}
