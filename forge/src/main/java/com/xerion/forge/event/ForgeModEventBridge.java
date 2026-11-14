package com.xerion.forge.event;

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
import com.xerion.forge.runtime.KeyBindingRegistry;
import com.xerion.forge.runtime.RenderResourceRegistry;
import com.xerion.forge.runtime.StartupLifecycle;
import com.xerion.forge.gui.dashboard.WindowsImeBridge;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = XerionForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeModEventBridge {
    private ForgeModEventBridge() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindingRegistry.registerKeyMappings(event);
    }

    public static boolean isOpenClickGuiKey(int key, int scanCode) {
        return KeyBindingRegistry.isOpenClickGuiKey(key, scanCode);
    }

    public static boolean isVeinMinerKeyDown() {
        return KeyBindingRegistry.isVeinMinerKeyDown();
    }

    public static String veinMinerKeyName() {
        return KeyBindingRegistry.veinMinerKeyName();
    }

    public static boolean isAutoSortKeyDown() {
        return KeyBindingRegistry.isAutoSortKeyDown();
    }

    public static String autoSortKeyName() {
        return KeyBindingRegistry.autoSortKeyName();
    }

    public static String pearlThrowKeyName() {
        return KeyBindingRegistry.pearlThrowKeyName();
    }

    public static boolean isQuickSwitchKeyDown() {
        return KeyBindingRegistry.isQuickSwitchKeyDown();
    }

    public static String quickSwitchKeyName() {
        return KeyBindingRegistry.quickSwitchKeyName();
    }

    @SubscribeEvent
    public static void setupRenderResources(FMLClientSetupEvent event) {
        RenderResourceRegistry.startEarlyResourcePreload();
        event.enqueueWork(WindowsImeBridge::initializeGlobal);
    }

    @SubscribeEvent
    public static void registerRuntimeReloadListeners(RegisterClientReloadListenersEvent event) {
        RenderResourceRegistry.registerRuntimeReloadListeners(event);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        RenderResourceRegistry.registerShaders(event);
    }

    public static void handleRuntimeTick(XerionCore core, TickEvent.Phase phase) {
        StartupLifecycle.handleRuntimeTick(core, phase);
    }

    public static void requestInitialConfigAutoLoad() {
        StartupLifecycle.requestInitialConfigAutoLoad();
    }
}

