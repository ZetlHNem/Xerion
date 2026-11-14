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

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class GuiMixin {
    @Inject(method = "m_280155_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$hidePumpkinOverlay(GuiGraphics graphics, ResourceLocation texture, float alpha,
                                                      CallbackInfo ci) {
        if (AmbienceService.hidePumpkinOverlay(texture)) {
            ci.cancel();
        }
    }

    @Inject(method = "m_280379_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$hidePortalOverlay(GuiGraphics graphics, float alpha, CallbackInfo ci) {
        if (AmbienceService.hidePortalOverlay()) {
            ci.cancel();
        }
    }
}
