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

import com.xerion.forge.esp.BarrierBlockOverlayRenderer;
import com.xerion.forge.esp.BedEspOverlayRenderer;
import com.xerion.forge.esp.BlockOverlayRenderer;
import com.xerion.forge.esp.BlockWireEspOverlayRenderer;
import com.xerion.forge.esp.ContainerWireEspOverlayRenderer;
import com.xerion.forge.esp.LightBlockOverlayRenderer;
import com.xerion.forge.esp.SpawnerEspOverlayRenderer;
import com.xerion.forge.esp.XrayOverlayRenderer;
import net.minecraftforge.common.MinecraftForge;

public final class WorldRenderEventRegistry {
    private WorldRenderEventRegistry() {
    }

    public static void registerEventBusRenderers() {
        MinecraftForge.EVENT_BUS.register(new BarrierBlockOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new BedEspOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new BlockWireEspOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new BlockOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new ContainerWireEspOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new LightBlockOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new SpawnerEspOverlayRenderer());
        MinecraftForge.EVENT_BUS.register(new XrayOverlayRenderer());
    }
}

