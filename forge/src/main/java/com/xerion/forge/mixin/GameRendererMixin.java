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
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.EntityHitResult;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "m_109087_", at = @At("RETURN"), require = 0)
    private void xerion$overrideCombatPick(float partialTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityHitResult result = CombatRaycast.pick(minecraft, partialTick);
        if (result != null) {
            minecraft.hitResult = result;
        }
    }

    @Inject(method = "m_109141_", at = @At("RETURN"), cancellable = true, require = 0)
    private void xerion$applyZoomFov(Camera camera, float partialTick, boolean useFovSetting,
                                               CallbackInfoReturnable<Double> cir) {
        if (ZoomState.rendering() && useFovSetting) {
            cir.setReturnValue(ZoomState.applyFov(cir.getReturnValueD()));
        }
    }

    @Inject(method = "m_280083_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$hideNauseaOverlay(GuiGraphics graphics, float strength, CallbackInfo ci) {
        if (AntiBlindnessService.hideNausea()) {
            ci.cancel();
        }
    }

    @Inject(method = "m_253088_", at = @At("RETURN"), require = 0)
    private void xerion$applyAspectRatio(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (!VisualSettings.aspectRatioEnabled()) {
            return;
        }
        Matrix4f projection = cir.getReturnValue();
        if (projection == null) {
            return;
        }
        float verticalScale = projection.m11();
        float aspect = VisualSettings.aspectRatioValue();
        if (verticalScale != 0.0f && aspect > 0.0f) {
            projection.m00(verticalScale / aspect);
        }
    }

    @Redirect(
            method = "m_253088_",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/Window;m_85441_()I"
            ),
            require = 0
    )
    private int xerion$aspectRatioProjectionWidth(Window window) {
        if (!VisualSettings.aspectRatioEnabled()) {
            return window.getWidth();
        }
        return Math.max(1, Math.round(window.getHeight() * VisualSettings.aspectRatioValue()));
    }
}
