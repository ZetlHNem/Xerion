package com.xerion.forge.movement;

import com.xerion.common.combat.CombatAimRules;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.packet.PacketBlockageService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Port of OpenOpal's SilenceTelly scaffold: the placement rotation is written
 * into the outbound movement packet, then the placement is armed after that
 * packet is actually sent and executed on the next input phase.
 */
public final class ScaffoldSilenceTellyService {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger("xerion-scaffold");
    private static final int SEARCH_RADIUS = 5;
    private static final int MAX_RESCUE_ROTATE_TICKS = 8;
    private static final double REACH = 4.5D;
    private static final double[] FACE_OFFSETS = {0.0D, 0.24D, -0.24D, 0.36D, -0.36D, 0.45D, -0.45D};

    private static BlockData blockData;
    private static BlockData lastBlockData;
    private static float[] rotation;
    private static float[] lastRotation;
    private static float[] pendingPacketRotation;
    private static Vec3 pendingEyePos;
    private static boolean placementRequested;
    private static int placementRequestAge = -1;
    private static BlockData requestData;
    private static int requestSlot = -1;
    private static InteractionHand requestHand = InteractionHand.MAIN_HAND;
    private static boolean placementArmed;
    private static int placementArmedAge = -1;
    private static BlockData armedData;
    private static int armedSlot = -1;
    private static InteractionHand armedHand = InteractionHand.MAIN_HAND;
    private static float[] armedRotation;
    private static float[] serverRotation;
    private static float lastPlacedYawDelta = Float.NaN;
    private static int lastPlaceTick = -1;
    private static int restoreSlot = -1;
    private static boolean restoreSlotPending;
    private static int rotateCount;
    private static int movementCancelTicks;
    private static int ups;
    private static int airTicks;
    private static int groundTicks;
    private static int eagleTicks;
    private static boolean waitingEagle;
    private static boolean modulePressedSneak;
    private static boolean rewriting;
    private static boolean blockFlyPending;
    private static int blockFlyTicks;
    private static int posY;
    private static int lastSearchTick = -1;
    private static BlockPos lastSearchOrigin;
    private static BlockData lastSearchData;
    private static String lastDebug;
    private static int lastDebugTick = -1;
    private static boolean activeFlag;

    private ScaffoldSilenceTellyService() {
    }

    public static boolean isActive() {
        return VisualSettings.scaffoldEnabled()
                && "SilenceTelly".equals(VisualSettings.scaffoldMode())
                && MC.player != null
                && MC.level != null;
    }

    public static void tick() {
        LocalPlayer player = MC.player;
        ClientLevel level = MC.level;
        if (player == null || level == null || MC.gameMode == null) {
            clearRuntimeState();
            return;
        }
        int tick = player.tickCount;
        activeFlag = true;
        if (player.onGround()) {
            groundTicks++;
            airTicks = 0;
            posY = player.blockPosition().getY() - 1;
        } else {
            airTicks++;
            groundTicks = 0;
        }
        if (MC.options.keyJump.isDown()) {
            posY = player.blockPosition().getY() - 1;
        }

        if (placementRequestAge >= 0 && tick - placementRequestAge > 1) {
            clearPlacementRequest();
        }
        if (placementArmedAge >= 0 && tick - placementArmedAge > 1) {
            clearPlacementArmed();
        }
        tickBlockFly(player);

        if (placementArmed) {
            if (tryPlaceArmed(player)) {
                placementArmed = false;
                prepareTarget(player);
            } else {
                clearPlacementArmed();
            }
            return;
        }
        prepareTarget(player);
    }

    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!isActive() || event.getInput() == null || !(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        var input = event.getInput();
        if (movementCancelTicks > 0) {
            input.forwardImpulse = 0.0F;
            input.leftImpulse = 0.0F;
            movementCancelTicks--;
            return;
        }
        updateEagleSneak(input);
        if (!"Telly".equals(VisualSettings.scaffoldSilenceTellyMode())) {
            return;
        }
        if (shouldJump(player, input)) {
            input.jumping = true;
            if (VisualSettings.scaffoldSilenceTellyEagle()) {
                waitingEagle = true;
                eagleTicks = 0;
            }
        }
    }

