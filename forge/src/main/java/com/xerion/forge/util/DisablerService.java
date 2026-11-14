package com.xerion.forge.util;

import com.xerion.common.config.DisablerSettings;
import com.xerion.forge.config.VisualSettings;
import com.xerion.forge.packet.PacketBlockageService;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Port of OpenOpal's DisablerModule runtime. Heypixel and CubeCraft are wired
 * against their 1.20.1 protocol equivalents; MiniBlox mirrors each movement
 * packet with a {@code miniblox:movepacket} custom payload, matching the
 * OpenOpal sidecar format on the native 1.20.1 connection.
 */
public final class DisablerService {
    private static final Logger LOGGER = LoggerFactory.getLogger("xerion-disabler");

    private static final long CUBECRAFT_PACKET_DELAY_MS = 2_000L;
    private static final long CUBECRAFT_WAIT_AFTER_LAG_MS = 5_000L;
    private static final long INVENTORY_MIN_OPEN_MS = 150L;
    private static final long THEMIS_BLINK_INTERVAL_MS = 200L;
    private static final long MINIBLOX_DEBUG_INTERVAL_MS = 2_000L;
    private static final ResourceLocation MINIBLOX_MOVE_PAYLOAD_ID =
            Objects.requireNonNull(ResourceLocation.tryParse("miniblox:movepacket"));

    private static final double[] PERFECT_ROTATION_STEPS = {
            0.0D, 5.625D, 11.25D, 16.875D, 22.5D, 28.125D, 33.75D, 39.375D,
            45.0D, 50.625D, 56.25D, 61.875D, 67.5D, 73.125D, 78.75D, 84.375D, 90.0D
    };

    private static final Queue<PacketEntry> CUBECRAFT_QUEUE = new ConcurrentLinkedQueue<>();
    private static boolean bypassing;
    private static boolean lastActive;
    private static String activeMode = "";

    // Heypixel slot / inventory state.
    private static int lastSentSlot = -1;
    private static long inventoryOpenTime;
    private static boolean inventoryOpen;
    private static ServerboundContainerClosePacket storedClosePacket;
    private static long storedCloseReleaseAt;

    // Heypixel rotation state.
    private static float lastYaw;
    private static float lastPitch;
    private static float currentYaw;
    private static float currentPitch;
    private static float yawDiff;
    private static float pitchDiff;
    private static float lastPlacedYawDiff;
    private static float lastPlacedPitchDiff;
    private static boolean rotated;
    private static long themisBlinkLastSend;
    private static int themisBlinkCount;

    // CubeCraft lag state.
    private static long waitStartedAt;
    private static boolean lagDetected;
    private static boolean waitingLastTick;
    private static long disabledStartedAt;
    private static boolean ignoreNextCorrection;

    // MiniBlox sidecar state.
    private static boolean sendingMiniBloxPayload;
    private static boolean minibloxWarned;
    private static boolean blinkConflictResolved;
    private static long minibloxPacketsTotal;
    private static long minibloxWindowPackets;
    private static long minibloxWindowStartedAt;

    private DisablerService() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean active = canProcess(minecraft);
        if (!active) {
            if (lastActive) {
                flushCubecraftQueue();
            }
            resetRuntimeState();
            lastActive = false;
            return;
        }

        if (!DisablerSettings.mode().equals(activeMode)) {
            flushCubecraftQueue();
            resetRuntimeState();
            activeMode = DisablerSettings.mode();
        }
        lastActive = true;

