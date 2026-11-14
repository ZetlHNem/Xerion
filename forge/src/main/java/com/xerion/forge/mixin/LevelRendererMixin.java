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

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Inject(method = "m_109703_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$cancelWeatherRender(LightTexture lightTexture, float partialTick,
                                                       double cameraX, double cameraY, double cameraZ,
                                                       CallbackInfo ci) {
        if (NoWeatherService.hideAllPrecipitation()) {
            ci.cancel();
        }
    }

    @Redirect(method = "m_109703_",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/biome/Biome;m_264600_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"),
            require = 0)
    private Biome.Precipitation xerion$filterWeatherRenderType(Biome biome, BlockPos pos) {
        Biome.Precipitation precipitation = biome.getPrecipitationAt(pos);
        if (precipitation == Biome.Precipitation.RAIN && NoWeatherService.hideRain()) {
            return Biome.Precipitation.NONE;
        }
        if (precipitation == Biome.Precipitation.SNOW && NoWeatherService.hideSnow()) {
            return Biome.Precipitation.NONE;
        }
        return precipitation;
    }

    @Inject(method = "m_109693_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$cancelWeatherParticlesAndSounds(Camera camera, CallbackInfo ci) {
        if (NoWeatherService.hideRain()) {
            ci.cancel();
        }
    }

    @Inject(method = "m_234310_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$allowSkyWithBlindness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        if (camera.getEntity() instanceof LivingEntity livingEntity
                && AntiBlindnessService.hideBlindnessOrDarkness(livingEntity)) {
            cir.setReturnValue(false);
        }
    }
}
