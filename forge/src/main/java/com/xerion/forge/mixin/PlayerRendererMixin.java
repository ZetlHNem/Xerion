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
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "m_117818_", at = @At("RETURN"), require = 0)
    private void xerion$applyKillAuraBlockPose(AbstractClientPlayer player, CallbackInfo ci) {
        HumanoidModel<AbstractClientPlayer> model = ((PlayerRenderer) (Object) this).getModel();
        if (KillAuraBlockAnimation.shouldApplyThirdPerson(player, HumanoidArm.RIGHT)) {
            model.rightArmPose = HumanoidModel.ArmPose.BLOCK;
        }
        if (KillAuraBlockAnimation.shouldApplyThirdPerson(player, HumanoidArm.LEFT)) {
            model.leftArmPose = HumanoidModel.ArmPose.BLOCK;
        }
    }

    @ModifyArg(method = "m_117775_", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/MultiBufferSource;m_6299_(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
            index = 0, require = 0)
    private RenderType xerion$replaceFirstPersonArmRenderType(RenderType original) {
        if (Minecraft.getInstance().player == null) {
            return original;
        }
        return LocalVisualChams.replaceArmRenderType(original, Minecraft.getInstance().player.getSkinTextureLocation());
    }

    @Inject(method = "m_117770_", at = @At("HEAD"), require = 0)
    private void xerion$beginRightArmChams(PoseStack poseStack,
                                                     MultiBufferSource buffer,
                                                     int light,
                                                     AbstractClientPlayer player,
                                                     CallbackInfo ci) {
        LocalVisualChams.beginFirstPersonArms();
    }

    @Inject(method = "m_117770_", at = @At("RETURN"), require = 0)
    private void xerion$endRightArmChams(PoseStack poseStack,
                                                   MultiBufferSource buffer,
                                                   int light,
                                                   AbstractClientPlayer player,
                                                   CallbackInfo ci) {
        LocalVisualChams.endFirstPersonArms();
    }

    @Inject(method = "m_117813_", at = @At("HEAD"), require = 0)
    private void xerion$beginLeftArmChams(PoseStack poseStack,
                                                    MultiBufferSource buffer,
                                                    int light,
                                                    AbstractClientPlayer player,
                                                    CallbackInfo ci) {
        LocalVisualChams.beginFirstPersonArms();
    }

    @Inject(method = "m_117813_", at = @At("RETURN"), require = 0)
    private void xerion$endLeftArmChams(PoseStack poseStack,
                                                  MultiBufferSource buffer,
                                                  int light,
                                                  AbstractClientPlayer player,
                                                  CallbackInfo ci) {
        LocalVisualChams.endFirstPersonArms();
    }
}
