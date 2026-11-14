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

import com.xerion.forge.runtime.RuntimeStateReset;
import com.xerion.forge.esp.BarrierBlockOverlayRenderer;
import com.xerion.forge.esp.BedEspOverlayRenderer;
import com.xerion.forge.esp.BlockWireEspOverlayRenderer;
import com.xerion.forge.esp.ContainerWireEspOverlayRenderer;
import com.xerion.forge.esp.LightBlockOverlayRenderer;
import com.xerion.forge.esp.MobScreenEspOverlayRenderer;
import com.xerion.forge.esp.SpawnerEspOverlayRenderer;
import com.xerion.forge.esp.XrayOverlayRenderer;
import com.xerion.forge.gui.dashboard.KawaseBlurPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class RuntimeWorldLifecycle {
    private static ClientLevel lastLevel;

    private RuntimeWorldLifecycle() {
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel currentRuntimeLevel = minecraft == null ? null : minecraft.level;
        if (currentRuntimeLevel == lastLevel) {
            return;
        }
        clearWorldRuntime();
        lastLevel = currentRuntimeLevel;
    }

    private static void clearWorldRuntime() {
        BarrierBlockOverlayRenderer.clearScanCache();
        BedEspOverlayRenderer.clearScanCache();
        BlockWireEspOverlayRenderer.clearScanCache();
        ContainerWireEspOverlayRenderer.clearScanCache();
        LightBlockOverlayRenderer.clearScanCache();
        SpawnerEspOverlayRenderer.clearScanCache();
        XrayOverlayRenderer.clearScanCache();
        MobScreenEspOverlayRenderer.clearRuntimeCache();
        BacktrackService.clear();
        BlinkService.clearRuntimeState();
        CombatHudTarget.clear();
        FakePlayerRegistry.clear();
        BetterChatService.clearRuntimeState();
        KawaseBlurPipeline.clearResources();
        RuntimeStateReset.clearWorldRuntime();
    }
}

