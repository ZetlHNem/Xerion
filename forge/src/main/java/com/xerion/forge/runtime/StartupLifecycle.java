package com.xerion.forge.runtime;

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

import com.xerion.common.XerionCore;
import com.xerion.forge.XerionForge;
import com.xerion.forge.config.ProfileConfigStore;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.gui.dashboard.ModuleDashboardScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StartupLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(XerionForge.MOD_ID);
    private static boolean clickGuiWarmupComplete;
    private static boolean initialConfigAutoLoadPending = true;

    private StartupLifecycle() {
    }

    public static void handleRuntimeTick(XerionCore core, TickEvent.Phase phase) {
        Minecraft minecraft = Minecraft.getInstance();
        RuntimeWorldLifecycle.tick(minecraft);
        if (phase != TickEvent.Phase.END) {
            return;
        }

        autoLoadInitialConfig(core);
        if (holdTitleScreenUntilRenderResourcesReady()) {
            return;
        }
        KeyBindingRegistry.handleOpenClickGuiToggle(minecraft, core);
    }

    public static void requestInitialConfigAutoLoad() {
        initialConfigAutoLoadPending = true;
    }

    private static void autoLoadInitialConfig(XerionCore core) {
        if (!initialConfigAutoLoadPending) {
            return;
        }
        initialConfigAutoLoadPending = false;
        ProfileConfigStore.AutoLoadResult result = ProfileConfigStore.autoLoad();
        if (result.loaded()) {
            ModuleDashboardScreen.warmupHotkeys(core);
            LOGGER.info("Auto-loaded config '{}'", result.profile());
        } else if (result.exception() != null) {
            LOGGER.warn("Auto-load config failed for '{}'", result.profile(), result.exception());
        }
    }

    private static boolean holdTitleScreenUntilRenderResourcesReady() {
        if (clickGuiWarmupComplete || MSDFRenderer.renderResourcesReady() || MSDFRenderer.preloadFailed()) {
            clickGuiWarmupComplete = true;
            return false;
        }
        MSDFRenderer.startAsyncPreload();
        return false;
    }
}

