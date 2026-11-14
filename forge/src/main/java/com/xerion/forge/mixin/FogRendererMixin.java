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
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
    @Inject(method = "m_234172_", at = @At("HEAD"), cancellable = true, require = 0)
    private static void xerion$applyNoFog(Camera camera, FogRenderer.FogMode fogMode,
                                                     float renderDistance, boolean thickFog,
                                                     float partialTick, CallbackInfo ci) {
        if (camera.getEntity() instanceof LivingEntity livingEntity
                && AntiBlindnessService.hideBlindnessOrDarkness(livingEntity)) {
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
            RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            ci.cancel();
            return;
        }
        if (!VisualSettings.noFogEnabled() || !shouldRemoveFog(camera.getFluidInCamera())) {
            return;
        }
        RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
        RenderSystem.setShaderFogShape(FogShape.CYLINDER);
        ci.cancel();
    }

    private static boolean shouldRemoveFog(FogType fogType) {
        return switch (fogType) {
            case WATER -> VisualSettings.noFogWater();
            case LAVA -> VisualSettings.noFogLava();
            case POWDER_SNOW -> VisualSettings.noFogPowderSnow();
            case NONE -> VisualSettings.noFogDistance();
        };
    }
}
