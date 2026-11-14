package com.xerion.forge.mixin;

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
import com.xerion.forge.chams.ChamsProfileResolver;
import com.xerion.common.visual.ChamsMaterial;
import com.xerion.common.visual.ChamsRenderContext;
import com.xerion.forge.chams.ChamsRenderTypes;
import com.xerion.forge.chams.LocalVisualChams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {
    private static final ThreadLocal<LivingEntity> MULTILOADERSETTINGS_RENDER_ENTITY = new ThreadLocal<>();

    @Shadow
    protected EntityModel<LivingEntity> f_115290_;

    @Inject(method = "m_7392_(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), require = 0)
    private void xerion$beginRenderEntity(LivingEntity entity,
                                                     float entityYaw,
                                                     float partialTick,
                                                     PoseStack poseStack,
                                                     MultiBufferSource bufferSource,
                                                     int packedLight,
                                                     CallbackInfo ci) {
        MULTILOADERSETTINGS_RENDER_ENTITY.set(entity);
    }

    @Inject(method = "m_7392_(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("RETURN"), require = 0)
    private void xerion$endRenderEntity(LivingEntity entity,
                                                   float entityYaw,
                                                   float partialTick,
                                                   PoseStack poseStack,
                                                   MultiBufferSource bufferSource,
                                                   int packedLight,
                                                   CallbackInfo ci) {
        MULTILOADERSETTINGS_RENDER_ENTITY.remove();
    }

    @Inject(method = "m_7392_(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;m_7225_(Lnet/minecraft/world/entity/LivingEntity;ZZZ)Lnet/minecraft/client/renderer/RenderType;"),
            require = 0)
    private void xerion$renderBehindWallsBeforeVisible(LivingEntity entity,
                                                                  float entityYaw,
                                                                  float partialTick,
                                                                  PoseStack poseStack,
                                                                  MultiBufferSource bufferSource,
                                                                  int packedLight,
                                                                  CallbackInfo ci) {
        if (entity == null
                || entity.isSpectator()
                || ChamsRenderContext.outlineMaskRender()
                || shouldSkipWorldSelf(entity)) {
            return;
        }

        ChamsProfileResolver.Profile profile = ChamsProfileResolver.profile(entity);
        if (!profile.throughWallsEnabled()) {
            return;
        }

        int color = profile.throughWallsColor();
        float alpha = ((color >>> 24) & 0xFF) / 255.0f;
        if (alpha <= 0.01f) {
            return;
        }

        ChamsMaterial material = profile.throughWallsMaterial();
        if (material == null) {
            return;
        }
        ResourceLocation texture = entityTexture(entity);
        var consumer = bufferSource.getBuffer(ChamsRenderTypes.throughWalls(texture, material, color));
        int light = ChamsRenderTypes.fullBright(material) ? net.minecraft.client.renderer.LightTexture.FULL_BRIGHT : packedLight;
        f_115290_.renderToBuffer(poseStack, consumer, light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Inject(method = "m_7225_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$replaceVisibleModelMaterial(LivingEntity entity,
                                                               boolean bodyVisible,
                                                               boolean translucent,
                                                               boolean glowing,
                                                               CallbackInfoReturnable<RenderType> cir) {
        if (entity == null
                || entity.isSpectator()
                || shouldSkipWorldSelf(entity)) {
            return;
        }

        if (ChamsRenderContext.outlineMaskRender()) {
            cir.setReturnValue(RenderType.outline(entityTexture(entity)));
            return;
        }

        if (glowing) {
            return;
        }

        if (ChamsRenderContext.previewGlowRender()) {
            ResourceLocation texture = entityTexture(entity);
            cir.setReturnValue(ChamsRenderTypes.overlay(texture, ChamsMaterial.GLOW, ChamsRenderContext.previewGlowColor()));
            return;
        }

        ChamsProfileResolver.Profile profile = ChamsProfileResolver.profile(entity);
        ChamsMaterial material = profile.visibleMaterial(entity);
        if (material == null) {
            if (entity instanceof Player player && LocalVisualChams.shouldApplyLocalTransparency(player)) {
                cir.setReturnValue(RenderType.entityTranslucent(entityTexture(entity)));
            }
            return;
        }

        ResourceLocation texture = entityTexture(entity);
        cir.setReturnValue(ChamsRenderTypes.visible(texture, material, profile.visibleColor(entity)));
    }

    @ModifyArg(method = "m_7392_(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;m_7695_(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),
            index = 7,
            require = 0)
    private float xerion$localTransparencyAlpha(float original) {
        LivingEntity entity = MULTILOADERSETTINGS_RENDER_ENTITY.get();
        if (entity instanceof Player player && LocalVisualChams.shouldApplyLocalTransparency(player)) {
            return LocalVisualChams.localTransparencyAlphaFloat();
        }
        return original;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ResourceLocation entityTexture(LivingEntity entity) {
        return ((LivingEntityRenderer) (Object) this).getTextureLocation(entity);
    }

    private static boolean shouldSkipWorldSelf(LivingEntity entity) {
        return !ChamsRenderContext.previewRender()
                && entity == Minecraft.getInstance().player
                && !VisualSettings.visualLocalPlayerChams();
    }
}

