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

import com.xerion.forge.chams.LocalVisualChams;
import com.xerion.forge.config.VisualSettings;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    private boolean xerion$blockingAnimation;

    @Shadow
    protected abstract void m_109335_(PoseStack poseStack, HumanoidArm arm, float swingProgress);

    @Inject(method = "m_109371_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$cancelArmWithItemDuringZoom(AbstractClientPlayer player,
                                                               float partialTicks,
                                                               float pitch,
                                                               InteractionHand hand,
                                                               float swingProgress,
                                                               ItemStack stack,
                                                               float equipProgress,
                                                               PoseStack poseStack,
                                                               MultiBufferSource buffer,
                                                               int light,
                                                               CallbackInfo ci) {
        if (!ZoomState.shouldRenderHands()) {
            xerion$blockingAnimation = false;
            ci.cancel();
        }
    }

    @Inject(method = "m_109371_", at = @At("HEAD"), require = 0)
    private void xerion$cacheKillAuraBlockAnimation(AbstractClientPlayer player,
                                                               float partialTicks,
                                                               float pitch,
                                                               InteractionHand hand,
                                                               float swingProgress,
                                                               ItemStack stack,
                                                               float equipProgress,
                                                               PoseStack poseStack,
                                                               MultiBufferSource buffer,
                                                               int light,
                                                               CallbackInfo ci) {
        KillAuraBlockAnimation.renderHook(hand, stack);
        xerion$blockingAnimation = KillAuraBlockAnimation.shouldApply(hand, stack);
    }

    @Inject(
            method = "m_109371_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;m_269530_(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            ),
            require = 0
    )
    private void xerion$applyKillAuraBlockAnimation(AbstractClientPlayer player,
                                                               float partialTicks,
                                                               float pitch,
                                                               InteractionHand hand,
                                                               float swingProgress,
                                                               ItemStack stack,
                                                               float equipProgress,
                                                               PoseStack poseStack,
                                                               MultiBufferSource buffer,
                                                               int light,
                                                               CallbackInfo ci) {
        xerion$applyViewModelTransform(poseStack);
        if (!xerion$blockingAnimation) {
            return;
        }
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        KillAuraBlockAnimation.apply(poseStack, arm, swingProgress);
    }

    private void xerion$applyViewModelTransform(PoseStack poseStack) {
        if (!VisualSettings.viewModelEnabled()) {
            return;
        }
        poseStack.translate(VisualSettings.viewModelOffsetX(),
                VisualSettings.viewModelOffsetY(),
                VisualSettings.viewModelOffsetZ());
        float scale = VisualSettings.viewModelScale();
        poseStack.scale(scale, scale, scale);
    }

    @Inject(method = "m_109371_", at = @At("RETURN"), require = 0)
    private void xerion$clearKillAuraBlockAnimation(AbstractClientPlayer player,
                                                               float partialTicks,
                                                               float pitch,
                                                               InteractionHand hand,
                                                               float swingProgress,
                                                               ItemStack stack,
                                                               float equipProgress,
                                                               PoseStack poseStack,
                                                               MultiBufferSource buffer,
                                                               int light,
                                                               CallbackInfo ci) {
        xerion$blockingAnimation = false;
    }

    @Redirect(
            method = "m_109371_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;m_109335_(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"
            ),
            require = 0
    )
    private void xerion$skipVanillaSwingWhenBlocking(ItemInHandRenderer renderer,
                                                                PoseStack poseStack,
                                                                HumanoidArm arm,
                                                                float swingProgress) {
        if (!xerion$blockingAnimation) {
            m_109335_(poseStack, arm, swingProgress);
        }
    }

    @ModifyArg(
            method = "m_109371_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;m_269530_(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            ),
            index = 5,
            require = 0
    )
    private MultiBufferSource xerion$replaceLocalItemBuffer(MultiBufferSource buffer) {
        return LocalVisualChams.itemBuffer(buffer);
    }

    @Inject(
            method = "m_109346_",
            at = @At("HEAD"),
            require = 0
    )
    private void xerion$beginLocalArmChams(PoseStack poseStack,
                                                      MultiBufferSource buffer,
                                                      int light,
                                                      float equipProgress,
                                                      float swingProgress,
                                                      HumanoidArm arm,
                                                      CallbackInfo ci) {
        LocalVisualChams.beginFirstPersonArms();
    }

    @Inject(
            method = "m_109346_",
            at = @At("RETURN"),
            require = 0
    )
    private void xerion$endLocalArmChams(PoseStack poseStack,
                                                    MultiBufferSource buffer,
                                                    int light,
                                                    float equipProgress,
                                                    float swingProgress,
                                                    HumanoidArm arm,
                                                    CallbackInfo ci) {
        LocalVisualChams.endFirstPersonArms();
    }

    @Inject(
            method = "m_109361_",
            at = @At("HEAD"),
            require = 0
    )
    private void xerion$beginLocalMapHandChams(PoseStack poseStack,
                                                         MultiBufferSource buffer,
                                                         int light,
                                                         HumanoidArm arm,
                                                         CallbackInfo ci) {
        LocalVisualChams.beginFirstPersonArms();
    }

    @Inject(
            method = "m_109361_",
            at = @At("RETURN"),
            require = 0
    )
    private void xerion$endLocalMapHandChams(PoseStack poseStack,
                                                       MultiBufferSource buffer,
                                                       int light,
                                                       HumanoidArm arm,
                                                       CallbackInfo ci) {
        LocalVisualChams.endFirstPersonArms();
    }
}
