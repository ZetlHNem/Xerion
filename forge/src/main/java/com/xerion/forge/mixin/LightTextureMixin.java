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
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
abstract class LightTextureMixin {
    @Final
    @Shadow
    private NativeImage f_109871_;

    @Inject(method = "m_109881_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;m_117985_()V"),
            require = 0)
    private void xerion$applyFullbrightLightTexture(float partialTick, CallbackInfo ci) {
        if (!VisualSettings.fullbrightEnabled() || !"Gamma".equals(VisualSettings.fullbrightMode())) {
            return;
        }
        float strength = Math.max(0.0f, Math.min(1.0f, VisualSettings.fullbrightStrength()));
        if (strength <= 0.0f) {
            return;
        }
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                int color = f_109871_.getPixelRGBA(x, y);
                f_109871_.setPixelRGBA(x, y, brighten(color, strength));
            }
        }
    }

    @Inject(method = "m_234319_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$hideDarknessGamma(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (AmbienceService.hideDarknessPulse() || AntiBlindnessService.hideEffect(MobEffects.DARKNESS)) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "m_234312_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$hideDarknessScale(LivingEntity entity, float darknessGamma, float partialTick,
                                                     CallbackInfoReturnable<Float> cir) {
        if (AmbienceService.hideDarknessPulse() || AntiBlindnessService.hideEffect(MobEffects.DARKNESS)) {
            cir.setReturnValue(0.0f);
        }
    }

    private static int brighten(int color, float strength) {
        int alpha = color & 0xFF000000;
        int red = color & 0x000000FF;
        int green = color & 0x0000FF00;
        int blue = color & 0x00FF0000;
        red += Math.round((0x000000FF - red) * strength);
        green += Math.round((0x0000FF00 - green) * strength);
        blue += Math.round((0x00FF0000 - blue) * strength);
        return alpha | (blue & 0x00FF0000) | (green & 0x0000FF00) | (red & 0x000000FF);
    }
}
