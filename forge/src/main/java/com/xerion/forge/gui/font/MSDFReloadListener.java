package com.xerion.forge.gui.font;

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

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class MSDFReloadListener extends SimplePreparableReloadListener<MSDFRenderer.ReloadResources> {
    @Override
    protected MSDFRenderer.ReloadResources prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        try {
            return MSDFRenderer.prepareReloadResources(resourceManager);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to prepare TTF font resources", ex);
        }
    }

    @Override
    protected void apply(MSDFRenderer.ReloadResources resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        MSDFRenderer.applyReloadResources(resources);
    }
}
