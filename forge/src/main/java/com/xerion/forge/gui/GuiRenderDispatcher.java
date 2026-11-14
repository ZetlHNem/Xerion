package com.xerion.forge.gui;

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
import com.xerion.common.XerionCoreHolder;
import com.xerion.common.event.events.RenderEvent;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.esp.MobScreenEspOverlayRenderer;
import com.xerion.forge.gui.dashboard.ArrayListHudRenderer;
import com.xerion.forge.gui.dashboard.AnimationClock;
import com.xerion.forge.gui.dashboard.BackpackHudRenderer;
import com.xerion.forge.gui.dashboard.DoubleTapHudRenderer;
import com.xerion.forge.gui.dashboard.HotkeyHudRenderer;
import com.xerion.forge.gui.dashboard.NotificationHudRenderer;
import com.xerion.forge.gui.dashboard.MusicLyricsHudRenderer;
import com.xerion.forge.gui.dashboard.KawaseBlurPipeline;
import com.xerion.forge.gui.dashboard.PotionBuffHudRenderer;
import com.xerion.forge.gui.dashboard.SpeedHudRenderer;
import com.xerion.forge.gui.dashboard.TargetsHudRenderer;
import com.xerion.forge.gui.dashboard.WatermarkHudRenderer;
import com.xerion.forge.gui.dashboard.XerionTitleScreen;
import com.xerion.forge.gui.dashboard.XerionCreateWorldScreen;
import com.xerion.forge.gui.dashboard.XerionBiomeWorldScreen;
import com.xerion.forge.gui.dashboard.XerionDataPackScreen;
import com.xerion.forge.gui.dashboard.XerionExperimentsScreen;
import com.xerion.forge.gui.dashboard.XerionEditWorldScreen;
import com.xerion.forge.gui.dashboard.XerionFlatWorldScreen;
import com.xerion.forge.gui.dashboard.XerionLoadingScreen;
import com.xerion.forge.gui.dashboard.XerionMultiplayerScreen;
import com.xerion.forge.mixin.JoinMultiplayerScreenAccessor;
import com.xerion.forge.gui.dashboard.XerionSafetyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.ExperimentsScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import com.xerion.forge.mixin.EditWorldScreenAccessor;

public final class GuiRenderDispatcher {
    private final XerionCore core;

    public GuiRenderDispatcher(XerionCore core) {
        this.core = core;
    }

    public void renderHud(RenderGuiEvent.Post event) {
        BetterChatService.render(event.getGuiGraphics());
        MobScreenEspOverlayRenderer.render(event.getGuiGraphics(), event.getPartialTick());
        core.eventBus().post(new RenderEvent.Hud(new HudRenderContext(
                event.getGuiGraphics(),
                event.getPartialTick()
        )));

        boolean clickGuiOpen = Minecraft.getInstance().screen instanceof ClickGuiScreen;
        if (!clickGuiOpen) {
            AnimationClock.beginFrame();
            KawaseBlurPipeline.beginOverlayStage();
            WatermarkHudRenderer.render(event.getGuiGraphics(), false);
            HotkeyHudRenderer.render(event.getGuiGraphics(), false);
            ArrayListHudRenderer.render(event.getGuiGraphics(), false);
            TargetsHudRenderer.render(event.getGuiGraphics(), false);
            BackpackHudRenderer.render(event.getGuiGraphics(), false);
            PotionBuffHudRenderer.render(event.getGuiGraphics(), false);
            SpeedHudRenderer.render(event.getGuiGraphics(), false);
            MusicLyricsHudRenderer.render(event.getGuiGraphics());
            DoubleTapHudRenderer.render(event.getGuiGraphics());
            NotificationHudRenderer.render(event.getGuiGraphics());
        }
    }

