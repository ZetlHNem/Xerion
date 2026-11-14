package com.xerion.forge.player;

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
import com.xerion.common.player.PlayerItemRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class AutoMineService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static BlockPos currentPos;
    private static Direction currentFace = Direction.UP;
    private static int delayTicks;

    public void tick() {
        LocalPlayer player = MC.player;
        if (!VisualSettings.autoMineEnabled()
                || player == null
                || MC.level == null
                || MC.gameMode == null
                || player.isSpectator()
                || MC.screen instanceof AbstractContainerScreen<?>) {
            reset();
            return;
        }

        BlockHitResult blockHit = targetBlock(player);
        if (blockHit == null) {
            reset();
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = MC.level.getBlockState(pos);
        if (!PlayerItemRules.autoMineMineable(state.isAir(), state.getDestroySpeed(MC.level, pos))) {
            reset();
            return;
        }

        if (PlayerItemRules.autoMineTargetChanged(pos.equals(currentPos))) {
            reset();
            currentPos = pos.immutable();
            currentFace = blockHit.getDirection();
            delayTicks = VisualSettings.autoMineDelayTicks();
        }

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        boolean accepted = MC.gameMode.continueDestroyBlock(pos, blockHit.getDirection());
        if (PlayerItemRules.autoMineShouldRetryStart(accepted)) {
            accepted = MC.gameMode.startDestroyBlock(pos, blockHit.getDirection());
        }
        if (accepted && VisualSettings.autoMineSwing()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static BlockHitResult targetBlock(LocalPlayer player) {
        if (PlayerItemRules.lockedMineTarget(VisualSettings.autoMineTarget()) && currentPos != null) {
            if (PlayerItemRules.withinRangeSq(
                    player.getEyePosition().distanceToSqr(currentPos.getCenter()),
                    VisualSettings.autoMineRange())) {
                return new BlockHitResult(currentPos.getCenter(), currentFace, currentPos, false);
            }
            return null;
        }
        HitResult result = player.pick(VisualSettings.autoMineRange(), MC.getFrameTime(), false);
        if (result instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        return null;
    }

    public static void reset() {
        if (currentPos != null && MC.gameMode != null) {
            MC.gameMode.stopDestroyBlock();
        }
        clearRuntimeState();
    }

    public static void clearRuntimeState() {
        currentPos = null;
        currentFace = Direction.UP;
        delayTicks = 0;
    }
}
