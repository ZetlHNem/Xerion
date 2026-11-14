package com.xerion.forge.runtime;

import com.xerion.common.XerionCore;
import com.xerion.forge.module.ModuleEventDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public final class GameEventLifecycle {
    private final ModuleEventDispatcher moduleEvents;
    private ClientLevel lastRuntimeLevel;

    public GameEventLifecycle(XerionCore core) {
        this.moduleEvents = new ModuleEventDispatcher(core);
    }

    public void clearInstanceWorldRuntimeIfNeeded(Minecraft minecraft) {
        ClientLevel currentLevel = minecraft == null ? null : minecraft.level;
        if (currentLevel == lastRuntimeLevel) {
            return;
        }
        moduleEvents.clearWorldRuntime(minecraft);
        lastRuntimeLevel = currentLevel;
    }

    public void runtimeTick(Minecraft minecraft, net.minecraftforge.event.TickEvent.Phase phase) {
        if (phase == net.minecraftforge.event.TickEvent.Phase.START) {
            runtimeTickStart(minecraft);
        } else {
            runtimeTickEnd(minecraft);
        }
    }

    public void renderTick(float renderTickTime) {
        moduleEvents.renderTick(renderTickTime);
    }

    public void playSound(PlaySoundEvent event) {
        moduleEvents.playSound(event);
    }

    public void playerTickStart(Minecraft minecraft) {
        moduleEvents.playerTickStart(minecraft);
    }

    public void attackEntity(AttackEntityEvent event) {
        moduleEvents.attackEntity(event);
    }

    public void movementInputUpdate(MovementInputUpdateEvent event) {
        moduleEvents.movementInputUpdate(event);
    }

    public void renderPlayerPre(RenderPlayerEvent.Pre event) {
        moduleEvents.renderPlayerPre(event);
    }

    public void renderPlayerPost(RenderPlayerEvent.Post event) {
        moduleEvents.renderPlayerPost(event);
    }

    public void renderLevelStage(RenderLevelStageEvent event) {
        moduleEvents.renderLevelStage(event);
    }

    private void runtimeTickStart(Minecraft minecraft) {
        moduleEvents.runtimeTickStart(minecraft);
    }

    private void runtimeTickEnd(Minecraft minecraft) {
        moduleEvents.runtimeTickEnd(minecraft);
    }
}