        String mode = activeMode;
        if (DisablerSettings.MODE_CUBECRAFT.equals(mode)) {
            tickCubecraft();
        } else if (DisablerSettings.MODE_MINIBLOX.equals(mode)) {
            tickMiniBlox();
        } else {
            tickHeypixel();
        }
    }

    /**
     * Runs after every outbound packet survived the capture chain. When the
     * MiniBlox mode is active and the packet is a movement packet that was not
     * cancelled by Blink / Bypass processing, this mirrors it with the
     * MiniBlox sidecar payload so a cancelled movement never leaves an orphan
     * payload behind.
     */
    public static void maybeSendMiniBloxPayload(Packet<?> packet) {
        if (bypassing
                || sendingMiniBloxPayload
                || !canProcess(Minecraft.getInstance())
                || !DisablerSettings.MODE_MINIBLOX.equals(DisablerSettings.mode())
                || !(packet instanceof ServerboundMovePlayerPacket movement)) {
            return;
        }
        sendMiniBloxMovePayload(movement);
    }

    public static boolean captureOutbound(Packet<?> packet) {
        if (bypassing || !canProcess(Minecraft.getInstance())) {
            return false;
        }
        String mode = DisablerSettings.mode();
        if (DisablerSettings.MODE_CUBECRAFT.equals(mode)) {
            return captureCubecraft(packet);
        }
        if (DisablerSettings.MODE_HEYPIXEL.equals(mode)) {
            return captureHeypixel(packet);
        }
        return false;
    }

    public static void onInventoryOpened() {
        if (!DisablerSettings.enabled()
                || !DisablerSettings.MODE_HEYPIXEL.equals(DisablerSettings.mode())) {
            return;
        }
        inventoryOpenTime = System.currentTimeMillis();
        inventoryOpen = true;
    }

    public static void onServerCorrection(ClientboundPlayerPositionPacket packet) {
        if (!DisablerSettings.enabled()
                || !DisablerSettings.MODE_CUBECRAFT.equals(DisablerSettings.mode())) {
            return;
        }
        if (canProcess(Minecraft.getInstance())) {
            if (ignoreNextCorrection) {
                ignoreNextCorrection = false;
                return;
            }
            lagDetected = true;
            waitStartedAt = System.currentTimeMillis();
            flushCubecraftQueue();
        }
    }

    public static void clearRuntimeState() {
        flushCubecraftQueue();
        resetRuntimeState();
        ignoreNextCorrection = true;
        activeMode = "";
        lastActive = false;
    }

    public static boolean isCubeCraftWaiting() {
        return DisablerSettings.enabled()
                && DisablerSettings.MODE_CUBECRAFT.equals(DisablerSettings.mode())
                && isWaiting();
    }

    private static boolean captureCubecraft(Packet<?> packet) {
        if (isWaiting()) {
            return false;
        }
        if (packet instanceof ServerboundPlayerCommandPacket command
                && command.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
            return true;
        }
        if (packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundPongPacket) {
            CUBECRAFT_QUEUE.add(new PacketEntry(packet, System.currentTimeMillis()));
            return true;
        }
        return false;
    }

    private static void tickCubecraft() {
        boolean waiting = isWaiting();
        if (waitingLastTick && !waiting) {
            disabledStartedAt = System.currentTimeMillis();
        }
        waitingLastTick = waiting;
        if (waiting) {
            return;
        }

        long now = System.currentTimeMillis();
        PacketEntry entry;
        while ((entry = CUBECRAFT_QUEUE.peek()) != null
                && now - entry.queuedAt() >= CUBECRAFT_PACKET_DELAY_MS) {
            CUBECRAFT_QUEUE.poll();
            sendDirect(entry.packet());
        }
    }

    private static boolean captureHeypixel(Packet<?> packet) {
        if (packet instanceof ServerboundSetCarriedItemPacket slotPacket) {
            return captureSelectedSlot(slotPacket);
        }
        if (packet instanceof ServerboundContainerClosePacket closePacket) {
            if (DisablerSettings.heypixelAcaInventoryFrequency()
                    && inventoryOpen
                    && System.currentTimeMillis() - inventoryOpenTime <= INVENTORY_MIN_OPEN_MS) {
                storedClosePacket = closePacket;
                storedCloseReleaseAt = inventoryOpenTime + INVENTORY_MIN_OPEN_MS + 1L;
                inventoryOpen = false;
                log("InventoryFrequency: stored close packet");
                return true;
            }
            inventoryOpen = false;
            return false;
        }
        if (DisablerSettings.heypixelThemisBlink()
                && (packet instanceof ServerboundMovePlayerPacket.StatusOnly
                || packet instanceof ServerboundPongPacket)) {
            themisBlinkCount++;
        }
        if (packet instanceof ServerboundMovePlayerPacket movement && movement.hasRotation()) {
            return captureRotationPacket(movement);
        }
        if (packet instanceof ServerboundUseItemOnPacket) {
            if (rotated) {
                lastPlacedYawDiff = yawDiff;
                lastPlacedPitchDiff = pitchDiff;
                rotated = false;
            }
        }
        return false;
    }

    private static boolean captureSelectedSlot(ServerboundSetCarriedItemPacket packet) {
        int slot = packet.getSlot();
        if (DisablerSettings.heypixelGrimBadPacketsA() && slot == lastSentSlot && slot != -1) {
            log("BadPacketsA: cancelled duplicate slot packet " + slot);
            return true;
        }
        if (DisablerSettings.heypixelAcaFastSwitch()
                && lastSentSlot != -1
                && slot != lastSentSlot) {
            sendIntermediateSlots(lastSentSlot, slot);
        }
        lastSentSlot = slot;
        return false;
    }

    private static boolean captureRotationPacket(ServerboundMovePlayerPacket movement) {
        boolean rotateOnly = movement instanceof ServerboundMovePlayerPacket.Rot;
        boolean positionAndRotation = movement instanceof ServerboundMovePlayerPacket.PosRot;
        if (!rotateOnly && !positionAndRotation) {
            return false;
        }

        float yaw = movement.getYRot(0.0F);
        float pitch = movement.getXRot(0.0F);
        boolean modified = false;

        if (DisablerSettings.heypixelGrimDuplicateRotPlace()) {
            float previousYaw = currentYaw;
            float previousPitch = currentPitch;
            currentYaw = yaw;
            currentPitch = pitch;
            yawDiff = Math.abs(currentYaw - previousYaw);
            pitchDiff = Math.abs(currentPitch - previousPitch);
            rotated = true;

            if (yawDiff > 2.0F && Math.abs(yawDiff - lastPlacedYawDiff) < 1.0E-4F) {
                yaw -= jitter(0.001F, 0.009F);
                modified = true;
                log("DuplicateRotPlace: modified yaw");
            }
            if (pitchDiff > 2.0F && Math.abs(pitchDiff - lastPlacedPitchDiff) < 1.0E-4F) {
                pitch = Mth.clamp(pitch - jitter(0.001F, 0.009F), -90.0F, 90.0F);
                modified = true;
                log("DuplicateRotPlace: modified pitch");
            }
        }

        if ((DisablerSettings.heypixelAcaAimStep() || DisablerSettings.heypixelAcaPerfectRotation())
                && (lastYaw != 0.0F || lastPitch != 0.0F)) {
            float adjustedYaw = yaw;
            float adjustedPitch = pitch;
            if (DisablerSettings.heypixelAcaAimStep()) {
                float[] stepped = applyAimStep(adjustedYaw, adjustedPitch);
                adjustedYaw = stepped[0];
                adjustedPitch = stepped[1];
            }
            if (DisablerSettings.heypixelAcaPerfectRotation()) {
                float[] perfect = applyPerfectRotation(adjustedYaw, adjustedPitch);
                adjustedYaw = perfect[0];
                adjustedPitch = perfect[1];
            }
            if (adjustedYaw != yaw || adjustedPitch != pitch) {
                yaw = adjustedYaw;
                pitch = Mth.clamp(adjustedPitch, -90.0F, 90.0F);
                modified = true;
                log("PerfectRotation/AimStep: adjusted rotation");
            }
        }

        lastYaw = yaw;
        lastPitch = pitch;
        if (!modified) {
            return false;
        }

        if (positionAndRotation) {
            sendDirect(new ServerboundMovePlayerPacket.PosRot(
                    movement.getX(0.0D),
                    movement.getY(0.0D),
                    movement.getZ(0.0D),
                    yaw,
                    pitch,
                    movement.isOnGround()));
        } else {
            sendDirect(new ServerboundMovePlayerPacket.Rot(yaw, pitch, movement.isOnGround()));
        }
        return true;
    }

    private static float[] applyAimStep(float yaw, float pitch) {
        double yawDelta = Math.abs(Mth.wrapDegrees(yaw - lastYaw));
        double pitchDelta = Math.abs(pitch - lastPitch);
        float newYaw = yaw;
        float newPitch = pitch;
        if (lastYaw != 0.0F && yawDelta < 1.0E-5D && pitchDelta > 1.0D) {
            newYaw = lastYaw + (float) (ThreadLocalRandom.current().nextGaussian() * 0.001D);
        }
        if (lastPitch != 0.0F && pitchDelta < 1.0E-5D && yawDelta > 1.0D) {
            newPitch = lastPitch + (float) (ThreadLocalRandom.current().nextGaussian() * 0.001D);
        }
        return new float[]{newYaw, newPitch};
    }

    private static float[] applyPerfectRotation(float yaw, float pitch) {
        double yawDelta = Math.abs(Mth.wrapDegrees(yaw - lastYaw));
        double pitchDelta = Math.abs(pitch - lastPitch);
        float newYaw = yaw;
        float newPitch = pitch;
        if (lastYaw != 0.0F && !isNearZeroOrMultiple(yawDelta) && isKnownRotationStep(yawDelta)) {
            newYaw = yaw + (float) (ThreadLocalRandom.current().nextGaussian() * 0.005D);
        }
        if (lastPitch != 0.0F && !isNearZeroOrMultiple(pitchDelta) && isKnownRotationStep(pitchDelta)) {
            newPitch = pitch + (float) (ThreadLocalRandom.current().nextGaussian() * 0.005D);
        }
        return new float[]{newYaw, newPitch};
    }

    private static float jitter(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    private static boolean isNearZeroOrMultiple(double value) {
        return Math.abs(value) <= 1.0E-10D || isMultipleOf(360.0D, value);
    }

    private static boolean isKnownRotationStep(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            return false;
        }
        for (double step : PERFECT_ROTATION_STEPS) {
            if (isMultipleOf(step, value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMultipleOf(double base, double value) {
        if (base == 0.0D) {
            return Math.abs(value) <= 1.0E-10D;
        }
        double ratio = value / base;
        return Math.abs(ratio - Math.round(ratio)) <= 1.0E-10D;
    }

    private static void sendIntermediateSlots(int fromSlot, int toSlot) {
        int distance = Math.abs(fromSlot - toSlot);
        if (distance <= 1 || isWrapAroundSlot(fromSlot, toSlot)) {
            return;
        }
        int step = fromSlot > toSlot ? -1 : 1;
        for (int slot = fromSlot + step; slot != toSlot; slot += step) {
            if (slot < 0 || slot > 8) {
                continue;
            }
            sendDirect(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    private static boolean isWrapAroundSlot(int fromSlot, int toSlot) {
        return (fromSlot == 0 && toSlot == 8) || (fromSlot == 8 && toSlot == 0);
    }

    private static void tickHeypixel() {
        if (storedClosePacket != null && System.currentTimeMillis() >= storedCloseReleaseAt) {
            sendDirect(storedClosePacket);
            storedClosePacket = null;
            storedCloseReleaseAt = 0L;
        }
        if (DisablerSettings.heypixelThemisBlink()
                && System.currentTimeMillis() - themisBlinkLastSend > THEMIS_BLINK_INTERVAL_MS) {
            if (themisBlinkCount == 0) {
                sendDirect(new ServerboundPongPacket(0));
            }
            themisBlinkLastSend = System.currentTimeMillis();
            themisBlinkCount = 0;
        }
    }

    private static void tickMiniBlox() {
        if (!minibloxWarned) {
            minibloxWarned = true;
            log("MiniBlox active; mirroring movement with miniblox:movepacket payloads");
        }
        resolveMiniBloxBufferingConflicts();
        if (DisablerSettings.debug()
                && System.currentTimeMillis() - minibloxWindowStartedAt >= MINIBLOX_DEBUG_INTERVAL_MS) {
            log(minibloxWindowPackets + " payloads/2s"
                    + " | total=" + minibloxPacketsTotal);
            minibloxWindowPackets = 0;
            minibloxWindowStartedAt = System.currentTimeMillis();
        }
    }

    private static void resolveMiniBloxBufferingConflicts() {
        if (blinkConflictResolved) {
            return;
        }
        if (VisualSettings.blinkEnabled()) {
            VisualSettings.setBlinkEnabled(false);
            log("MiniBlox: disabled Blink to preserve movement packet pairing");
        }
        blinkConflictResolved = true;
    }

    private static void sendMiniBloxMovePayload(ServerboundMovePlayerPacket movement) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        ClientPacketListener connection = minecraft == null ? null : minecraft.getConnection();
        if (player == null || connection == null) {
            return;
        }

        sendingMiniBloxPayload = true;
        try {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeDouble(finiteOrZero(player.xo));
            buffer.writeDouble(finiteOrZero(player.yo));
            buffer.writeDouble(finiteOrZero(player.zo));
            buffer.writeFloat(sanitizeAngle(movement.getYRot(player.getYRot())));
            buffer.writeFloat(sanitizeAngle(movement.getXRot(player.getXRot())));
            // 1.20.1 Input: forwardImpulse = W(+)/S(-), leftImpulse = A(+)/D(-);
            // these map to MiniBlox forward/sideways in the same order as the source.
            buffer.writeFloat(sanitize(player.input == null ? 0.0F : player.input.forwardImpulse));
            buffer.writeFloat(sanitize(player.input == null ? 0.0F : player.input.leftImpulse));
            buffer.writeBoolean(player.input != null && player.input.jumping);
            buffer.writeBoolean(player.input != null && player.input.shiftKeyDown);
            buffer.writeBoolean(movement.isOnGround());
            connection.send(new ServerboundCustomPayloadPacket(MINIBLOX_MOVE_PAYLOAD_ID, buffer));
            minibloxPacketsTotal++;
            minibloxWindowPackets++;
        } catch (Throwable throwable) {
            LOGGER.warn("[Disabler] MiniBlox move payload send failed: {}",
                    throwable.getClass().getSimpleName());
        } finally {
            sendingMiniBloxPayload = false;
        }
    }

    private static float sanitize(float value) {
        return Float.isFinite(value) ? Mth.clamp(value, -1.0F, 1.0F) : 0.0F;
    }

    private static float sanitizeAngle(float value) {
        return Float.isFinite(value) ? value : 0.0F;
    }

    private static double finiteOrZero(double value) {
        return Double.isFinite(value) ? value : 0.0D;
    }

    private static boolean canProcess(Minecraft minecraft) {
        if (!DisablerSettings.enabled() || minecraft == null) {
            return false;
        }
        if (DisablerSettings.onlyRemoteServer() && minecraft.hasSingleplayerServer()) {
            return false;
        }
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.getConnection() == null) {
            return false;
        }
        if (player.isSpectator() || !player.isAlive()) {
            return false;
        }
        return true;
    }

    private static boolean isWaiting() {
        return lagDetected && System.currentTimeMillis() - waitStartedAt < CUBECRAFT_WAIT_AFTER_LAG_MS;
    }

    private static void flushCubecraftQueue() {
        PacketEntry entry;
        while ((entry = CUBECRAFT_QUEUE.poll()) != null) {
            sendDirect(entry.packet());
        }
    }

    private static void sendDirect(Packet<?> packet) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener == null) {
            return;
        }
        bypassing = true;
        try {
            PacketBlockageService.runDirectSend(() -> listener.send(packet));
        } finally {
            bypassing = false;
        }
    }

    private static void log(String message) {
        if (DisablerSettings.logging()) {
            LOGGER.info("[Disabler] {}", message);
        }
    }

    private static void resetRuntimeState() {
        lastSentSlot = -1;
        inventoryOpenTime = 0L;
        inventoryOpen = false;
        storedClosePacket = null;
        storedCloseReleaseAt = 0L;
        lastYaw = 0.0F;
        lastPitch = 0.0F;
        currentYaw = 0.0F;
        currentPitch = 0.0F;
        yawDiff = 0.0F;
        pitchDiff = 0.0F;
        lastPlacedYawDiff = 0.0F;
        lastPlacedPitchDiff = 0.0F;
        rotated = false;
        themisBlinkLastSend = System.currentTimeMillis();
        themisBlinkCount = 0;
        lagDetected = false;
        waitingLastTick = false;
        waitStartedAt = 0L;
        disabledStartedAt = System.currentTimeMillis();
        ignoreNextCorrection = false;
        sendingMiniBloxPayload = false;
        minibloxWarned = false;
        blinkConflictResolved = false;
        minibloxPacketsTotal = 0L;
        minibloxWindowPackets = 0L;
        minibloxWindowStartedAt = System.currentTimeMillis();
    }

    private record PacketEntry(Packet<?> packet, long queuedAt) {
    }
}
