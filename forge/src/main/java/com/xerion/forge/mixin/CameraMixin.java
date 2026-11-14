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

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {
    @Shadow
    protected abstract void m_90581_(Vec3 position);

    @Shadow
    protected abstract void m_90572_(float yaw, float pitch);

    @Inject(method = "m_90575_", at = @At("RETURN"), require = 0)
    private void xerion$applyFreecamPosition(BlockGetter level, Entity entity, boolean detached,
                                                       boolean thirdPersonReverse, float partialTick,
                                                       CallbackInfo ci) {
        Vec3 position = FreecamService.renderCameraPosition();
        if (position != null) {
            m_90581_(position);
            m_90572_(FreecamService.cameraYaw(), FreecamService.cameraPitch());
        }
    }
}
