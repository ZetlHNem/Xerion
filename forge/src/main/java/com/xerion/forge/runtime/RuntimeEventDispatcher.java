package com.xerion.forge.runtime;

import com.xerion.common.XerionCore;
import com.xerion.forge.combat.*;
import com.xerion.forge.event.ForgeModEventBridge;
import com.xerion.forge.gui.GuiRenderDispatcher;
import com.xerion.forge.gui.dashboard.WidgetSurface;
import com.xerion.forge.misc.AutoHypixelService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class RuntimeEventDispatcher {
    private final XerionCore core;
    private final GuiRenderDispatcher guiRenderDispatcher;
    private final GameEventLifecycle gameLifecycle;

    public RuntimeEventDispatcher(XerionCore core) {
        this.core = core;
        this.guiRenderDispatcher = new GuiRenderDispatcher(core);
        this.gameLifecycle = new GameEventLifecycle(core);
    }

    @SubscribeEvent
    public void onRuntimeTick(ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        gameLifecycle.clearInstanceWorldRuntimeIfNeeded(minecraft);
        ForgeModEventBridge.handleRuntimeTick(core, event.phase);
        gameLifecycle.runtimeTick(minecraft, event.phase);
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        gameLifecycle.renderTick(event.renderTickTime);
    }

    @SubscribeEvent
    public void onPlaySound(PlaySoundEvent event) {
        gameLifecycle.playSound(event);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        gameLifecycle.attackEntity(event);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.START || !(event.player instanceof LocalPlayer)) {
            return;
        }
        gameLifecycle.playerTickStart(Minecraft.getInstance());
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        WidgetSurface.updateHotkeyState();
        guiRenderDispatcher.renderHud(event);
    }

    @SubscribeEvent
    public void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        guiRenderDispatcher.renderScreenPre(event);
    }

    @SubscribeEvent
    public void onScreenOpening(ScreenEvent.Opening event) {
        guiRenderDispatcher.screenOpening(event);
    }

    @SubscribeEvent
    public void onScreenInitialized(ScreenEvent.Init.Post event) {
        guiRenderDispatcher.screenInitialized(event);
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        AutoHypixelService.onChat(event.getMessage());
        guiRenderDispatcher.chatReceived(event);
    }

    @SubscribeEvent
    public void onMovementInputUpdate(MovementInputUpdateEvent event) {
        gameLifecycle.movementInputUpdate(event);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        InputEventDispatcher.handleKeyInput(event);
    }

    @SubscribeEvent
    public void onMouseButton(InputEvent.MouseButton event) {
        InputEventDispatcher.handleMouseButton(event);
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        gameLifecycle.renderPlayerPre(event);
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        gameLifecycle.renderPlayerPost(event);
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        gameLifecycle.renderLevelStage(event);
    }
}

