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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
abstract class PlayerMixin {
    private static final double SAFE_WALK_STEP = 0.05D;
    private static final double SAFE_WALK_GROUND_PROBE = 0.08D;

    @Shadow
    protected abstract boolean m_36343_();

    @Shadow
    private boolean m_36386_() {
        throw new AssertionError();
    }

    @Inject(method = "m_5763_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$safeWalk(Vec3 movement, MoverType moverType, CallbackInfoReturnable<Vec3> cir) {
        Player player = (Player) (Object) this;
        if (!(player instanceof LocalPlayer localPlayer)) {
            return;
        }
        if (!VisualSettings.safeWalkEnabled()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.options.keyJump.isDown()) {
            return;
        }
        if ((moverType != MoverType.SELF && moverType != MoverType.PLAYER)
                || !localPlayer.onGround() || localPlayer.isPassenger()
                || localPlayer.isSpectator() || localPlayer.getAbilities().flying) {
            return;
        }
        if (movement.x == 0.0D && movement.z == 0.0D) {
            return;
        }

        Vec3 adjusted = safeWalkMovement(localPlayer, movement);
        if (adjusted.x != movement.x || adjusted.z != movement.z) {
            cir.setReturnValue(adjusted);
        }
    }

    @Inject(method = "m_5706_", at = @At("RETURN"), require = 0)
    private void xerion$keepSprintAfterAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!(player instanceof LocalPlayer localPlayer)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != localPlayer || !KeepSprintService.shouldKeepSprintAfterAttack(minecraft, localPlayer)) {
            return;
        }

        if (!localPlayer.isSprinting()) {
            localPlayer.setSprinting(true);
        }
        double slowdownPercent = Math.max(0.0D, Math.min(100.0D, VisualSettings.keepSprintSlowdown())) / 100.0D;
        if (slowdownPercent <= 0.0D) {
            return;
        }
        double factor = 0.6D + 0.4D * (1.0D - slowdownPercent);
        localPlayer.setDeltaMovement(localPlayer.getDeltaMovement().multiply(factor, 1.0D, factor));
    }

    @Inject(method = "m_36399_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$preventFoodExhaustion(float exhaustion, CallbackInfo ci) {
        if (VisualSettings.antiHungerEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "m_36281_(Lnet/minecraft/world/level/block/state/BlockState;)F",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void xerion$fastBreakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        if ((Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(FastBreakService.modifyDestroySpeed(cir.getReturnValueF()));
        }
    }

    @Redirect(method = "m_5763_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_36343_()Z"),
            require = 0)
    private boolean xerion$safeWalkStayingOnGroundSurface(Player player) {
        if (safeWalkActive(player)) {
            return true;
        }
        boolean result = m_36343_();
        return result;
    }

    @Redirect(method = "m_5763_",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;m_36386_()Z"),
            require = 0)
    private boolean xerion$safeWalkAboveGround(Player player) {
        if (safeWalkActive(player)) {
            return true;
        }
        boolean result = m_36386_();
        return result;
    }

    private static Vec3 safeWalkMovement(LocalPlayer player, Vec3 movement) {
        AABB box = player.getBoundingBox();
        double x = movement.x;
        double z = movement.z;

        while (x != 0.0D && player.level().noCollision(player, box.move(x, -SAFE_WALK_GROUND_PROBE, 0.0D))) {
            x = approachZero(x);
        }
        while (z != 0.0D && player.level().noCollision(player, box.move(0.0D, -SAFE_WALK_GROUND_PROBE, z))) {
            z = approachZero(z);
        }
        while (x != 0.0D && z != 0.0D && player.level().noCollision(player, box.move(x, -SAFE_WALK_GROUND_PROBE, z))) {
            x = approachZero(x);
            z = approachZero(z);
        }

        return new Vec3(x, movement.y, z);
    }

    private static double approachZero(double value) {
        if (Math.abs(value) < SAFE_WALK_STEP) {
            return 0.0D;
        }
        return value > 0.0D ? value - SAFE_WALK_STEP : value + SAFE_WALK_STEP;
    }

    private static boolean safeWalkActive(Player player) {
        if (!(player instanceof LocalPlayer localPlayer)) {
            return false;
        }
        if (!VisualSettings.safeWalkEnabled()
                || !"Freeze".equals(VisualSettings.safeWalkMode())) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.screen == null
                && !minecraft.options.keyJump.isDown()
                && !localPlayer.isPassenger()
                && !localPlayer.isSpectator()
                && !localPlayer.getAbilities().flying;
    }
}
