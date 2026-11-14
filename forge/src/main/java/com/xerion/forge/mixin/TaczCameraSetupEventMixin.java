package com.xerion.forge.mixin;

import com.xerion.forge.combat.TaczRagebotHooks;
import com.tacz.guns.api.event.common.GunFireEvent;
import net.minecraftforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.client.event.CameraSetupEvent", remap = false)
abstract class TaczCameraSetupEventMixin {
    @Inject(method = "initialCameraRecoil", at = @At("HEAD"), cancellable = true, require = 0)
    private static void xerion$skipInitialRagebotRecoil(GunFireEvent event, CallbackInfo ci) {
        if (TaczRagebotHooks.removeRecoilActive(event.getShooter())) {
            ci.cancel();
        }
    }

    @Inject(method = "applyCameraRecoil", at = @At("HEAD"), cancellable = true, require = 0)
    private static void xerion$skipActiveRagebotRecoil(ViewportEvent.ComputeCameraAngles event, CallbackInfo ci) {
        if (TaczRagebotHooks.removeRecoilActive()) {
            ci.cancel();
        }
    }
}
