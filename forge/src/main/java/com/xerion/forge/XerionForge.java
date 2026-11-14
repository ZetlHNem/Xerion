package com.xerion.forge;

import com.xerion.common.XerionCore;
import com.xerion.common.XerionCoreHolder;
import com.xerion.common.platform.PlatformServices;
import com.xerion.common.platform.PlatformRuntime;
import com.xerion.common.platform.PlatformRuntimeInfo;
import com.xerion.common.platform.PlatformRuntimeRegistry;
import com.xerion.forge.event.ForgeModEventBridge;
import com.xerion.forge.game.ForgePlatformGame;
import com.xerion.forge.combat.TaczDoubleTapEventHandler;
import com.xerion.forge.gui.font.MSDFRenderer;
import com.xerion.forge.runtime.RuntimeEventDispatcher;
import com.xerion.forge.runtime.WorldRenderEventRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod(XerionForge.MOD_ID)
public final class XerionForge {
    public static final String MOD_ID = "xerion";

    private final XerionCore core;
    private final RuntimeEventDispatcher eventDispatcher;

    public XerionForge() {
        PlatformRuntimeRegistry.register(new PlatformRuntimeInfo(
                PlatformRuntime.Loader.FORGE,
                MOD_ID,
                "Xerion",
                XerionForge.class.getPackage().getImplementationVersion()
        ));
        PlatformServices.registerGame(new ForgePlatformGame());
        XerionCoreHolder.initialize();
        this.core = XerionCoreHolder.get();
        this.eventDispatcher = new RuntimeEventDispatcher(core);
        MSDFRenderer.startEarlyCachePreload();
        MinecraftForge.EVENT_BUS.register(eventDispatcher);
        if (ModList.get().isLoaded("tacz")) {
            MinecraftForge.EVENT_BUS.register(new TaczDoubleTapEventHandler());
        }
        WorldRenderEventRegistry.registerEventBusRenderers();
        ForgeModEventBridge.requestInitialConfigAutoLoad();
    }
}




