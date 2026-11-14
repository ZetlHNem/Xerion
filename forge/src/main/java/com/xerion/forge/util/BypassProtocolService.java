package com.xerion.forge.util;

import com.xerion.common.config.BypassSettings;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

import java.util.Objects;

/**
 * Keeps Xerion-generated protocol traffic internally consistent. This is a
 * compatibility layer, not an anti-cheat bypass.
 */
public final class BypassProtocolService {
    private static final long MOVE_DUPLICATE_WINDOW_NANOS = 5_000_000L;

    private static ServerboundPlayerCommandPacket.Action lastCommand;
    private static long lastCommandNanos;
    private static Boolean sprinting;
    private static MovePacketState lastMovePacket;
    private static long lastMovePacketNanos;
    private static long correctionRecoveryUntilNanos;

    private BypassProtocolService() {
    }

    public static boolean captureOutbound(Packet<?> packet) {
        if (!BypassSettings.enabled()) {
            return false;
        }
        if (packet instanceof ServerboundPlayerCommandPacket command) {
            return captureCommand(command);
        }
        if (packet instanceof ServerboundMovePlayerPacket movement) {
            return captureMovement(movement);
        }
        return false;
    }

    private static boolean captureCommand(ServerboundPlayerCommandPacket command) {
        if (!BypassSettings.actionDeduplication()) {
            return false;
        }

        long now = System.nanoTime();
        ServerboundPlayerCommandPacket.Action action = command.getAction();
        boolean duplicate = Objects.equals(action, lastCommand)
                && lastCommandNanos > 0L
                && now - lastCommandNanos >= 0L
                && now - lastCommandNanos < BypassSettings.actionDeduplicationWindowMs() * 1_000_000L;
        Boolean requestedSprintState = requestedSprintState(action);
        if (requestedSprintState != null && Objects.equals(requestedSprintState, sprinting)) {
            return true;
        }
        if (duplicate) {
            return true;
        }
        lastCommand = action;
        lastCommandNanos = now;
        if (requestedSprintState != null) {
            sprinting = requestedSprintState;
        }
        return false;
    }

    private static boolean captureMovement(ServerboundMovePlayerPacket packet) {
        if (!BypassSettings.packetDeduplication()) {
            return false;
        }
        long now = System.nanoTime();
        MovePacketState current = MovePacketState.from(packet);
        boolean duplicate = current.equals(lastMovePacket)
                && lastMovePacketNanos > 0L
                && now - lastMovePacketNanos >= 0L
                && now - lastMovePacketNanos < MOVE_DUPLICATE_WINDOW_NANOS;
        if (!duplicate) {
            lastMovePacket = current;
            lastMovePacketNanos = now;
        }
        return duplicate;
    }

    public static void onServerCorrection(Packet<?> packet) {
        if (!BypassSettings.enabled() || !BypassSettings.correctionRecovery()
                || !(packet instanceof ClientboundPlayerPositionPacket)) {
            return;
        }
        correctionRecoveryUntilNanos = System.nanoTime()
                + BypassSettings.correctionRecoveryWindowMs() * 1_000_000L;
        sprinting = null;
    }

    public static boolean correctionRecoveryActive() {
        return BypassSettings.enabled() && BypassSettings.correctionRecovery()
                && System.nanoTime() < correctionRecoveryUntilNanos;
    }

    public static void clearRuntimeState() {
        lastCommand = null;
        lastCommandNanos = 0L;
        sprinting = null;
        lastMovePacket = null;
        lastMovePacketNanos = 0L;
        correctionRecoveryUntilNanos = 0L;
    }

    private static Boolean requestedSprintState(ServerboundPlayerCommandPacket.Action action) {
        return switch (action) {
            case START_SPRINTING -> true;
            case STOP_SPRINTING -> false;
            default -> null;
        };
    }

    private record MovePacketState(boolean hasPosition, boolean hasRotation,
                                   double x, double y, double z,
                                   float yaw, float pitch, boolean onGround) {
        private static MovePacketState from(ServerboundMovePlayerPacket packet) {
            boolean hasPosition = packet.hasPosition();
            boolean hasRotation = packet.hasRotation();
            return new MovePacketState(
                    hasPosition,
                    hasRotation,
                    hasPosition ? packet.getX(0.0D) : 0.0D,
                    hasPosition ? packet.getY(0.0D) : 0.0D,
                    hasPosition ? packet.getZ(0.0D) : 0.0D,
                    hasRotation ? packet.getYRot(0.0F) : 0.0F,
                    hasRotation ? packet.getXRot(0.0F) : 0.0F,
                    packet.isOnGround());
        }
    }
}
