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
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer", remap = false)
abstract class EmbeddiumBlockRendererMixin {
    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$xrayRenderModel(BlockRenderContext context, ChunkBuildBuffers buffers,
                                                   CallbackInfo ci) {
        if (context != null && XrayService.enabled()
                && !XrayService.shouldRenderBlock(context.localSlice(), context.pos(), context.state())) {
            ci.cancel();
        }
    }

    @Inject(method = "isFaceVisible", at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$xrayRenderAllSelectedFaces(BlockRenderContext context, Direction direction,
                                                    CallbackInfoReturnable<Boolean> cir) {
        if (context != null && XrayService.enabled()
                && !VisualSettings.xrayExposedOnly()
                && XrayService.shouldRenderBlock(context.state())) {
            cir.setReturnValue(true);
        }
    }
}
