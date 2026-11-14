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
import com.xerion.common.visual.ChamsMaterial;
import com.xerion.common.visual.ChamsRenderContext;
import com.xerion.forge.chams.ChamsRenderTypes;
import com.xerion.forge.chams.ChamsProfileResolver;
import com.xerion.forge.chams.LocalVisualChams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderLayer.class)
abstract class RenderLayerMixin {
    private static final ThreadLocal<LivingEntity> MULTILOADERSETTINGS_LAYER_ENTITY = new ThreadLocal<>();
    private static final ThreadLocal<ResourceLocation> MULTILOADERSETTINGS_LAYER_TEXTURE = new ThreadLocal<>();

    @ModifyArg(method = "m_117376_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;m_6299_(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
            index = 0, require = 0)
    private static RenderType xerion$replaceLayerRenderType(RenderType original) {
        LivingEntity entity = MULTILOADERSETTINGS_LAYER_ENTITY.get();
        if (entity == null
                || entity.isSpectator()
                || shouldSkipWorldSelf(entity)) {
            return original;
        }

        ResourceLocation texture = MULTILOADERSETTINGS_LAYER_TEXTURE.get();
        if (texture == null) {
            return original;
        }
        if (ChamsRenderContext.outlineMaskRender()) {
            return RenderType.outline(texture);
        }
        if (ChamsRenderContext.previewGlowRender()) {
            return ChamsRenderTypes.overlay(texture, ChamsMaterial.GLOW, ChamsRenderContext.previewGlowColor());
        }

        ChamsProfileResolver.Profile profile = ChamsProfileResolver.profile(entity);
        ChamsMaterial material = profile.visibleMaterial(entity);
        if (material == null) {
            if (entity instanceof Player player && LocalVisualChams.shouldApplyLocalTransparency(player)) {
                return RenderType.entityTranslucent(texture);
            }
            return original;
        }
        return ChamsRenderTypes.visible(texture, material, profile.visibleColor(entity));
    }

    @ModifyArg(method = "m_117376_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;m_7695_(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),
            index = 7, require = 0)
    private static float xerion$localTransparencyLayerAlpha(float original) {
        LivingEntity entity = MULTILOADERSETTINGS_LAYER_ENTITY.get();
        if (entity instanceof Player player && LocalVisualChams.shouldApplyLocalTransparency(player)) {
            return LocalVisualChams.localTransparencyAlphaFloat();
        }
        return original;
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "m_117376_", at = @At("HEAD"), require = 0)
    private static void xerion$beginLayerEntity(net.minecraft.client.model.EntityModel<?> model,
                                                           ResourceLocation texture,
                                                           com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                           net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                                           int packedLight,
                                                           LivingEntity entity,
                                                           float red,
                                                           float green,
                                                           float blue,
                                                           org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        MULTILOADERSETTINGS_LAYER_ENTITY.set(entity);
        MULTILOADERSETTINGS_LAYER_TEXTURE.set(texture);
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "m_117376_", at = @At("RETURN"), require = 0)
    private static void xerion$endLayerEntity(net.minecraft.client.model.EntityModel<?> model,
                                                         ResourceLocation texture,
                                                         com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                         net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                                         int packedLight,
                                                         LivingEntity entity,
                                                         float red,
                                                         float green,
                                                         float blue,
                                                         org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        MULTILOADERSETTINGS_LAYER_ENTITY.remove();
        MULTILOADERSETTINGS_LAYER_TEXTURE.remove();
    }

    private static boolean shouldSkipWorldSelf(LivingEntity entity) {
        return !ChamsRenderContext.previewRender()
                && entity == Minecraft.getInstance().player
                && !VisualSettings.visualLocalPlayerChams();
    }
}