    /**
     * Rewrites an outbound movement packet so it carries the pending placement
     * rotation. Returns the replacement packet, or null when nothing changes.
     */
    public static Packet<?> rewriteMovement(Packet<?> packet) {
        if (rewriting || !isActive() || !placementRequested || pendingPacketRotation == null
                || requestData == null || !(packet instanceof ServerboundMovePlayerPacket move)) {
            return null;
        }
        if (!(move instanceof ServerboundMovePlayerPacket.Rot)
                && !(move instanceof ServerboundMovePlayerPacket.PosRot)) {
            return null;
        }
        double x = move.getX(MC.player.getX());
        double y = move.getY(MC.player.getY());
        double z = move.getZ(MC.player.getZ());
        Vec3 eyePos = new Vec3(x, y + MC.player.getEyeHeight(), z);
        if (!hitsBlockFace(pendingPacketRotation, eyePos, requestData)) {
            return null;
        }
        pendingEyePos = eyePos;
        if (move instanceof ServerboundMovePlayerPacket.PosRot) {
            return new ServerboundMovePlayerPacket.PosRot(x, y, z,
                    pendingPacketRotation[0], pendingPacketRotation[1], move.isOnGround());
        }
        return new ServerboundMovePlayerPacket.Rot(pendingPacketRotation[0], pendingPacketRotation[1],
                move.isOnGround());
    }

    public static void sendRewritten(Packet<?> packet) {
        if (packet == null || MC.getConnection() == null) {
            return;
        }
        rewriting = true;
        try {
            MC.getConnection().send(packet);
        } finally {
            rewriting = false;
        }
    }

    /**
     * Called after a movement packet was actually written to the channel.
     * When the applied rotation can still hit the requested face, the
     * placement is armed for the next input phase.
     */
    public static void onMovementSent(Packet<?> packet) {
        if (!isActive() || !placementRequested || requestData == null
                || !(packet instanceof ServerboundMovePlayerPacket move) || !move.hasRotation()) {
            return;
        }
        float yaw = move.getYRot(0.0F);
        float pitch = move.getXRot(0.0F);
        if (!hitsBlockFace(new float[]{yaw, pitch}, pendingEyePos != null ? pendingEyePos : MC.player.getEyePosition(),
                requestData)) {
            clearPlacementRequest();
            return;
        }
        placementArmed = true;
        placementArmedAge = MC.player.tickCount;
        armedData = requestData;
        armedSlot = requestSlot;
        armedHand = requestHand;
        armedRotation = new float[]{yaw, pitch};
        if (serverRotation != null) {
            lastPlacedYawDeltaCandidate(serverRotation, armedRotation);
        }
        serverRotation = armedRotation;
        clearPlacementRequest();
        debug("placement armed face=" + armedData.facing());
    }

    public static void clearRuntimeState() {
        activeFlag = false;
        blockData = null;
        lastBlockData = null;
        rotation = null;
        lastRotation = null;
        pendingPacketRotation = null;
        pendingEyePos = null;
        placementRequested = false;
        placementRequestAge = -1;
        requestData = null;
        requestSlot = -1;
        requestHand = InteractionHand.MAIN_HAND;
        placementArmed = false;
        placementArmedAge = -1;
        armedData = null;
        armedSlot = -1;
        armedHand = InteractionHand.MAIN_HAND;
        armedRotation = null;
        serverRotation = null;
        lastPlacedYawDelta = Float.NaN;
        lastPlaceTick = -1;
        rotateCount = 0;
        movementCancelTicks = 0;
        ups = 0;
        airTicks = 0;
        groundTicks = 0;
        eagleTicks = 0;
        waitingEagle = false;
        releaseSneak();
        rewriting = false;
        blockFlyPending = false;
        blockFlyTicks = 0;
        lastSearchTick = -1;
        lastSearchOrigin = null;
        lastSearchData = null;
        restoreSlotPending = false;
        restoreSlot = -1;
    }

