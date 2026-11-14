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
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(BlockCollisions.class)
abstract class BlockCollisionsMixin {
    @Unique
    private Entity xerion$collisionEntity;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void xerion$captureCollisionEntity(CollisionGetter collisionGetter,
                                                          Entity entity,
                                                          AABB box,
                                                          boolean onlySuffocatingBlocks,
                                                          BiFunction<BlockPos.MutableBlockPos, VoxelShape, ?> resultProvider,
                                                          CallbackInfo ci) {
        this.xerion$collisionEntity = entity;
    }

    @Redirect(method = "computeNext",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/BlockGetter;m_8055_(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
            require = 0)
    private BlockState xerion$phaseNoClipCollision(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState state = blockGetter.getBlockState(blockPos);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isPassenger() || player.isSpectator() || player.getAbilities().flying) {
            return state;
        }
        Entity entity = this.xerion$collisionEntity;
        if (entity == null || !entity.getUUID().equals(player.getUUID())) {
            return state;
        }
        if (!VisualSettings.phaseEnabled()) {
            return state;
        }

        BlockPos playerPos = BlockPos.containing(player.position());
        boolean downPhase = VisualSettings.phaseSneakDown() && Minecraft.getInstance().options.keyShift.isDown();
        boolean keepFloor = blockPos.equals(playerPos.below()) && !downPhase;
        if (!keepFloor) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }
}
