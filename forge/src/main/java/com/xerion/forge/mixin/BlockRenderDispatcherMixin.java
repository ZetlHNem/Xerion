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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderDispatcher.class)
abstract class BlockRenderDispatcherMixin {
    private static final ThreadLocal<Boolean> MULTILOADERSETTINGS_XRAY_SELECTED_PASS =
            ThreadLocal.withInitial(() -> false);

    @Inject(method = "renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;Lnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void xerion$xrayRenderBatched(BlockState state, BlockPos pos, BlockAndTintGetter level,
                                                     PoseStack poseStack, VertexConsumer consumer,
                                                     boolean checkSides, RandomSource random,
                                                     ModelData modelData, RenderType renderType,
                                                     CallbackInfo ci) {
        if (!XrayService.enabled() || MULTILOADERSETTINGS_XRAY_SELECTED_PASS.get()) {
            return;
        }
        if (!XrayService.shouldRenderBlock(state)) {
            ci.cancel();
            return;
        }
        if (!VisualSettings.xrayExposedOnly() && checkSides) {
            ci.cancel();
            MULTILOADERSETTINGS_XRAY_SELECTED_PASS.set(true);
            try {
                ((BlockRenderDispatcher) (Object) this).renderBatched(state, pos, level, poseStack,
                        consumer, false, random, modelData, renderType);
            } finally {
                MULTILOADERSETTINGS_XRAY_SELECTED_PASS.set(false);
            }
        }
    }
}