    /**
     * Clears lingering state (slot swap, forced sneak) once the mode is no
     * longer selected.
     */
    public static void dismissIfNeeded() {
        if (activeFlag) {
            clearRuntimeState();
        }
    }

    // ------------------------------------------------------------------
    // Target search + rotation
    // ------------------------------------------------------------------

    private static void prepareTarget(LocalPlayer player) {
        if (PacketBlockageService.outbound().isAnyBlockages()) {
            clearPlacementRequest();
            return;
        }
        if (!selectBlockSlot(player)) {
            clearTarget();
            return;
        }
        updateBlockTarget(player);
        updateRescueState(player);
        float[] computed = computeRotation(player);
        if (computed != null) {
            computed = applyDuplicateRotation(computed);
            computed = applyFixedSensitivity(player, computed);
        }
        rotation = computed;
        if (computed == null || blockData == null) {
            clearPlacementRequest();
            return;
        }
        placementRequested = true;
        placementRequestAge = player.tickCount;
        requestData = blockData;
        requestSlot = selectedSlot;
        requestHand = selectedHand;
        pendingPacketRotation = computed;
    }

    private static boolean selectBlockSlot(LocalPlayer player) {
        ItemStack offhand = player.getOffhandItem();
        if (isValidBlock(offhand)) {
            selectedSlot = -1;
            selectedHand = InteractionHand.OFF_HAND;
            return true;
        }
        int current = player.getInventory().selected;
        if (isValidBlock(player.getMainHandItem())) {
            selectedSlot = current;
            selectedHand = InteractionHand.MAIN_HAND;
            return true;
        }
        int best = -1;
        int bestCount = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isValidBlock(stack) && stack.getCount() > bestCount) {
                best = slot;
                bestCount = stack.getCount();
            }
        }
        if (best < 0) {
            return false;
        }
        selectedSlot = best;
        selectedHand = InteractionHand.MAIN_HAND;
        if (selectedHand == InteractionHand.MAIN_HAND && "None".equals(VisualSettings.scaffoldSwapMode())) {
            return false;
        }
        if (best != current) {
            restoreSlot = current;
            restoreSlotPending = "Silent".equals(VisualSettings.scaffoldSwapMode());
            player.getInventory().selected = best;
        }
        return true;
    }

    private static int selectedSlot = -1;
    private static InteractionHand selectedHand = InteractionHand.MAIN_HAND;

    private static void updateBlockTarget(LocalPlayer player) {
        BlockPos playerBlock = player.blockPosition();
        BlockData possible = null;
        if (isIgnoredBlock(MC.level.getBlockState(playerBlock))) {
            possible = getBlockData(new BlockPos((int) Math.floor(player.getX()), posY, (int) Math.floor(player.getZ())));
        }
        if (possible != null) {
            blockData = possible;
        }
        lastBlockData = possible;
        if (!isBlockDataUsable(blockData)) {
            blockData = possible;
        }
    }

    private static void updateRescueState(LocalPlayer player) {
        if (blockData == null) {
            return;
        }
        double predictedY = player.getY() + player.getDeltaMovement().y - 0.08D;
        if (blockData.pos().getY() > predictedY
                || player.getEyePosition().distanceTo(Vec3.atCenterOf(blockData.pos())) >= VisualSettings.scaffoldSilenceTellySafeDistance()) {
            movementCancelTicks = Math.max(movementCancelTicks, 1);
            if (rotateCount++ >= MAX_RESCUE_ROTATE_TICKS) {
                rotateCount = 0;
            }
        } else {
            rotateCount = 0;
        }
    }

    private static float[] computeRotation(LocalPlayer player) {
        if (blockData == null) {
            return null;
        }
        float[] reference = getReferenceRotation(player);
        Vec3 eyePos = player.getEyePosition();
        float[] target = getClosestToBlockFace(blockData, reference, eyePos,
                VisualSettings.scaffoldSilenceTellyFixRotation());
        if (target == null) {
            float plus = player.getYRot() + 100.0F;
            float minus = player.getYRot() - 100.0F;
            target = Math.abs(CombatAimRules.wrapDegrees(plus - reference[0]))
                    < Math.abs(CombatAimRules.wrapDegrees(minus - reference[0]))
                    ? new float[]{plus, reference[1]}
                    : new float[]{minus, reference[1]};
        }
        if (movementCancelTicks > 0) {
            return getClosestToBlockFace(blockData, reference, eyePos, true);
        }

        String mode = VisualSettings.scaffoldSilenceTellyMode();
        if ("Telly".equals(mode)) {
            if (MC.options.keyJump.isDown() && VisualSettings.scaffoldSilenceTellyNoUptelly()) {
                return rememberUsable(target, eyePos);
            }
            if (MC.options.keyJump.isDown() && VisualSettings.scaffoldSilenceTellySlowUpTelly()) {
                ups++;
                if (ups % 2 == 0) {
                    return rememberUsable(target, eyePos);
                }
            }
            int rotationTick = VisualSettings.scaffoldSilenceTellyRotationTick();
            if (VisualSettings.scaffoldSilenceTellyHeypixelUpTelly()
                    && (airTicks < rotationTick || VisualSettings.scaffoldSilenceTellySafeMode())) {
                if (groundTicks > 0) {
                    if (VisualSettings.scaffoldSilenceTellySafeMode()
                            && (!VisualSettings.scaffoldSilenceTellyTestOnGround() || MC.options.keyJump.isDown())) {
                        if (groundTicks == 1) {
                            target = new float[]{reference[0] + CombatAimRules.wrapDegrees(target[0] - reference[0]) / 2.0F, 75.5F};
                        } else if (groundTicks == 2) {
                            return new float[]{player.getYRot(), 75.5F};
                        }
                    } else {
                        return new float[]{player.getYRot(), 75.5F};
                    }
                } else {
                    float limit = airTicks == 1 ? 80.0F : 50.0F;
                    float diff = CombatAimRules.wrapDegrees(target[0] - reference[0]);
                    target = new float[]{reference[0] + Math.max(-limit, Math.min(limit, diff)), target[1]};
                }
            } else if (airTicks < rotationTick) {
                if (!VisualSettings.scaffoldSilenceTellySnap() || MC.options.keyJump.isDown()) {
                    return new float[]{player.getYRot(), 85.0F + (float) Math.random()};
                }
                if (lastBlockData == null) {
                    return new float[]{player.getYRot(), 85.0F + (float) Math.random()};
                }
            }
        } else if ("Normal".equals(mode) && VisualSettings.scaffoldSilenceTellyGodBridge()) {
            target = new float[]{movementYaw(player), 75.5F};
        }
        return rememberUsable(target, eyePos);
    }

    private static float[] rememberUsable(float[] target, Vec3 eyePos) {
        if (target == null || blockData == null) {
            return target;
        }
        if (lastRotation != null && hitsBlockFace(lastRotation, eyePos, blockData)) {
            return lastRotation;
        }
        lastRotation = target;
        return target;
    }

    private static float[] getReferenceRotation(LocalPlayer player) {
        return serverRotation != null ? serverRotation
                : new float[]{player.getYRot(), player.getXRot()};
    }

    private static float[] applyDuplicateRotation(float[] target) {
        if (target == null || !VisualSettings.scaffoldSilenceTellyDuplicateRotPlace()) {
            return target;
        }
        float yaw = target[0] - random(0.0001F, 0.0003F);
        float pitch = target[1] - random(0.001F, 0.003F);
        pitch = Math.max(-90.0F, Math.min(90.0F, pitch));
        return new float[]{yaw, pitch};
    }

    private static float[] applyFixedSensitivity(LocalPlayer player, float[] target) {
        if (target == null || !VisualSettings.scaffoldSilenceTellyFixRotation()) {
            return target;
        }
        return patchSensitivity(target, getReferenceRotation(player));
    }

    private static float[] patchSensitivity(float[] rot, float[] reference) {
        double sensitivity = MC.options.sensitivity().get() * 0.6D + 0.2D;
        double multiplier = sensitivity * sensitivity * sensitivity * 8.0D;
        double divisor = multiplier * 0.15D;
        if (divisor <= 0.0D) {
            return rot;
        }
        float yaw = reference[0] + (float) (Math.round((rot[0] - reference[0]) / divisor) * divisor);
        float pitch = reference[1] + (float) (Math.round((rot[1] - reference[1]) / divisor) * divisor);
        return new float[]{yaw, pitch};
    }

    private static float[] getClosestToBlockFace(BlockData data, float[] reference, Vec3 eyePos, boolean fixSensitivity) {
        float[] best = null;
        double bestDifference = Double.MAX_VALUE;
        for (double first : FACE_OFFSETS) {
            for (double second : FACE_OFFSETS) {
                if (Math.abs(first) == 0.45D && Math.abs(second) == 0.45D) {
                    continue;
                }
                Vec3 hitVec = faceHitVec(data.pos(), data.facing(), first, second);
                float[] raw = rotationTo(eyePos, hitVec);
                float[] candidate = fixSensitivity ? patchSensitivity(raw, reference) : raw;
                if (!hitsBlockFace(candidate, eyePos, data)) {
                    continue;
                }
                double difference = rotationDifference(reference, candidate);
                if (difference < bestDifference) {
                    bestDifference = difference;
                    best = candidate;
                }
            }
        }
        if (best != null) {
            return best;
        }
        float[] center = rotationTo(eyePos, faceHitVec(data.pos(), data.facing(), 0.0D, 0.0D));
        float[] fallback = fixSensitivity ? patchSensitivity(center, reference) : center;
        return hitsBlockFace(fallback, eyePos, data) ? fallback : null;
    }

    private static Vec3 faceHitVec(BlockPos pos, Direction face, double first, double second) {
        double x = pos.getX() + 0.5D + face.getStepX() * 0.5D;
        double y = pos.getY() + 0.5D + face.getStepY() * 0.5D;
        double z = pos.getZ() + 0.5D + face.getStepZ() * 0.5D;
        switch (face.getAxis()) {
            case X -> {
                y += first;
                z += second;
            }
            case Y -> {
                x += first;
                z += second;
            }
            case Z -> {
                x += first;
                y += second;
            }
        }
        return new Vec3(x, y, z);
    }

    private static float[] rotationTo(Vec3 from, Vec3 to) {
        CombatAimRules.Rotation rotation = CombatAimRules.rotationBetween(
                from.x, from.y, from.z, to.x, to.y, to.z);
        return new float[]{rotation.yaw(), rotation.pitch()};
    }

    private static double rotationDifference(float[] from, float[] to) {
        float yaw = CombatAimRules.wrapDegrees(to[0] - from[0]);
        float pitch = to[1] - from[1];
        return Math.sqrt(yaw * yaw + pitch * pitch);
    }

    // ------------------------------------------------------------------
    // Placement
    // ------------------------------------------------------------------

    private static boolean tryPlaceArmed(LocalPlayer player) {
        if (armedData == null || armedRotation == null || placementArmedAge != player.tickCount
                || lastPlaceTick == player.tickCount) {
            return false;
        }
        if (MC.getConnection() == null || !isSlotValid(armedSlot, armedHand)) {
            return false;
        }
        Vec3 eyePos = player.getEyePosition();
        if (!hitsBlockFace(armedRotation, eyePos, armedData)) {
            return false;
        }
        BlockHitResult hit = raycast(eyePos, armedRotation);
        if (hit == null || !hit.getBlockPos().equals(armedData.pos()) || hit.getDirection() != armedData.facing()) {
            return false;
        }
        if (!isPlacementAllowed(player, armedData)) {
            return false;
        }
        if (VisualSettings.scaffoldSilenceTellyBlockFly()) {
            sendSwapOffhand();
            blockFlyPending = true;
            blockFlyTicks = 0;
        }
        if (VisualSettings.scaffoldInteractBeforePlace() && armedHand == InteractionHand.MAIN_HAND) {
            MC.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        }
        if (VisualSettings.scaffoldSilenceTellyAbuseRotation()) {
            int times = (int) (Math.abs(CombatAimRules.wrapDegrees(armedRotation[0] - (lastRotation == null ? armedRotation[0] : lastRotation[0]))) / 30.0F);
            for (int i = 0; i < times; i++) {
                MC.gameMode.useItem(player, armedHand);
            }
        }
        InteractionResult result = MC.gameMode.useItemOn(player, armedHand, hit);
        if (!result.consumesAction()) {
            return false;
        }
        if (VisualSettings.scaffoldSwingHand()) {
            player.swing(armedHand);
        } else {
            MC.getConnection().send(new ServerboundSwingPacket(armedHand));
        }
        lastPlaceTick = player.tickCount;
        if (serverRotation != null) {
            lastPlacedYawDelta = Math.abs(CombatAimRules.wrapDegrees(armedRotation[0] - serverRotation[0]));
        }
        serverRotation = armedRotation;
        if (restoreSlotPending && restoreSlot >= 0) {
            player.getInventory().selected = restoreSlot;
            restoreSlotPending = false;
            restoreSlot = -1;
        }
        debug("place ok hand=" + armedHand + " face=" + armedData.facing());
        return true;
    }

    private static void lastPlacedYawDeltaCandidate(float[] from, float[] to) {
        lastPlacedYawDelta = Math.abs(CombatAimRules.wrapDegrees(to[0] - from[0]));
    }

    private static boolean isPlacementAllowed(LocalPlayer player, BlockData data) {
        if (!isPosSolid(data.pos()) || !isReplaceableForPlacement(data.placePos())) {
            return false;
        }
        BlockState state = MC.level.getBlockState(data.placePos());
        if (!state.getCollisionShape(MC.level, data.placePos()).isEmpty()) {
            AABB box = state.getCollisionShape(MC.level, data.placePos()).bounds().move(data.placePos());
            if (player.getBoundingBox().intersects(box)) {
                return player.getDeltaMovement().y <= 0.0D
                        && player.getBoundingBox().minY >= box.maxY - 1.0E-3D;
            }
        }
        return true;
    }

    private static void tickBlockFly(LocalPlayer player) {
        if (!blockFlyPending) {
            return;
        }
        if (++blockFlyTicks > 2) {
            sendSwapOffhand();
            blockFlyPending = false;
            blockFlyTicks = 0;
        }
    }

    private static void sendSwapOffhand() {
        if (MC.getConnection() == null) {
            return;
        }
        MC.getConnection().send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                BlockPos.ZERO,
                Direction.DOWN));
    }

    // ------------------------------------------------------------------
    // Block search
    // ------------------------------------------------------------------

    private static BlockData getBlockData(BlockPos pos) {
        if (lastSearchTick == MC.player.tickCount && pos.equals(lastSearchOrigin)) {
            return lastSearchData;
        }
        BlockData data = getPos(pos);
        if (data == null) {
            BlockPos support = findSupportPos();
            if (support == null) {
                return cacheSearch(pos, null);
            }
            Direction face = getPlaceSide(support);
            if (face == null) {
                return cacheSearch(pos, null);
            }
            data = new BlockData(support, face);
        }
        return cacheSearch(pos, isReplaceableForPlacement(data.placePos()) ? data : null);
    }

    private static BlockData cacheSearch(BlockPos origin, BlockData data) {
        lastSearchTick = MC.player == null ? -1 : MC.player.tickCount;
        lastSearchOrigin = origin == null ? null : origin.immutable();
        lastSearchData = data;
        return data;
    }

    private static BlockData getPos(BlockPos pos) {
        if (isPosSolid(pos.offset(-1, 0, 0))) {
            return new BlockData(pos.offset(-1, 0, 0), Direction.EAST);
        }
        if (isPosSolid(pos.offset(1, 0, 0))) {
            return new BlockData(pos.offset(1, 0, 0), Direction.WEST);
        }
        if (isPosSolid(pos.offset(0, 0, 1))) {
            return new BlockData(pos.offset(0, 0, 1), Direction.NORTH);
        }
        if (isPosSolid(pos.offset(0, 0, -1))) {
            return new BlockData(pos.offset(0, 0, -1), Direction.SOUTH);
        }
        if (isPosSolid(pos.offset(0, -1, 0))) {
            return new BlockData(pos.offset(0, -1, 0), Direction.UP);
        }
        return null;
    }

    private static BlockPos findSupportPos() {
        LocalPlayer player = MC.player;
        BlockPos origin = player.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= 0; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos candidate = origin.offset(x, y, z);
                    if (candidate.getY() >= origin.getY() || !isPosSolid(candidate)) {
                        continue;
                    }
                    double distance = candidate.distSqr(origin);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = candidate;
                    }
                }
            }
        }
        return best;
    }

    private static Direction getPlaceSide(BlockPos support) {
        BlockPos playerBlock = MC.player.blockPosition();
        Direction best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Direction face : new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST}) {
            BlockPos placePos = support.relative(face);
            if (placePos.equals(playerBlock) || !isReplaceableForPlacement(placePos)) {
                continue;
            }
            double distance = placePos.distSqr(playerBlock);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = face;
            }
        }
        return best;
    }

    private static boolean isPosSolid(BlockPos pos) {
        if (MC.level == null || MC.level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState state = MC.level.getBlockState(pos);
        Block block = state.getBlock();
        return !state.isAir()
                && !state.canBeReplaced()
                && state.getFluidState().isEmpty()
                && !state.getCollisionShape(MC.level, pos).isEmpty()
                && !isIgnoredBlock(state)
                && !(block instanceof TrapDoorBlock)
                && !(block instanceof DoorBlock)
                && !(block instanceof FenceGateBlock)
                && block != Blocks.COBWEB
                && block != Blocks.FIRE;
    }

    private static boolean isReplaceableForPlacement(BlockPos pos) {
        if (MC.level == null || MC.level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState state = MC.level.getBlockState(pos);
        return state.canBeReplaced() || isIgnoredBlock(state);
    }

    private static boolean isIgnoredBlock(BlockState state) {
        Block block = state.getBlock();
        return state.isAir() || block instanceof BushBlock || block instanceof SnowLayerBlock;
    }

    private static boolean isBlockDataUsable(BlockData data) {
        return data != null && isPosSolid(data.pos()) && isReplaceableForPlacement(data.placePos());
    }

    private static boolean isValidBlock(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return !(block instanceof SlabBlock) && !(block instanceof BushBlock) && block != Blocks.AIR;
    }

    private static boolean isSlotValid(int slot, InteractionHand hand) {
        if (MC.player == null) {
            return false;
        }
        return isValidBlock(hand == InteractionHand.OFF_HAND
                ? MC.player.getOffhandItem()
                : MC.player.getInventory().getItem(slot));
    }

    // ------------------------------------------------------------------
    // Movement / jump / eagle
    // ------------------------------------------------------------------

    private static boolean shouldJump(LocalPlayer player, net.minecraft.client.player.Input input) {
        if (!player.onGround() || player.isShiftKeyDown() || input.shiftKeyDown
                || MC.options.keyJump.isDown() || MC.options.keyUse.isDown()) {
            return false;
        }
        boolean moving = Math.abs(input.forwardImpulse) > 1.0E-4F || Math.abs(input.leftImpulse) > 1.0E-4F;
        if (!moving || groundTicks <= 0) {
            return false;
        }
        return switch (VisualSettings.scaffoldSilenceTellyJumpMode()) {
            case "None" -> false;
            case "Parkour" -> shouldParkourJump(player);
            default -> true;
        };
    }

    private static boolean shouldParkourJump(LocalPlayer player) {
        double yaw = Math.toRadians(player.getYRot());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        BlockPos frontOne = BlockPos.containing(player.getX() + forwardX, player.getY() - 0.1D, player.getZ() + forwardZ);
        BlockPos frontTwo = BlockPos.containing(player.getX() + forwardX * 2.0D, player.getY() - 0.1D, player.getZ() + forwardZ * 2.0D);
        return MC.level.getBlockState(frontOne).isAir() || MC.level.getBlockState(frontTwo).isAir();
    }

    private static void updateEagleSneak(net.minecraft.client.player.Input input) {
        if (!waitingEagle) {
            return;
        }
        eagleTicks++;
        int start = VisualSettings.scaffoldSilenceTellyEagleTick();
        int end = start + VisualSettings.scaffoldSilenceTellyKeepEagleTick();
        if (eagleTicks == start && !MC.options.keyShift.isDown()) {
            MC.options.keyShift.setDown(true);
            input.shiftKeyDown = true;
            modulePressedSneak = true;
        }
        if (eagleTicks >= end) {
            releaseSneak();
            waitingEagle = false;
            eagleTicks = 0;
        }
    }

    private static void releaseSneak() {
        if (modulePressedSneak) {
            MC.options.keyShift.setDown(false);
            modulePressedSneak = false;
        }
    }

    private static float movementYaw(LocalPlayer player) {
        if (player.input == null) {
            return player.getYRot();
        }
        float forward = player.input.forwardImpulse;
        float sideways = player.input.leftImpulse;
        float yaw = player.getYRot();
        if (forward < 0.0F) {
            yaw += 180.0F;
        }
        float factor = forward < 0.0F ? -0.5F : forward > 0.0F ? 0.5F : 1.0F;
        if (sideways > 0.0F) {
            yaw -= 90.0F * factor;
        }
        if (sideways < 0.0F) {
            yaw += 90.0F * factor;
        }
        return yaw;
    }

    // ------------------------------------------------------------------
    // Raycast
    // ------------------------------------------------------------------

    private static boolean hitsBlockFace(float[] rot, Vec3 eyePos, BlockData data) {
        if (rot == null || eyePos == null || data == null) {
            return false;
        }
        BlockHitResult hit = raycast(eyePos, rot);
        return hit != null && hit.getBlockPos().equals(data.pos()) && hit.getDirection() == data.facing();
    }

    private static BlockHitResult raycast(Vec3 eyePos, float[] rot) {
        LocalPlayer player = MC.player;
        if (player == null || MC.level == null) {
            return null;
        }
        Vec3 direction = Vec3.directionFromRotation(rot[1], rot[0]);
        Vec3 end = eyePos.add(direction.scale(REACH));
        HitResult result = MC.level.clip(new ClipContext(
                eyePos, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (result instanceof BlockHitResult blockHit && result.getType() == HitResult.Type.BLOCK) {
            return blockHit;
        }
        return null;
    }

    // ------------------------------------------------------------------
    // State helpers
    // ------------------------------------------------------------------

    private static void clearTarget() {
        clearPlacementRequest();
        clearPlacementArmed();
        blockData = null;
        rotation = null;
        movementCancelTicks = 0;
        rotateCount = 0;
    }

    private static void clearPlacementRequest() {
        placementRequested = false;
        placementRequestAge = -1;
        requestData = null;
        requestSlot = -1;
        pendingPacketRotation = null;
        pendingEyePos = null;
    }

    private static void clearPlacementArmed() {
        placementArmed = false;
        placementArmedAge = -1;
        armedData = null;
        armedSlot = -1;
        armedRotation = null;
    }

    private static float random(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    private static void debug(String message) {
        if (!VisualSettings.scaffoldSilenceTellyDebug() || MC.player == null) {
            return;
        }
        if (message.equals(lastDebug) && MC.player.tickCount - lastDebugTick < 8) {
            return;
        }
        lastDebug = message;
        lastDebugTick = MC.player.tickCount;
        LOGGER.info("[SilenceTelly] {}", message);
    }

    private record BlockData(BlockPos pos, Direction facing) {
        private BlockPos placePos() {
            return pos.relative(facing);
        }
    }
}
