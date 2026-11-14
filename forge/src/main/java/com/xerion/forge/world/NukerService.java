package com.xerion.forge.world;

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
import com.xerion.common.world.WorldMiningRules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class NukerService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static BlockPos currentPos;
    private static Direction currentFace = Direction.UP;
    private static int delayTicks;
    private static boolean packetStarted;
    private static float packetProgress;

    public void tick() {
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (!VisualSettings.nukerEnabled()
                || player == null
                || level == null
                || MC.gameMode == null
                || player.isSpectator()
                || MC.screen instanceof AbstractContainerScreen<?>) {
            reset();
            return;
        }

        float range = VisualSettings.nukerRange();
        Block sameBlock = sameBlockTarget(player, range);
        if ("Same Block".equals(VisualSettings.nukerTarget()) && sameBlock == null) {
            reset();
            return;
        }

        List<Candidate> candidates = candidates(player, level, range, sameBlock);
        if (candidates.isEmpty()) {
            reset();
            return;
        }

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        if (player.getAbilities().instabuild) {
            mineCreative(player, candidates);
            return;
        }

        if (VisualSettings.nukerPacketMine()) {
            minePacket(player, level, candidates);
            return;
        }

        mineSurvival(player, level, candidates);
    }

    private static void mineCreative(LocalPlayer player, List<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            rotateTo(player, candidate.pos);
            if (MC.gameMode.startDestroyBlock(candidate.pos, candidate.face)) {
                swing(player);
            }
            break;
        }
        delayTicks = VisualSettings.nukerDelayTicks();
    }

    private static void mineSurvival(LocalPlayer player, ClientLevel level, List<Candidate> candidates) {
        Candidate candidate = currentCandidate(level, candidates);
        if (candidate == null) {
            candidate = candidates.get(0);
            if (currentPos != null && !currentPos.equals(candidate.pos)) {
                MC.gameMode.stopDestroyBlock();
            }
            currentPos = candidate.pos.immutable();
            currentFace = candidate.face;
        }

        rotateTo(player, candidate.pos);
        boolean accepted = MC.gameMode.continueDestroyBlock(candidate.pos, candidate.face);
        if (!accepted) {
            accepted = MC.gameMode.startDestroyBlock(candidate.pos, candidate.face);
        }
        if (accepted) {
            swing(player);
        }
        delayTicks = VisualSettings.nukerDelayTicks();
    }

    private static void minePacket(LocalPlayer player, ClientLevel level, List<Candidate> candidates) {
        Candidate candidate = currentCandidate(level, candidates);
        if (candidate == null) {
            abortPacketMine();
            candidate = candidates.get(0);
            currentPos = candidate.pos.immutable();
            currentFace = candidate.face;
            packetProgress = 0.0f;
        }

        if (MC.getConnection() == null) {
            return;
        }

        rotateTo(player, candidate.pos);
        if (!packetStarted) {
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                    candidate.pos,
                    candidate.face));
            packetStarted = true;
            swing(player);
        }

        BlockState state = level.getBlockState(candidate.pos);
        if (state.isAir()) {
            abortPacketMine();
            return;
        }

        packetProgress += state.getDestroyProgress(player, level, candidate.pos);
        if (packetProgress >= 1.0f) {
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    candidate.pos,
                    candidate.face));
            swing(player);
            packetStarted = false;
            packetProgress = 0.0f;
            delayTicks = VisualSettings.nukerDelayTicks();
        }
    }

    private static Candidate currentCandidate(ClientLevel level, List<Candidate> candidates) {
        if (currentPos == null || level.getBlockState(currentPos).isAir()) {
            currentPos = null;
            return null;
        }
        for (Candidate candidate : candidates) {
            if (candidate.pos.equals(currentPos)) {
                currentFace = candidate.face;
                return candidate;
            }
        }
        currentPos = null;
        return null;
    }

    private static List<Candidate> candidates(LocalPlayer player, ClientLevel level, float range, Block sameBlock) {
        Vec3 eye = player.getEyePosition();
        BlockPos center = player.blockPosition();
        int radius = WorldMiningRules.scanRadius(range);
        double maxDistanceSq = WorldMiningRules.rangeDistanceSq(range);
        List<Candidate> result = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    double distanceSq = eye.distanceToSqr(Vec3.atCenterOf(mutable));
                    if (Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z)) > radius) {
                        continue;
                    }
                    if (VisualSettings.nukerExtensionEnabled(VisualSettings.NUKER_EXTENSION_SKIP_FEET_LEVEL)
                            && mutable.getY() == center.getY() - 1) {
                        continue;
                    }
                    BlockState state = level.getBlockState(mutable);
                    Direction face = targetFace(level, mutable, eye);
                    if (!validTarget(level, mutable, state, face, sameBlock)) {
                        continue;
                    }
                    result.add(new Candidate(mutable.immutable(), face == null ? Direction.UP : face, distanceSq));
                }
            }
        }

        result.sort(NukerService::compareCandidates);
        return result;
    }

    private static int compareCandidates(Candidate left, Candidate right) {
        return WorldMiningRules.compareNukerCandidates(
                VisualSettings.nukerSortMode(),
                left.pos.getY(),
                left.distanceSq,
                right.pos.getY(),
                right.distanceSq
        );
    }

    private static boolean validTarget(ClientLevel level, BlockPos pos, BlockState state, Direction exposedFace, Block sameBlock) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0f) {
            return false;
        }
        return WorldMiningRules.nukerTargetAllowed(
                VisualSettings.nukerTarget(),
                listedBlockAllowed(state),
                sameBlock != null && state.is(sameBlock),
                exposedFace != null
        );
    }

    private static boolean listedBlockAllowed(BlockState state) {
        var id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id != null && VisualSettings.nukerBlockEnabled(id.toString());
    }

    private static void swing(LocalPlayer player) {
        if (VisualSettings.nukerSwing()) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static void rotateTo(LocalPlayer player, BlockPos pos) {
        if (!VisualSettings.nukerRotate()) {
            return;
        }
        RotationPacketBridge.requestPacketLookAt(player, pos);
    }

    private static Direction targetFace(ClientLevel level, BlockPos pos, Vec3 eye) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.isAir() || !neighbor.canOcclude()) {
                return direction;
            }
        }
        Vec3 center = Vec3.atCenterOf(pos);
        return Direction.getNearest(eye.x - center.x, eye.y - center.y, eye.z - center.z);
    }

    private static Block sameBlockTarget(LocalPlayer player, float range) {
        HitResult result = player.pick(range, MC.getFrameTime(), false);
        if (!(result instanceof BlockHitResult blockHit) || blockHit.getType() != HitResult.Type.BLOCK || MC.level == null) {
            return null;
        }
        BlockState state = MC.level.getBlockState(blockHit.getBlockPos());
        return state.isAir() ? null : state.getBlock();
    }

    public static void reset() {
        if (packetStarted) {
            abortPacketMine();
        } else if (currentPos != null && MC.gameMode != null) {
            MC.gameMode.stopDestroyBlock();
        }
        clearRuntimeState();
    }

    public static void clearRuntimeState() {
        currentPos = null;
        currentFace = Direction.UP;
        delayTicks = 0;
        packetStarted = false;
        packetProgress = 0.0f;
    }

    private static void abortPacketMine() {
        if (packetStarted && currentPos != null && MC.getConnection() != null) {
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                    currentPos,
                    currentFace));
        }
        packetStarted = false;
        packetProgress = 0.0f;
    }

    private static final class Candidate {
        private final BlockPos pos;
        private final Direction face;
        private final double distanceSq;

        private Candidate(BlockPos pos, Direction face, double distanceSq) {
            this.pos = pos;
            this.face = face;
            this.distanceSq = distanceSq;
        }
    }
}
