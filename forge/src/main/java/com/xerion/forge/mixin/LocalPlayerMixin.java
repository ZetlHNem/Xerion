package com.xerion.forge.mixin;

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

import com.xerion.forge.config.VisualSettings;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Shadow
    private float f_108589_;

    @Shadow
    private float f_108590_;

    @Inject(method = "m_108733_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$allowOmniSprintStart(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        var input = player.input;
        if (!VisualSettings.keepSprintEnabled() || input == null) {
            return;
        }

        cir.setReturnValue(Math.abs(input.forwardImpulse) > 1.0E-5f || Math.abs(input.leftImpulse) > 1.0E-5f);
    }

    @ModifyConstant(method = "m_8107_", constant = @Constant(floatValue = 0.2F), require = 0)
    private float xerion$removeUseItemSlowdown(float value) {
        return NoSlowService.canCancelSlowdown() ? 1.0F : value;
    }

    @Inject(method = "m_8119_", at = @At("RETURN"), require = 0)
    private void xerion$clearNauseaSpinOnTick(CallbackInfo ci) {
        xerion$clearNauseaSpin();
    }

    @Inject(method = "m_8107_", at = @At("RETURN"), require = 0)
    private void xerion$clearNauseaSpinOnAiStep(CallbackInfo ci) {
        xerion$clearNauseaSpin();
    }

    @Redirect(method = "m_108640_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;m_146908_()F"), require = 0)
    private float xerion$redirectScaffoldYaw(LocalPlayer player) {
        return ScaffoldRotation.active() && ScaffoldRotation.yawActive() ? ScaffoldRotation.yaw() : player.getYRot();
    }

    @Redirect(method = "m_108640_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;m_146909_()F"), require = 0)
    private float xerion$redirectScaffoldPitch(LocalPlayer player) {
        return ScaffoldRotation.active() ? ScaffoldRotation.pitch() : player.getXRot();
    }

    @Inject(method = "m_108640_", at = @At("RETURN"), require = 0)
    private void xerion$consumeScaffoldPacketRotation(CallbackInfo ci) {
        ScaffoldRotation.consumeSend();
    }

    private void xerion$clearNauseaSpin() {
        if (AntiBlindnessService.hideNausea()) {
            f_108589_ = 0.0f;
            f_108590_ = 0.0f;
        }
    }
}
