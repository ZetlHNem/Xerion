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
import com.xerion.forge.chams.ChamsGlow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityMixin {
    private static final double SAFE_WALK_STEP = 0.05D;
    private static final double SAFE_WALK_GROUND_PROBE = 0.08D;

    @Inject(method = "m_19876_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$chamsOutlineColor(CallbackInfoReturnable<Integer> cir) {
        Entity entity = (Entity) (Object) this;
        if (ChamsGlow.shouldGlow(entity)) {
            cir.setReturnValue(ChamsGlow.color(entity));
        }
    }

    @Inject(method = "m_19884_", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$freecamTurn(double yawDelta, double pitchDelta, CallbackInfo ci) {
        if (FreecamService.handleTurn((Entity) (Object) this, yawDelta, pitchDelta)) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "m_6478_", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private Vec3 xerion$safeWalkMove(Vec3 movement, MoverType moverType) {
        Entity entity = (Entity) (Object) this;
        if (!(entity instanceof LocalPlayer player)) {
            return movement;
        }
        boolean active = safeWalkActive(player, moverType);
        if (!active || movement.x == 0.0D && movement.z == 0.0D) {
            return movement;
        }

        Vec3 adjusted = safeWalkMovement(player, movement);
        return adjusted;
    }

    @ModifyVariable(method = "m_7601_(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 1, require = 0)
    private Vec3 xerion$fastWebMotion(Vec3 motion, BlockState state) {
        return FastWebService.modifyStuckMotion(state, motion);
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

    private static boolean safeWalkActive(LocalPlayer player, MoverType moverType) {
        if (!VisualSettings.safeWalkEnabled()
                || !"Freeze".equals(VisualSettings.safeWalkMode())) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.screen == null
                && !minecraft.options.keyJump.isDown()
                && (moverType == MoverType.SELF || moverType == MoverType.PLAYER)
                && player.onGround()
                && !player.isPassenger()
                && !player.isSpectator()
                && !player.getAbilities().flying;
    }

}
