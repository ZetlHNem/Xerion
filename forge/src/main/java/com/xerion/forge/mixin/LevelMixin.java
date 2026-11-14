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

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
abstract class LevelMixin {
    @Inject(method = "m_46722_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$hideRainLevel(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (NoWeatherService.hideAllPrecipitation()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "m_46661_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$hideThunderLevel(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (NoWeatherService.hideThunder()) {
            cir.setReturnValue(0.0f);
        }
    }

    @Inject(method = "m_46468_", at = @At("RETURN"), cancellable = true, require = 0)
    private void xerion$timeChangerDayTime(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(TimeChangerService.dayTime(cir.getReturnValue()));
    }
}
