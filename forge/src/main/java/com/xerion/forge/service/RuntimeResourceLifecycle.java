package com.xerion.forge.service;

import com.xerion.forge.esp.BarrierBlockOverlayRenderer;
import com.xerion.forge.esp.BedEspOverlayRenderer;
import com.xerion.forge.esp.BlockWireEspOverlayRenderer;
import com.xerion.forge.esp.ContainerWireEspOverlayRenderer;
import com.xerion.forge.esp.LightBlockOverlayRenderer;
import com.xerion.forge.esp.MobScreenEspOverlayRenderer;
import com.xerion.forge.esp.SpawnerEspOverlayRenderer;
import com.xerion.forge.esp.XrayOverlayRenderer;
import com.xerion.forge.misc.BetterChatService;
import com.xerion.forge.world.NukerService;

public final class RuntimeResourceLifecycle {
    private RuntimeResourceLifecycle() {
    }

    public static void clearWorldRuntimeResources() {
        BlockWireEspOverlayRenderer.clearScanCache();
        BlockWireEspOverlayRenderer.clearAccentColorCache();
        ContainerWireEspOverlayRenderer.clearScanCache();
        XrayOverlayRenderer.clearScanCache();
        SpawnerEspOverlayRenderer.clearScanCache();
        BedEspOverlayRenderer.clearScanCache();
        BarrierBlockOverlayRenderer.clearScanCache();
        LightBlockOverlayRenderer.clearScanCache();
        MobScreenEspOverlayRenderer.clearRuntimeCache();
        NukerService.clearRuntimeState();
        BetterChatService.clearRuntimeState();
    }
}
