package com.xerion.forge.world;

import com.xerion.common.movement.ScaffoldRotation;
import com.xerion.common.world.WorldMiningRules;

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

import com.xerion.forge.gui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraft.util.Mth;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class VeinMinerService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final int SERVER_BATCH_LIMIT = 128;
    private static final BlockPos[] SHAPELESS_OFFSETS = createShapelessOffsets();
    private static final BlockPos[] PLANT_OFFSETS = createPlantOffsets();
    private static final List<BlockPos> TARGETS = new ArrayList<>();
    private static final Set<BlockPos> BASIC_MINED_FRONTIER = new HashSet<>();
    private static BlockPos lastBasicMinedPos;
    private static final ArrayDeque<BlockPos> PACKET_QUEUE = new ArrayDeque<>();
    private static BlockPos seedPos;
    private static BlockState seedState;
    private static Direction seedFace = Direction.UP;
    private static boolean keyWasDown;
    private static boolean waitingForSeedBreak;
    private static int chainWaitTicks;
    private static boolean packetMining;
    private static BlockPos packetPos;
    private static Direction packetFace = Direction.UP;
    private static float packetProgress;
    private static boolean basicMining;
    private static int basicDelayTicks;
    private static BlockPos basicRotateTarget;
    private static float basicRotateYaw;
    private static float basicRotatePitch;
    private static boolean basicRenderRotationActive;
    private static float basicRenderYaw;
    private static float basicRenderPitch;
    private static long basicRenderLastUpdateNanos;

    public void tick() {
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (!canRun(player, level)) {
            reset();
            return;
        }

        boolean keyDown = ForgeModEventBridge.isVeinMinerKeyDown();
        keyWasDown = keyDown;
        if (!keyDown) {
            resetSelectionOnly();
            basicMining = false;
            basicDelayTicks = 0;
            clearBasicRotation();
            return;
        }

        if ("Basic".equals(VisualSettings.veinMinerMode())) {
            tickBasic(player, level);
            return;
        }

        if (waitingForSeedBreak) {
            if (seedPos == null) {
                waitingForSeedBreak = false;
                chainWaitTicks = 0;
                resetSelectionOnly();
                return;
            }
            BlockState currentSeedState = level.getBlockState(seedPos);
            if (currentSeedState.isAir()) {
                waitingForSeedBreak = false;
                chainWaitTicks = 0;
                destroySelection(player, level);
                return;
            }
            if (!MC.options.keyAttack.isDown()
                    || seedState == null
                    || !validSeed(level, seedPos, currentSeedState)
                    || !VeinMinerBlockMatcher.sameBlockFamily(seedState, currentSeedState)
                    || !isStillTargetingSeed(player)
                    || chainWaitTicks > 80) {
                waitingForSeedBreak = false;
                chainWaitTicks = 0;
                refreshSelection(player, level);
                return;
            }
            chainWaitTicks++;
            return;
        }

        if (packetMining) {
            tickPacketQueue(player, level);
            return;
        }

        pruneSelection(level);
        refreshSelection(player, level);

        if (MC.options.keyAttack.isDown() && seedPos != null && !TARGETS.isEmpty()) {
            waitingForSeedBreak = true;
            chainWaitTicks = 0;
        }
    }

    public void renderPreview(RenderLevelStageEvent event) {
        VeinMinerPreviewRenderer.render(event, keyWasDown, TARGETS, seedPos);
    }

    private static boolean canRun(LocalPlayer player, ClientLevel level) {
        return VisualSettings.veinMinerEnabled()
                && player != null
                && level != null
                && MC.gameMode != null
                && !player.isSpectator()
                && !(MC.screen instanceof ClickGuiScreen)
                && !(MC.screen instanceof AbstractContainerScreen<?>)
                && !VisualSettings.nukerEnabled();
    }

    private static void tickBasic(LocalPlayer player, ClientLevel level) {
        if (!basicMining && TARGETS.isEmpty() && seedPos == null) {
            refreshSelection(player, level);
            basicMining = seedPos != null;
        }
        pruneBasicSelection(level);
        if (seedPos != null && !TARGETS.contains(seedPos)) {
            TARGETS.add(0, seedPos);
        }
        if (TARGETS.isEmpty()) {
            resetSelectionOnly();
            basicMining = false;
            basicDelayTicks = 0;
            clearBasicRotation();
            return;
        }
        if (basicDelayTicks > 0) {
            basicDelayTicks--;
            return;
        }
        Vec3 eye = player.getEyePosition();
        BlockPos target = chooseBasicTarget(eye);
        if (!withinPickRange(target, eye)) {
            moveToward(player, target);
            if (VisualSettings.veinMinerRotate()) {
                holdSilentRotation(player, target);
            }
            return;
        }
        if (VisualSettings.veinMinerRotate()) {
            holdSilentRotation(player, target);
        } else {
            clearBasicRotation();
        }
        Direction face = targetFace(level, target, eye);
        boolean accepted = MC.gameMode != null && MC.gameMode.continueDestroyBlock(target, face);
        if (!accepted && MC.gameMode != null) {
            accepted = MC.gameMode.startDestroyBlock(target, face);
        }
        if (accepted) {
            player.swing(InteractionHand.MAIN_HAND);
        }
        basicDelayTicks = 2;
    }

    private static void pruneBasicSelection(ClientLevel level) {
        TARGETS.removeIf(pos -> {
            BlockState state = level.getBlockState(pos);
            boolean removed = state.isAir() || state.getDestroySpeed(level, pos) < 0.0f;
            if (removed) {
                rememberBasicMined(pos);
            }
            return removed;
        });
        if (seedPos != null && (level.getBlockState(seedPos).isAir()
                || level.getBlockState(seedPos).getDestroySpeed(level, seedPos) < 0.0f)) {
            rememberBasicMined(seedPos);
            seedPos = null;
        }
    }

    private static BlockPos chooseBasicTarget(Vec3 eye) {
        return TARGETS.stream()
                .min(Comparator
                        .comparingInt(VeinMinerService::basicAdjacencyRank)
                        .thenComparingDouble(pos -> distanceToAabbSqr(pos, eye)))
                .orElse(TARGETS.get(0));
    }

    private static int basicAdjacencyRank(BlockPos pos) {
        if (lastBasicMinedPos != null && isTwentySixWayAdjacent(pos, lastBasicMinedPos)) {
            return 0;
        }
        if (adjacentToBasicFrontier(pos)) {
            return 1;
        }
        return 2;
    }

    private static void rememberBasicMined(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        BASIC_MINED_FRONTIER.add(immutable);
        lastBasicMinedPos = immutable;
    }

    private static boolean adjacentToBasicFrontier(BlockPos pos) {
        if (BASIC_MINED_FRONTIER.isEmpty()) {
            return seedPos != null && isTwentySixWayAdjacent(pos, seedPos);
        }
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if ((x != 0 || y != 0 || z != 0)
                            && BASIC_MINED_FRONTIER.contains(pos.offset(x, y, z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isTwentySixWayAdjacent(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        return WorldMiningRules.twentySixWayAdjacent(dx, dy, dz);
    }

    private static boolean withinPickRange(BlockPos pos, Vec3 eye) {
        double reach = MC.gameMode == null ? 4.5D : MC.gameMode.getPickRange();
        return distanceToAabbSqr(pos, eye) <= reach * reach;
    }

    private static double distanceToAabbSqr(BlockPos pos, Vec3 eye) {
        double dx = WorldMiningRules.axisDistance(eye.x, pos.getX(), pos.getX() + 1.0D);
        double dy = WorldMiningRules.axisDistance(eye.y, pos.getY(), pos.getY() + 1.0D);
        double dz = WorldMiningRules.axisDistance(eye.z, pos.getZ(), pos.getZ() + 1.0D);
        return dx * dx + dy * dy + dz * dz;
    }

    private static void moveToward(LocalPlayer player, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 delta = new Vec3(center.x - player.getX(), 0.0D, center.z - player.getZ());
        double length = delta.length();
        if (length <= 0.05D) {
            return;
        }
        Vec3 motion = delta.scale(0.11D / length);
        player.setDeltaMovement(player.getDeltaMovement().x + motion.x, player.getDeltaMovement().y, player.getDeltaMovement().z + motion.z);
    }

    private static void holdSilentRotation(LocalPlayer player, BlockPos targetPos) {
        if (!targetPos.equals(basicRotateTarget)) {
            Vec3 target = Vec3.atCenterOf(targetPos);
            Vec3 eye = player.getEyePosition();
            double dx = target.x - eye.x;
            double dy = target.y - eye.y;
            double dz = target.z - eye.z;
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            basicRotateYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0D);
            basicRotatePitch = Mth.clamp((float) (-Math.toDegrees(Math.atan2(dy, horizontal))), -90.0f, 90.0f);
            basicRotateTarget = targetPos.immutable();
            if (!basicRenderRotationActive) {
                basicRenderYaw = basicRotateYaw;
                basicRenderPitch = basicRotatePitch;
                basicRenderLastUpdateNanos = System.nanoTime();
            }
        }
        ScaffoldRotation.requestPacketOnly(basicRotateYaw, basicRotatePitch, true);
        basicRenderRotationActive = true;
    }

    private static void clearBasicRotation() {
        basicRotateTarget = null;
        basicRenderRotationActive = false;
        basicRenderLastUpdateNanos = 0L;
    }

    public static boolean basicRenderRotationActive() {
        return basicRenderRotationActive
                && "Basic".equals(VisualSettings.veinMinerMode())
                && VisualSettings.veinMinerEnabled()
                && VisualSettings.veinMinerRotate();
    }

    public static void updateBasicRenderRotationFrame() {
        if (basicRenderRotationActive()) {
            updateBasicRenderRotation();
        }
    }

    public static float basicRenderYaw() {
        return basicRenderYaw;
    }

    public static float basicRenderPitch() {
        return basicRenderPitch;
    }

    private static void updateBasicRenderRotation() {
        long now = System.nanoTime();
        if (basicRenderLastUpdateNanos == 0L) {
            basicRenderLastUpdateNanos = now;
            return;
        }
        float deltaSeconds = Math.min(0.05f, (now - basicRenderLastUpdateNanos) / 1_000_000_000.0f);
        basicRenderLastUpdateNanos = now;
        float alpha = 1.0f - (float) Math.exp(-deltaSeconds * 18.0f);
        basicRenderYaw = lerpYaw(basicRenderYaw, basicRotateYaw, alpha);
        basicRenderPitch = Mth.lerp(alpha, basicRenderPitch, basicRotatePitch);
    }

    private static float lerpYaw(float from, float to, float alpha) {
        return from + Mth.wrapDegrees(to - from) * alpha;
    }

    private static void refreshSelection(LocalPlayer player, ClientLevel level) {
        BlockHitResult hit = targetBlock(player);
        if (hit == null) {
            resetSelectionOnly();
            return;
        }
        BlockPos pos = hit.getBlockPos().immutable();
        BlockState state = level.getBlockState(pos);
        if (!validSeed(level, pos, state)) {
            resetSelectionOnly();
            return;
        }
        if (pos.equals(seedPos) && seedState != null && VeinMinerBlockMatcher.sameBlockFamily(seedState, state)) {
            return;
        }
        seedPos = pos;
        seedState = state;
        seedFace = hit.getDirection();
        TARGETS.clear();
        BASIC_MINED_FRONTIER.clear();
        lastBasicMinedPos = null;
        TARGETS.addAll(createSelection(level, pos, state));
        TARGETS.remove(pos);
    }

    private static void pruneSelection(ClientLevel level) {
        if (seedPos == null || seedState == null) {
            TARGETS.clear();
            return;
        }
        BlockState currentSeedState = level.getBlockState(seedPos);
        if (currentSeedState.isAir()
                || !validSeed(level, seedPos, currentSeedState)
                || !VeinMinerBlockMatcher.sameBlockFamily(seedState, currentSeedState)) {
            resetSelectionOnly();
            return;
        }
        TARGETS.removeIf(pos -> pos.equals(seedPos)
                || !validSelectionBlock(level, pos, level.getBlockState(pos), seedState));
    }

    private static BlockHitResult targetBlock(LocalPlayer player) {
        float reach = MC.gameMode == null ? 4.5f : MC.gameMode.getPickRange();
        HitResult result = player.pick(reach, MC.getFrameTime(), false);
        if (result instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        return null;
    }

    private static boolean isStillTargetingSeed(LocalPlayer player) {
        if (seedPos == null) {
            return false;
        }
        BlockHitResult hit = targetBlock(player);
        return hit != null && seedPos.equals(hit.getBlockPos());
    }

    private static boolean validSeed(ClientLevel level, BlockPos pos, BlockState state) {
        return !state.isAir() && state.getDestroySpeed(level, pos) >= 0.0f;
    }

    private static List<BlockPos> createSelection(ClientLevel level, BlockPos origin, BlockState originalState) {
        int maxBlocks = WorldMiningRules.veinMinerMaxBlocks(VisualSettings.veinMinerMaxBlocks());
        int scanLimit = WorldMiningRules.veinMinerScanLimit(maxBlocks);
        String mode = VisualSettings.veinMinerMode();
        String format = VisualSettings.veinMinerFormat();
        Set<BlockPos> known = "Basic".equals(mode)
                ? shapelessSelection(level, origin, originalState, scanLimit)
                : switch (format) {
                    case "Tunnel" -> tunnelSelection(level, origin, originalState, maxBlocks);
                    case "Small Vein" -> smallVeinSelection(level, origin, originalState, maxBlocks);
                    case "Sphere" -> sphereSelection(level, origin, originalState, maxBlocks);
                    case "Plane" -> planeSelection(level, origin, originalState, maxBlocks);
                    case "Stair" -> stairSelection(level, origin, originalState, maxBlocks);
                    default -> shapelessSelection(level, origin, originalState, scanLimit);
                };
        return known.stream()
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(origin)))
                .limit(maxBlocks)
                .map(BlockPos::immutable)
                .toList();
    }

    private static Set<BlockPos> shapelessSelection(ClientLevel level, BlockPos origin, BlockState originalState, int scanLimit) {
        Set<BlockPos> known = new HashSet<>();
        Set<BlockPos> openSet = new HashSet<>();
        ArrayDeque<BlockPos> openQueue = new ArrayDeque<>();
        BlockPos[] offsets = VeinMinerBlockMatcher.isPlantLike(originalState) ? PLANT_OFFSETS : SHAPELESS_OFFSETS;
        openSet.add(origin.immutable());
        openQueue.add(origin.immutable());

        while (!openQueue.isEmpty() && known.size() < scanLimit) {
            BlockPos pos = openQueue.removeFirst();
            openSet.remove(pos);
            BlockState state = pos.equals(origin) ? originalState : level.getBlockState(pos);
            if (!validSelectionBlock(level, pos, state, originalState)) {
                continue;
            }
            known.add(pos.immutable());
            for (BlockPos offset : offsets) {
                BlockPos next = pos.offset(offset).immutable();
                if (!known.contains(next) && openSet.add(next)) {
                    openQueue.add(next);
                }
            }
        }
        return known;
    }

    private static Set<BlockPos> smallVeinSelection(ClientLevel level, BlockPos origin, BlockState originalState, int limit) {
        Set<BlockPos> known = new HashSet<>();
        Direction direction = seedFace.getOpposite();
        BlockPos cursor = origin.immutable();
        while (known.size() < limit) {
            BlockState state = cursor.equals(origin) ? originalState : level.getBlockState(cursor);
            if (!validSelectionBlock(level, cursor, state, originalState)) {
                break;
            }
            known.add(cursor.immutable());
            cursor = cursor.relative(direction).immutable();
        }
        return known;
    }

    private static Set<BlockPos> tunnelSelection(ClientLevel level, BlockPos origin, BlockState originalState, int limit) {
        Set<BlockPos> known = new HashSet<>();
        Direction direction = seedFace.getOpposite();
        int radius = WorldMiningRules.tunnelRadius(limit);
        int depth = WorldMiningRules.tunnelDepth(limit, radius);
        Direction right = horizontalRight(direction);
        Direction up = direction.getAxis() == Direction.Axis.Y ? horizontalForward() : Direction.UP;
        for (int d = 0; d < depth && known.size() < limit; d++) {
            BlockPos center = origin.relative(direction, d).immutable();
            for (int u = -radius; u <= radius && known.size() < limit; u++) {
                for (int r = -radius; r <= radius && known.size() < limit; r++) {
                    BlockPos cursor = center.relative(right, r).relative(up, u).immutable();
                    BlockState state = cursor.equals(origin) ? originalState : level.getBlockState(cursor);
                    if (validSelectionBlock(level, cursor, state, originalState)) {
                        known.add(cursor);
                    }
                }
            }
        }
        return known;
    }

    private static Direction horizontalForward() {
        LocalPlayer player = MC.player;
        if (player == null) {
            return Direction.NORTH;
        }
        return Direction.getNearest(player.getLookAngle().x, 0.0D, player.getLookAngle().z);
    }

    private static Direction horizontalRight(Direction direction) {
        if (direction.getAxis() == Direction.Axis.Y) {
            Direction forward = horizontalForward();
            return forward.getClockWise();
        }
        return direction.getClockWise();
    }

    private static void addIfValid(Set<BlockPos> known, ClientLevel level, BlockPos pos, BlockState originalState) {
        BlockState state = pos.equals(seedPos) ? originalState : level.getBlockState(pos);
        if (validSelectionBlock(level, pos, state, originalState)) {
            known.add(pos.immutable());
        }
    }

    private static List<BlockPos> sortedValidCandidates(Set<BlockPos> candidates, ClientLevel level, BlockPos origin,
                                                        BlockState originalState, int limit) {
        return candidates.stream()
                .filter(pos -> validSelectionBlock(level, pos, pos.equals(origin) ? originalState : level.getBlockState(pos), originalState))
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(origin)))
                .limit(limit)
                .map(BlockPos::immutable)
                .toList();
    }

    private static Set<BlockPos> setOf(List<BlockPos> positions) {
        return new HashSet<>(positions);
    }

    private static Set<BlockPos> sphereSelection(ClientLevel level, BlockPos origin, BlockState originalState, int limit) {
        Set<BlockPos> candidates = new HashSet<>();
        int radius = WorldMiningRules.sphereRadius(limit);
        double radiusSqr = radius * radius;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            BlockPos immutable = pos.immutable();
            if (immutable.distSqr(origin) <= radiusSqr) {
                candidates.add(immutable);
            }
        }
        return setOf(sortedValidCandidates(candidates, level, origin, originalState, limit));
    }

    private static Set<BlockPos> planeSelection(ClientLevel level, BlockPos origin, BlockState originalState, int limit) {
        Set<BlockPos> candidates = new HashSet<>();
        Direction.Axis axis = seedFace.getAxis();
        int radius = WorldMiningRules.planeRadius(limit);
        for (int a = -radius; a <= radius; a++) {
            for (int b = -radius; b <= radius; b++) {
                BlockPos pos = switch (axis) {
                    case X -> origin.offset(0, a, b);
                    case Y -> origin.offset(a, 0, b);
                    case Z -> origin.offset(a, b, 0);
                };
                candidates.add(pos.immutable());
            }
        }
        return setOf(sortedValidCandidates(candidates, level, origin, originalState, limit));
    }

    private static Set<BlockPos> stairSelection(ClientLevel level, BlockPos origin, BlockState originalState, int limit) {
        Set<BlockPos> known = new HashSet<>();
        Direction direction = horizontalForward();
        LocalPlayer player = MC.player;
        int yStep = player != null && player.getXRot() > 20.0f ? -1 : 1;
        int width = WorldMiningRules.stairWidth(limit);
        int halfWidth = width / 2;
        int steps = WorldMiningRules.stairSteps(limit, width);
        Direction right = direction.getClockWise();
        for (int step = 0; step < steps && known.size() < limit; step++) {
            BlockPos center = origin.relative(direction, step).offset(0, yStep * (step / 2), 0).immutable();
            for (int w = -halfWidth; w <= halfWidth && known.size() < limit; w++) {
                addIfValid(known, level, center.relative(right, w), originalState);
            }
        }
        return known;
    }

    private static boolean validSelectionBlock(ClientLevel level, BlockPos pos, BlockState state, BlockState originalState) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0f
                || !VeinMinerBlockMatcher.sameBlockFamily(originalState, state)) {
            return false;
        }
        return true;
    }

    private static void destroySelection(LocalPlayer player, ClientLevel level) {
        if (TARGETS.isEmpty()) {
            resetSelectionOnly();
            return;
        }
        if (destroySelectionIntegrated(player)) {
            resetSelectionOnly();
            return;
        }
        PACKET_QUEUE.clear();
        PACKET_QUEUE.addAll(TARGETS);
        TARGETS.clear();
        packetMining = true;
        packetPos = null;
        packetProgress = 0.0f;
        tickPacketQueue(player, level);
    }

    private static boolean destroySelectionIntegrated(LocalPlayer player) {
        if (MC.getSingleplayerServer() == null || player.getGameProfile() == null) {
            return false;
        }
        ServerPlayer serverPlayer = MC.getSingleplayerServer().getPlayerList().getPlayer(player.getUUID());
        if (serverPlayer == null || serverPlayer.gameMode == null) {
            return false;
        }
        int broken = 0;
        for (BlockPos pos : TARGETS) {
            if (broken >= SERVER_BATCH_LIMIT || pos.equals(seedPos)) {
                continue;
            }
            BlockState state = serverPlayer.serverLevel().getBlockState(pos);
            if (seedState != null && validServerSelectionBlock(serverPlayer, pos, state, seedState)) {
                serverPlayer.gameMode.destroyBlock(pos);
                broken++;
            }
        }
        return true;
    }

    private static boolean validServerSelectionBlock(ServerPlayer player, BlockPos pos, BlockState state, BlockState originalState) {
        if (state.isAir() || state.getDestroySpeed(player.serverLevel(), pos) < 0.0f
                || !VeinMinerBlockMatcher.sameBlockFamily(originalState, state)) {
            return false;
        }
        return true;
    }

    private static void tickPacketQueue(LocalPlayer player, ClientLevel level) {
        if (MC.getConnection() == null || seedState == null) {
            reset();
            return;
        }
        if (packetPos != null) {
            BlockState state = level.getBlockState(packetPos);
            if (state.isAir() || !validSelectionBlock(level, packetPos, state, seedState)) {
                packetPos = null;
                packetProgress = 0.0f;
            }
        }
        while (packetPos == null && !PACKET_QUEUE.isEmpty()) {
            BlockPos candidate = PACKET_QUEUE.removeFirst();
            BlockState state = level.getBlockState(candidate);
            if (validSelectionBlock(level, candidate, state, seedState)) {
                packetPos = candidate.immutable();
                packetFace = targetFace(level, packetPos, player.getEyePosition());
                packetProgress = 0.0f;
            }
        }
        if (packetPos == null) {
            packetMining = false;
            resetSelectionOnly();
            return;
        }

        BlockState state = level.getBlockState(packetPos);
        if (player.getAbilities().instabuild) {
            MC.gameMode.startDestroyBlock(packetPos, packetFace);
            player.swing(InteractionHand.MAIN_HAND);
            packetPos = null;
            return;
        }

        if (packetProgress <= 0.0f) {
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                    packetPos,
                    packetFace));
            player.swing(InteractionHand.MAIN_HAND);
        }
        packetProgress += state.getDestroyProgress(player, level, packetPos);
        if (packetProgress >= 1.0f) {
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    packetPos,
                    packetFace));
            player.swing(InteractionHand.MAIN_HAND);
            packetPos = null;
            packetProgress = 0.0f;
        }
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

    private static BlockPos[] createShapelessOffsets() {
        BlockPos[] offsets = new BlockPos[26];
        int index = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        offsets[index++] = new BlockPos(x, y, z);
                    }
                }
            }
        }
        return offsets;
    }

    private static BlockPos[] createPlantOffsets() {
        BlockPos[] offsets = new BlockPos[26];
        int index = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x != 0 || z != 0) {
                    offsets[index++] = new BlockPos(x, 0, z);
                }
            }
        }
        offsets[index++] = BlockPos.ZERO.above();
        offsets[index] = BlockPos.ZERO.below();
        return offsets;
    }

    private static void resetSelectionOnly() {
        TARGETS.clear();
        BASIC_MINED_FRONTIER.clear();
        lastBasicMinedPos = null;
        seedPos = null;
        seedState = null;
        seedFace = Direction.UP;
        waitingForSeedBreak = false;
        chainWaitTicks = 0;
        clearBasicRotation();
    }

    public static void reset() {
        if (packetPos != null && MC.getConnection() != null) {
            MC.getConnection().send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK,
                    packetPos,
                    packetFace));
        }
        clearRuntimeState();
    }

    public static void clearRuntimeState() {
        PACKET_QUEUE.clear();
        packetMining = false;
        packetPos = null;
        packetProgress = 0.0f;
        keyWasDown = false;
        resetSelectionOnly();
    }

}