    public void renderScreenPre(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof ChatScreen chatScreen
                && BetterChatService.renderInputReplacement(chatScreen, event.getGuiGraphics(), event.getMouseX(),
                event.getMouseY(), event.getPartialTick())) {
            event.setCanceled(true);
        }
    }

    public void screenOpening(ScreenEvent.Opening event) {
        if (!VisualSettings.modernUiEnabled()) {
            return;
        }
        if (event.getNewScreen() instanceof EditWorldScreen editWorldScreen) {
            EditWorldScreenAccessor accessor = (EditWorldScreenAccessor) (Object) editWorldScreen;
            event.setNewScreen(new XerionEditWorldScreen(
                    accessor.xerion$getCallback(), accessor.xerion$getLevelAccess()));
            return;
        }
        if (event.getNewScreen() instanceof JoinMultiplayerScreen multiplayerScreen
                && !(multiplayerScreen instanceof XerionMultiplayerScreen)) {
            Screen lastScreen = ((JoinMultiplayerScreenAccessor) (Object) multiplayerScreen)
                    .xerion$getLastScreen();
            event.setNewScreen(new XerionMultiplayerScreen(lastScreen));
            return;
        }
        if (event.getNewScreen() instanceof SafetyScreen) {
            event.setNewScreen(new XerionSafetyScreen(event.getCurrentScreen()));
            return;
        }
        if (event.getNewScreen() instanceof CreateBuffetWorldScreen biomeWorldScreen) {
            event.setNewScreen(new XerionBiomeWorldScreen(biomeWorldScreen));
            return;
        }
        if (event.getNewScreen() instanceof CreateFlatWorldScreen flatWorldScreen) {
            event.setNewScreen(new XerionFlatWorldScreen(flatWorldScreen));
            return;
        }
        if (event.getNewScreen() instanceof ExperimentsScreen experimentsScreen) {
            event.setNewScreen(new XerionExperimentsScreen(experimentsScreen));
            return;
        }
        if (event.getNewScreen() instanceof PackSelectionScreen packSelectionScreen
                && packSelectionScreen.getTitle().getString().equals(
                Component.translatable("dataPack.title").getString())) {
            event.setNewScreen(new XerionDataPackScreen(packSelectionScreen));
            return;
        }
        if (event.getNewScreen() instanceof CreateWorldScreen createWorldScreen) {
            event.setNewScreen(new XerionCreateWorldScreen(createWorldScreen));
            return;
        }
        if (event.getNewScreen() instanceof GenericDirtMessageScreen loadingScreen) {
            event.setNewScreen(new XerionLoadingScreen(loadingScreen.getTitle()));
            return;
        }
        if (event.getNewScreen() instanceof TitleScreen
                && !Minecraft.getInstance().isDemo()) {
            event.setNewScreen(new XerionTitleScreen());
        }
    }

    public void screenInitialized(ScreenEvent.Init.Post event) {
        if (VisualSettings.modernUiEnabled()
                || !(event.getScreen() instanceof TitleScreen titleScreen)
                || Minecraft.getInstance().isDemo()) {
            return;
        }

        int menuTop = titleScreen.height / 4 + 48;
        int originalFooterY = menuTop + 84;
        int shiftedFooterY = menuTop + 96;
        int centerX = titleScreen.width / 2;
        for (var listener : event.getListenersList()) {
            if (listener instanceof AbstractWidget widget
                    && widget.getY() == originalFooterY
                    && widget.getX() >= centerX - 124
                    && widget.getX() <= centerX + 124) {
                widget.setY(shiftedFooterY);
            }
        }

        event.addListener(Button.builder(Component.translatable("xerion.menu.dashboard"), button ->
                        Minecraft.getInstance().setScreen(new ClickGuiScreen(XerionCoreHolder.get())))
                .bounds(centerX - 100, menuTop + 72, 200, 20)
                .build());
    }

    public void chatReceived(ClientChatReceivedEvent event) {
        if (!VisualSettings.betterChatEnabled()) {
            return;
        }
        BetterChatService.add(event.getMessage());
        event.setCanceled(true);
    }
}
