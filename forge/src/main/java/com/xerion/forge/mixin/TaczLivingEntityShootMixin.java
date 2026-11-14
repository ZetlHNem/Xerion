package com.xerion.forge.mixin;

import com.xerion.forge.combat.TaczRagebotHooks;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Pseudo
@Mixin(targets = "com.tacz.guns.entity.shooter.LivingEntityShoot", remap = false)
abstract class TaczLivingEntityShootMixin {
    @Shadow(remap = false)
    @Final
    private LivingEntity shooter;

    @Shadow(remap = false)
    @Final
    private ShooterDataHolder data;

    private boolean xerion$doubleTapBypass;
    private boolean xerion$wasBolting;
    private long xerion$previousBoltTimestamp;
    private TaczRagebotHooks.ServerShotAim xerion$serverShotAim;

    @ModifyVariable(method = "shoot(Ljava/util/function/Supplier;Ljava/util/function/Supplier;JFZ)Lcom/tacz/guns/api/entity/ShootResult;",
            at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0, remap = false)
    private Supplier<Float> xerion$useRealShotPitch(Supplier<Float> original) {
        TaczRagebotHooks.ServerShotAim shotAim = xerion$getServerShotAim();
        return shotAim == null ? original : shotAim::pitch;
    }

    @ModifyVariable(method = "shoot(Ljava/util/function/Supplier;Ljava/util/function/Supplier;JFZ)Lcom/tacz/guns/api/entity/ShootResult;",
            at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0, remap = false)
    private Supplier<Float> xerion$useRealShotYaw(Supplier<Float> original) {
        TaczRagebotHooks.ServerShotAim shotAim = xerion$getServerShotAim();
        return shotAim == null ? original : shotAim::yaw;
    }

    private TaczRagebotHooks.ServerShotAim xerion$getServerShotAim() {
        if (xerion$serverShotAim == null) {
            xerion$serverShotAim = TaczRagebotHooks.beginServerShotAim(shooter);
        }
        return xerion$serverShotAim;
    }

    @Inject(method = "shoot(Ljava/util/function/Supplier;Ljava/util/function/Supplier;JFZ)Lcom/tacz/guns/api/entity/ShootResult;",
            at = @At("HEAD"), require = 0, remap = false)
    private void xerion$beginMarkedDoubleTapShot(java.util.function.Supplier<Float> pitch,
                                                  java.util.function.Supplier<Float> yaw,
                                                  long timestamp, float chargeProgress,
                                                  boolean checkCharge, CallbackInfoReturnable<?> cir) {
        xerion$getServerShotAim();
        xerion$doubleTapBypass = TaczRagebotHooks.consumeDoubleTapCooldownBypass(shooter, timestamp);
        if (xerion$doubleTapBypass) {
            xerion$wasBolting = data.isBolting;
            xerion$previousBoltTimestamp = data.boltTimestamp;
            data.isBolting = false;
        }
    }

    @Inject(method = "getShootCoolDown(J)J", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void xerion$allowMarkedDoubleTapShot(long timestamp, CallbackInfoReturnable<Long> cir) {
        if (xerion$doubleTapBypass) {
            cir.setReturnValue(0L);
        }
    }

    @Inject(method = "shoot(Ljava/util/function/Supplier;Ljava/util/function/Supplier;JFZ)Lcom/tacz/guns/api/entity/ShootResult;",
            at = @At("RETURN"), require = 0, remap = false)
    private void xerion$finishMarkedDoubleTapShot(java.util.function.Supplier<Float> pitch,
                                                   java.util.function.Supplier<Float> yaw,
                                                   long timestamp, float chargeProgress,
                                                   boolean checkCharge, CallbackInfoReturnable<?> cir) {
        if (xerion$doubleTapBypass) {
            Object result = cir.getReturnValue();
            if (result != null && "SUCCESS".equals(result.toString())) {
                data.shootTimestamp = Math.max(0L, System.currentTimeMillis() - data.baseTimestamp);
            }
            data.isBolting = xerion$wasBolting;
            data.boltTimestamp = xerion$previousBoltTimestamp;
            xerion$doubleTapBypass = false;
        }
        xerion$serverShotAim = null;
    }
}
