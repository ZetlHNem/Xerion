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

import com.xerion.forge.chams.ChamsGlow;
import com.xerion.common.visual.ChamsRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "m_91314_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$forceChamsOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (ChamsRenderContext.outlineMaskRender() && ChamsGlow.shouldGlow(entity)) {
            cir.setReturnValue(true);
        }
    }
}
