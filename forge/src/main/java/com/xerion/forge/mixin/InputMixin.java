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
import net.minecraft.client.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Input.class)
abstract class InputMixin {
    @Inject(method = "m_108577_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$allowOmniForwardImpulse(CallbackInfoReturnable<Boolean> cir) {
        if (!VisualSettings.keepSprintEnabled()) {
            return;
        }

        Input input = (Input) (Object) this;
        cir.setReturnValue(Math.abs(input.forwardImpulse) > 1.0E-5f || Math.abs(input.leftImpulse) > 1.0E-5f);
    }
}
