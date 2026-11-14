package com.xerion.forge.render;

import com.xerion.common.movement.ScaffoldRotation;

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

import com.xerion.forge.chams.ChamsProfileResolver;
import com.xerion.forge.chams.PlayerChamsReplacementRenderer;
import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderPlayerEvent;

public final class PlayerRenderPipeline {
    private final KillAuraService KillAuraService;
    private Player adjustedPlayer;
    private float bodyYaw;
    private float lastBodyYaw;
    private float headYaw;
    private float lastHeadYaw;
    private float pitch;
    private float lastPitch;

    public PlayerRenderPipeline(KillAuraService KillAuraService) {
        this.KillAuraService = KillAuraService;
    }

    public void beforeRender(RenderPlayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = event.getEntity();
        ChamsProfileResolver.Profile profile = ChamsProfileResolver.profile(player);
        if (profile == ChamsProfileResolver.Profile.TEAM
                && VisualSettings.visualTeamDisableModelRendering()) {
            if (profile.hasAnyChams(player)) {
                PlayerChamsReplacementRenderer.render(event);
            }
            event.setCanceled(true);
            return;
        }

        boolean killAuraRender = player == minecraft.player && KillAuraService.renderOverrideActive();
        boolean veinMinerRender = player == minecraft.player && !killAuraRender && VeinMinerService.basicRenderRotationActive();
        boolean scaffoldRender = player == minecraft.player && !killAuraRender && !veinMinerRender && ScaffoldRotation.renderActive();
        if (!killAuraRender && !veinMinerRender && !scaffoldRender) {
            return;
        }

        adjustedPlayer = player;
        bodyYaw = player.yBodyRot;
        lastBodyYaw = player.yBodyRotO;
        headYaw = player.yHeadRot;
        lastHeadYaw = player.yHeadRotO;
        pitch = player.getXRot();
        lastPitch = player.xRotO;

        if (killAuraRender) {
            player.yBodyRot = KillAuraService.renderYaw();
            player.yBodyRotO = KillAuraService.renderLastYaw();
            player.yHeadRot = KillAuraService.renderYaw();
            player.yHeadRotO = KillAuraService.renderLastYaw();
            player.setXRot(KillAuraService.renderPitch());
            player.xRotO = KillAuraService.renderLastPitch();
            return;
        }

        if (veinMinerRender) {
            VeinMinerService.updateBasicRenderRotationFrame();
            player.yBodyRot = VeinMinerService.basicRenderYaw();
            player.yBodyRotO = VeinMinerService.basicRenderYaw();
            player.yHeadRot = VeinMinerService.basicRenderYaw();
            player.yHeadRotO = VeinMinerService.basicRenderYaw();
            player.setXRot(VeinMinerService.basicRenderPitch());
            player.xRotO = VeinMinerService.basicRenderPitch();
            return;
        }

        if (ScaffoldRotation.renderYawActive()) {
            player.yBodyRot = ScaffoldRotation.renderYaw();
            player.yBodyRotO = ScaffoldRotation.renderLastYaw();
            player.yHeadRot = ScaffoldRotation.renderYaw();
            player.yHeadRotO = ScaffoldRotation.renderLastYaw();
        }
        player.setXRot(ScaffoldRotation.renderPitch());
        player.xRotO = ScaffoldRotation.renderLastPitch();
    }

    public void afterRender(RenderPlayerEvent.Post event) {
        restoreRotation(event.getEntity());
    }

    private void restoreRotation(Player player) {
        if (player != adjustedPlayer) {
            return;
        }

        player.yBodyRot = bodyYaw;
        player.yBodyRotO = lastBodyYaw;
        player.yHeadRot = headYaw;
        player.yHeadRotO = lastHeadYaw;
        player.setXRot(pitch);
        player.xRotO = lastPitch;
        adjustedPlayer = null;
    }
}
