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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
abstract class KeyboardInputMixin {
    @Inject(method = "m_214106_", at = @At("TAIL"), require = 0)
    private void xerion$scaffoldTellyJump(boolean slowDown, float movementMultiplier, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Input input = (Input) (Object) this;
        applyScreenMovementInput(minecraft);
        FreecamService.suppressPlayerInput(input);

        if (VisualSettings.noSlowEnabled()
                && slowDown
                && movementMultiplier > 1.0E-5F
                && player != null
                && player.isUsingItem()
                && !player.isPassenger()
                && !player.isSpectator()) {
            input.forwardImpulse /= movementMultiplier;
            input.leftImpulse /= movementMultiplier;
        }

        if (!VisualSettings.scaffoldEnabled() || !"TellyBridge".equals(VisualSettings.scaffoldMode())) {
            return;
        }

        if (player == null || minecraft.screen != null || player.isPassenger() || player.isSpectator() || player.getAbilities().flying) {
            return;
        }
        if (!player.onGround() || minecraft.options.keyJump.isDown()) {
            return;
        }
        if (Math.abs(input.forwardImpulse) > 1.0E-5F || Math.abs(input.leftImpulse) > 1.0E-5F) {
            input.jumping = true;
        }
    }

    private void applyScreenMovementInput(Minecraft minecraft) {
        MenuMovementService.applyToInput(minecraft, (Input) (Object) this);
    }
}
