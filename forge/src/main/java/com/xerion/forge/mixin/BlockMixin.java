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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
abstract class BlockMixin {
    @Inject(method = "m_152444_", at = @At("HEAD"), cancellable = true)
    private static void xerion$xrayShouldRenderFace(BlockState state, BlockGetter level,
                                                               BlockPos pos, Direction direction,
                                                               BlockPos neighborPos,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (XrayService.enabled() && XrayService.shouldRenderBlock(state)) {
            cir.setReturnValue(XrayService.shouldRenderFace(level, pos, direction));
        }
    }
}
