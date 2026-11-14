package com.xerion.forge.visual;

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

import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.esp.MobScreenEspOverlayRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class WorldOverlayRenderPipeline {
    private final WorldEffectRenderer worldEffectRenderer;
    private final ModelGlowRenderer modelGlowRenderer = new ModelGlowRenderer();

    public WorldOverlayRenderPipeline(WorldEffectRenderer worldEffectRenderer) {
        this.worldEffectRenderer = worldEffectRenderer;
    }

    public void render(RenderLevelStageEvent event) {
        boolean visualEspEnabled = VisualSettings.visualEspEnabled();
        boolean renderModelGlow = modelGlowRenderer.enabled();
        boolean renderSounds = visualEspEnabled && VisualSettings.visualSounds() && worldEffectRenderer.hasSoundMarkers();
        boolean renderSoulParticles = visualEspEnabled && VisualSettings.visualSoulParticles()
                && worldEffectRenderer.hasSoulParticles();
        if (!renderModelGlow && !renderSounds && !renderSoulParticles) {
            MobScreenEspOverlayRenderer.renderWorldNames(event);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }

        Camera camera = event.getCamera() == null ? minecraft.gameRenderer.getMainCamera() : event.getCamera();
        Matrix4f projectionMatrix = event.getProjectionMatrix();

        PoseStack cameraPoseStack = new PoseStack();
        cameraPoseStack.mulPoseMatrix(ModelGlowRenderer.cameraRelativeMatrix(event.getPoseStack().last().pose()));

        Matrix4f previousProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(projectionMatrix), VertexSorting.DISTANCE_TO_ORIGIN);
        minecraft.getMainRenderTarget().bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        try {
            if (renderModelGlow) {
                modelGlowRenderer.render(minecraft, cameraPoseStack, camera, event.getPartialTick());
                resetProjection(minecraft, projectionMatrix);
                ModelGlowRenderer.prepareWorldOverlayRenderState();
            }
            MobScreenEspOverlayRenderer.renderWorldNames(event);
            resetProjection(minecraft, projectionMatrix);
            if (renderSounds) {
                ModelGlowRenderer.prepareWorldOverlayRenderState();
                worldEffectRenderer.renderSoundMarkers(minecraft, cameraPoseStack, camera, projectionMatrix);
            }
            if (renderSoulParticles) {
                ModelGlowRenderer.prepareWorldOverlayRenderState();
                worldEffectRenderer.renderSoulParticles(cameraPoseStack, camera);
            }
        } finally {
            RenderSystem.lineWidth(1.0f);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            modelViewStack.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(previousProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            minecraft.getMainRenderTarget().bindWrite(false);
        }
    }

    private static void resetProjection(Minecraft minecraft, Matrix4f projectionMatrix) {
        RenderSystem.setProjectionMatrix(new Matrix4f(projectionMatrix), VertexSorting.DISTANCE_TO_ORIGIN);
        minecraft.getMainRenderTarget().bindWrite(false);
    }
}
