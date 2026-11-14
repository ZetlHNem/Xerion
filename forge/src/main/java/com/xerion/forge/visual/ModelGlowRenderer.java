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

import com.xerion.forge.chams.ChamsGlow;
import com.xerion.common.visual.ChamsRenderContext;
import com.xerion.forge.chams.ChamsProfileResolver;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.mixin.LevelRendererAccessor;
import com.xerion.forge.mixin.PostChainAccessor;
import com.xerion.forge.mixin.PostPassAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class ModelGlowRenderer {
    public boolean enabled() {
        return VisualSettings.visualEspEnabled() && VisualSettings.visualGlow()
                || VisualSettings.visualTeamEnabled() && VisualSettings.visualTeamGlow()
                || VisualSettings.visualLocalEnabled() && VisualSettings.visualLocalGlow();
    }

    public void render(Minecraft minecraft, PoseStack worldPoseStack, Camera camera, float partialTick) {
        if (!(minecraft.levelRenderer instanceof LevelRendererAccessor accessor)
                || minecraft.level == null
                || minecraft.player == null) {
            return;
        }

        PostChain entityEffect = accessor.xerion$getEntityEffect();
        RenderTarget entityTarget = accessor.xerion$getEntityTarget();
        if (entityEffect == null || entityTarget == null) {
            return;
        }

        double range = modelGlowRange();
        AABB searchBox = minecraft.player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = new ArrayList<>();
        for (LivingEntity entity : minecraft.level.getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (ChamsGlow.shouldGlow(entity) && entity.isAlive() && withinModelGlowRange(minecraft.player, entity)) {
                entities.add(entity);
            }
        }
        for (LivingEntity dummy : FakePlayerRegistry.dummyEntities()) {
            if (ChamsGlow.shouldGlow(dummy) && dummy.isAlive()
                    && withinModelGlowRange(minecraft.player, dummy)) {
                entities.add(dummy);
            }
        }
        if (entities.isEmpty()) {
            return;
        }

        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        OutlineBufferSource outlineBuffer = minecraft.renderBuffers().outlineBufferSource();
        setEntityOutlineOpacity(entityEffect, 1.0f);
        entityTarget.clear(Minecraft.ON_OSX);
        entityTarget.bindWrite(false);

        ChamsRenderContext.beginOutlineMaskRender();
        try {
            MultiBufferSource bufferSource = outlineBuffer;
            for (LivingEntity entity : entities) {
                int color = ChamsGlow.argbColor(entity);
                int alpha = (color >>> 24) & 0xFF;
                if (alpha <= 0) {
                    continue;
                }
                outlineBuffer.setColor((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, 255);
                double x = entity.xOld + (entity.getX() - entity.xOld) * partialTick - camera.getPosition().x;
                double y = entity.yOld + (entity.getY() - entity.yOld) * partialTick - camera.getPosition().y;
                double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTick - camera.getPosition().z;
                float yaw = (float) lerp(entity.yRotO, entity.getYRot(), partialTick);
                dispatcher.render(entity, x, y, z, yaw, partialTick,
                        worldPoseStack, bufferSource, LightTexture.FULL_BRIGHT);
            }
            minecraft.renderBuffers().bufferSource().endBatch();
            outlineBuffer.endOutlineBatch();
            entityEffect.process(partialTick);
        } finally {
            ChamsRenderContext.endOutlineMaskRender();
            minecraft.getMainRenderTarget().bindWrite(false);
            setEntityOutlineOpacity(entityEffect, 1.0f);
        }
    }

    public static Matrix4f cameraRelativeMatrix(Matrix4f source) {
        Matrix4f matrix = new Matrix4f(source);
        matrix.m30(0.0f);
        matrix.m31(0.0f);
        matrix.m32(0.0f);
        return matrix;
    }

    public static void prepareWorldOverlayRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void setEntityOutlineOpacity(PostChain entityEffect, float opacity) {
        if (!(entityEffect instanceof PostChainAccessor accessor)) {
            return;
        }
        for (PostPass pass : accessor.xerion$getPasses()) {
            if (pass instanceof PostPassAccessor passAccessor) {
                var uniform = passAccessor.xerion$getEffect().getUniform("uOpacity");
                if (uniform != null) {
                    uniform.set(opacity);
                }
            }
        }
    }

    public static float mappedEntityOutlineOpacity(int alpha) {
        float normalized = Math.max(0.0f, Math.min(1.0f, alpha / 255.0f));
        return (float) Math.pow(normalized, 2.2D);
    }

    private static double modelGlowRange() {
        double range = 0.0D;
        if (VisualSettings.visualEspEnabled() && VisualSettings.visualGlow()) {
            range = Math.max(range, VisualSettings.visualEnemyRange());
        }
        if (VisualSettings.visualTeamEnabled() && VisualSettings.visualTeamGlow()) {
            range = Math.max(range, VisualSettings.visualTeamRange());
        }
        if (VisualSettings.visualLocalEnabled() && VisualSettings.visualLocalGlow()) {
            range = Math.max(range, VisualSettings.visualLocalRange());
        }
        return Math.max(1.0D, range);
    }

    private static boolean withinModelGlowRange(LocalPlayer player, LivingEntity entity) {
        double range = switch (ChamsProfileResolver.profile(entity)) {
            case ENEMY -> VisualSettings.visualEnemyRange();
            case TEAM -> VisualSettings.visualTeamRange();
            case LOCAL -> VisualSettings.visualLocalRange();
            case NONE -> 0.0D;
        };
        return range > 0.0D && player.distanceToSqr(entity) <= range * range;
    }

    private static double lerp(double start, double end, float partialTick) {
        return start + (end - start) * partialTick;
    }
}
